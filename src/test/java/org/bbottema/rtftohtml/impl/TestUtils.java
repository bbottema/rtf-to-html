package org.bbottema.rtftohtml.impl;

import java.io.InputStream;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class TestUtils {
    public static String classpathFileToString(String classPathFile) {
        InputStream resourceAsStream = RTF2HTMLConverterClassic.class.getClassLoader().getResourceAsStream(classPathFile);
        return new Scanner(requireNonNull(resourceAsStream), UTF_8.name()).useDelimiter("\\A").next();
    }

    public static String normalizeText(String text) {
        return text.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
    }
}
