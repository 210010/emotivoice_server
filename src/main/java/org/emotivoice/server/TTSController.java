package org.emotivoice.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
    public List<String> getGeneratedSpeech(
            HttpServletRequest req,
            @RequestBody AnnotatedText[] annotatedTexts
    ) throws Exception {
        User user = (User) req.getAttribute("user");

        List<String> wavFilenames = new ArrayList<>();
        for (AnnotatedText annotatedText : annotatedTexts) {
            byte[] wavData = ttsModelService.executeModel(annotatedText);
            String filename = ttsModelService.getLastFilename();
            new FileOutputStream(Paths.get(wavDir, user.getToken(), filename).toFile()).write(wavData);

            wavFilenames.add(filename);
        }

        return wavFilenames;
    }

    @GetMapping(value = "/audio")
    public StreamingResponseBody streamAudio(
            HttpServletRequest req,
            @RequestParam("filename") String filename
    ) throws Exception {
        User user = (User) req.getAttribute("user");

        File file = Paths.get(wavDir, user.getToken(), filename).toFile();
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
