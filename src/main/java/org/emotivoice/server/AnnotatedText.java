package org.emotivoice.server;

import java.util.Arrays;

public class AnnotatedText {
    public String text;
    public String style;
    public float[] params;

    @Override
    public String toString() {
        String ann;
        if(style == null) ann = Arrays.toString(params);
        else ann = "{" + style + "}";
        return String.format("\"%s\"%s", text, ann);
    }
}
