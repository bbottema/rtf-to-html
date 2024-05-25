package org.bbottema.rtftohtml.impl.util.charsetdetection;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bbottema.rtftohtml.impl.util.CodePage.GB18030;
import static org.bbottema.rtftohtml.impl.util.CodePage.WINDOWS_1251;
import static org.bbottema.rtftohtml.impl.util.CodePage.WINDOWS_1252;
import static org.bbottema.rtftohtml.impl.util.CodePage.WINDOWS_932;

class RtfCharsetDetectorByUsedFont {

    private static final Pattern NON_LATIN_FONT_PATTERN = Pattern.compile("Microsoft YaHei UI|SimSun|NSimSun|FangSong|KaiTi|SimHei|Symbol|Wingdings|Webdings|Arial Cyr|MS Mincho|MS Gothic");

    @Nullable
    public static Charset detectCharsetByUsedFont(String rtfContent) {
        Matcher fontMatcher = NON_LATIN_FONT_PATTERN.matcher(rtfContent);
        return fontMatcher.find() ? charsetForFont(fontMatcher.group()) : null;
    }

    @Nullable
    private static Charset charsetForFont(String fontName) {
        switch (fontName) {
            case "Microsoft YaHei UI":
            case "SimSun":
            case "NSimSun":
            case "FangSong":
            case "KaiTi":
            case "SimHei":
                return GB18030.getCharset();
            case "Arial Cyr":
                return WINDOWS_1251.getCharset();
            case "MS Mincho":
            case "MS Gothic":
                return WINDOWS_932.getCharset();
            default:
                return WINDOWS_1252.getCharset(); // CP1252 would also work
        }
    }
}
