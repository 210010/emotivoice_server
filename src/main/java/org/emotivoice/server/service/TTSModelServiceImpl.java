package org.emotivoice.server.service;

import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

import org.emotivoice.server.AnnotatedText;
import org.emotivoice.server.SSHManager;
import org.emotivoice.server.SSHManager.CommandResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TTSModelServiceImpl implements TTSModelService {

    private final SSHManager sshManager;

    @Value("${model_server.interpreter_path}")
    private String interpreterPath;

    @Value("${model_server.script_path}")
    private String scriptPath;

    @Value("${model_server.outdir_path}")
    private String outdirPath;

    private int stampID = 0;

    private String lastGeneratedID;

    @Autowired
    public TTSModelServiceImpl(SSHManager sshManager) {this.sshManager = sshManager;}

    private String generateID() {
        lastGeneratedID = ZonedDateTime.now(ZoneId.of("UTC+09")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                          + String.format("__%03d", stampID++);
        return lastGeneratedID;
    }

    private String getFilename(String suffix) {
        return lastGeneratedID + suffix + ".wav";
    }

    private String getTargetRemoteFilePath() {
        return getTargetRemoteFilePath("");
    }

    private String getTargetRemoteFilePath(String suffix) {
        String filename = getFilename(suffix);
        return Paths.get(outdirPath, filename).toString();
    }

    private String createInferenceCommand(AnnotatedText annotatedText, String outFilename, boolean isRefAudioStyle) {
        String style = annotatedText.style;
        String text = annotatedText.text;

        if(isRefAudioStyle) {
            return String.format("%s %s --text '%s' --predef-style '%s' --out '%s'",
                                 interpreterPath,
                                 scriptPath,
                                 text,
                                 style,
                                 outFilename
            );
        }
        else {
            /* not implemented yet */
            return "";
        }
    }

    @Override
    public byte[] executeModel(AnnotatedText annotatedText) throws Exception {
        generateID();

        String outFilename = getTargetRemoteFilePath();
        String cmdString = createInferenceCommand(annotatedText, outFilename,annotatedText.params == null);

        String workingDir = Paths.get(scriptPath).getParent().toString();
        try {
            sshManager.connect();
            CommandResult result = sshManager.executeCommand(String.format("cd '%s' && %s", workingDir, cmdString));
            return sshManager.download(outFilename);
        }
        finally {
            sshManager.disconnect();
        }
    }

    @Override
    public byte[][] executeModel(AnnotatedText[] annotatedTexts) throws Exception {
        generateID();

        int idx = 0;
        StringJoiner sjoiner = new StringJoiner(" && ");
        String[] outFilenames = new String[annotatedTexts.length];
        for(AnnotatedText annotatedText : annotatedTexts) {
            String outFilename = getTargetRemoteFilePath(Integer.toString(idx));
            String cmdString = createInferenceCommand(annotatedText, outFilename, annotatedText.params == null);
            sjoiner.add(cmdString);

            outFilenames[idx] = outFilename;
            ++idx;
        }
        String cmdStringConcat = sjoiner.toString();

        byte[][] binArray = new byte[outFilenames.length][];
        String workingDir = Paths.get(scriptPath).getParent().toString();
        try {
            sshManager.connect();
            CommandResult result = sshManager.executeCommand(
                    String.format("cd '%s' && %s", workingDir, cmdStringConcat));

            for (int i = 0; i < outFilenames.length; ++i) {
                binArray[i] = sshManager.download(outFilenames[i]);
            }

            return binArray;
        } finally {
            sshManager.disconnect();
        }
    }

    @Override
    public String getLastGeneratedID() {
        return lastGeneratedID;
    }
}
