package org.emotivoice.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.emotivoice.server.service.AuthenticationService;
import org.emotivoice.server.service.TTSModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController(value = "/tts")
public class TTSController {

    private TTSModelService ttsModelService;
    private AuthenticationService authenticationService;

    @Value("${wav_dir}")
    private String wavDir;

    @Autowired
    public TTSController(TTSModelService ttsModelService,
                         AuthenticationService authenticationService) {
        this.ttsModelService = ttsModelService;
        this.authenticationService = authenticationService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String getGeneratedSpeech(
            HttpServletRequest req,
            @RequestBody AnnotatedText[] annotatedTexts
    ) throws Exception {
        User user = (User) req.getAttribute("user");

        /* get .wav binary data list */
        byte[][] wavDataArr = ttsModelService.executeModel(annotatedTexts);
        AudioInputStream[] audioInputStreams = new AudioInputStream[wavDataArr.length];
        long totalFrameLen = 0;
        for (int i = 0; i < wavDataArr.length; ++i) {
            audioInputStreams[i] = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavDataArr[i]));
            totalFrameLen += audioInputStreams[i].getFrameLength();
        }

        /* join .wav binary data */
        AudioFormat audioFormat = audioInputStreams[0].getFormat();
        AudioInputStream joinedAudio = new AudioInputStream(
                new SequenceInputStream(Collections.enumeration(Arrays.asList(audioInputStreams))),
                audioFormat,
                totalFrameLen
        );

        String filename = ttsModelService.getLastGeneratedID() + ".wav";
        Path path = Paths.get(wavDir, user.getToken(), filename);

        AudioSystem.write(joinedAudio, Type.WAVE, path.toFile());
        return (Paths.get(user.getToken(), filename)).toString();
    }

    @GetMapping(value = "/audio")
    public StreamingResponseBody streamAudio(
            HttpServletRequest req,
            @RequestParam("filename") String filename
    ) throws Exception {
//        User user = (User) req.getAttribute("user");

//        File file = Paths.get(wavDir, user.getToken(), filename).toFile();
        File file = Paths.get(wavDir, filename).toFile();
        final InputStream is = new FileInputStream(file);
        return os -> {
            byte[] tmp = new byte[4096];
            int nRead;
            while ((nRead = is.read(tmp)) > 0) {
                os.write(tmp, 0, nRead);
            }
            os.flush();
        };
    }
}
