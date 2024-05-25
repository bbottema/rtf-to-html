package org.bbottema.rtftohtml.impl.util;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
import static org.bbottema.rtftohtml.impl.util.CodePage.*;

public class CharsetHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CharsetHelper.class);
    private static final String[] CHARSET_PREFIXES = {"", "cp", "iso-", "ibm", "x-windows-", "ms"};

    public static Charset findCharsetForCodePage(String rtfCodePage) {
        return rtfCodePage.equals("65001") || rtfCodePage.equalsIgnoreCase("cp65001")
                ? StandardCharsets.UTF_8
                : detectCharset(rtfCodePage);
    }

    private static Charset detectCharset(String rtfCodePage) {
        for (String prefix : CHARSET_PREFIXES) {
            try {
                return Charset.forName(prefix + rtfCodePage);
            } catch (UnsupportedCharsetException ignore) {
                // ignore
            }
        }
        throw new UnsupportedCharsetException(rtfCodePage);
    }

    public static Charset detectCharsetFromRtfContent(String rtfContent) {
        return ofNullable(detectCharsetByAnsicpg(rtfContent))
                .orElse(WINDOWS_1252.getCharset());
    }

    @Nullable
    public static Charset detectCharsetByAnsicpg(String rtfContent) {
        Matcher matcher = Pattern.compile("\\\\ansicpg(\\d+)").matcher(rtfContent);
        if (matcher.find()) {
            int codePage = Integer.parseInt(matcher.group(1));
            try {
                return CodePage.getCharsetByCodePage(codePage);
            } catch (Exception e) {
                LOGGER.warn("Failed to detect charset from ansicpg: {}", codePage, e);
                return null;
            }
        }
        return null;
    }
}