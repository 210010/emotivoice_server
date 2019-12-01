package org.emotivoice.server.service;

import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.nio.file.Paths;

import org.emotivoice.server.AnnotatedText;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TTSModelServiceTest {

    @Autowired
    private TTSModelService ttsModelService;

    @Value("${wav_dir}") private String wavDir;

    @Test
    public void executeModel() throws Exception {
        AnnotatedText annotatedText = new AnnotatedText();
        annotatedText.text = "이것은 테스트를 위한 텍스트입니다";
        annotatedText.style = "angry_002";

        byte[] wavBinary = ttsModelService.executeModel(annotatedText);
        new FileOutputStream(Paths.get(wavDir, "output.wav").toFile()).write(wavBinary);
    }
}