package org.bbottema.rtftohtml.impl.util.charsetdetection;

import org.bbottema.rtftohtml.impl.util.CodePage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RtfCharsetDetectorByAnsicpg {
    private static final Logger LOGGER = LoggerFactory.getLogger(RtfCharsetDetectorByAnsicpg.class);

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