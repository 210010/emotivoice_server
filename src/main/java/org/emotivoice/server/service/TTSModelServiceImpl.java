package org.emotivoice.server.service;

import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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

    private String lastGeneratedFilename;

    @Autowired
    public TTSModelServiceImpl(SSHManager sshManager) {this.sshManager = sshManager;}

    private String generateName() {
        return ZonedDateTime.now(ZoneId.of("UTC+09")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                     + String.format("__%03d.wav", stampID++);
    }

    @Override
    public byte[] executeModel(AnnotatedText annotatedText) throws Exception {
        String style = annotatedText.style;
        String text = annotatedText.text;
        String filename = lastGeneratedFilename = generateName();
        String outFilename = Paths.get(outdirPath, filename).toString();
        String cmdString;

        String workingDir = Paths.get(scriptPath).getParent().toString();

        if (annotatedText.params == null) {
            cmdString = String.format("%s %s --text '%s' --predef-style '%s' --out '%s'",
                                             interpreterPath,
                                             scriptPath,
                                             text,
                                             style,
                                             outFilename
                                             );
        }
        else {
            /* NOT SUPPORTED */
            throw new UnsupportedOperationException("params version not supported");
        }

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
    public String getLastFilename() {
        return lastGeneratedFilename;
    }
}
