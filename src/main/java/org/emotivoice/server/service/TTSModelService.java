package org.emotivoice.server.service;

import org.emotivoice.server.AnnotatedText;

public interface TTSModelService {

    byte[] executeModel(AnnotatedText annotatedText) throws Exception;

    byte[][] executeModel(AnnotatedText[] annotatedTexts) throws Exception;

    String getLastGeneratedID();
}
