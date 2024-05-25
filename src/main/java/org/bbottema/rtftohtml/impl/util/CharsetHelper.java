package org.bbottema.rtftohtml.impl.util;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharsetHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharsetHelper.class);

    private static final String[] CHARSET_PREFIXES = {"", "cp", "iso-", "ibm", "x-windows-", "ms"};
    public static final Charset WINDOWS_CHARSET = Charset.forName("windows-1252"); // forName("CP1252") would also work
    private static final Pattern NON_LATIN_FONT_PATTERN = Pattern.compile("Microsoft YaHei UI|SimSun|NSimSun|FangSong|KaiTi|SimHei|Symbol|Wingdings|Webdings|Arial Cyr|MS Mincho|MS Gothic");

    public static Charset findCharsetForCodePage(String rtfCodePage) {
        return rtfCodePage.equals("65001") || rtfCodePage.equalsIgnoreCase("cp65001") ? StandardCharsets.UTF_8 : detectCharset(rtfCodePage);
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
        Charset charset = CharsetHelper.detectCharsetFromAnsicpg(rtfContent);
        if (charset != null && charset.name().equals("windows-1252")) {
            Matcher fontMatcher = NON_LATIN_FONT_PATTERN.matcher(rtfContent);
            if (fontMatcher.find()) {
                return detectCharsetBasedOnFont(fontMatcher.group());
            }
        }
        return charset != null ? charset : WINDOWS_CHARSET; // Default to windows-1252 if not found
    }

    private static Charset detectCharsetBasedOnFont(String fontName) {
        switch (fontName) {
            case "Microsoft YaHei UI":
            case "SimSun":
            case "NSimSun":
            case "FangSong":
            case "KaiTi":
            case "SimHei":
                return Charset.forName("GB18030");
            case "Arial Cyr":
                return Charset.forName("windows-1251"); // Russian
            case "MS Mincho":
            case "MS Gothic":
                return Charset.forName("windows-932"); // Japanese
            default:
                return WINDOWS_CHARSET;
        }
    }

    public static Charset detectCharsetFromAnsicpg(String rtfContent) {
        Matcher matcher = Pattern.compile("\\\\ansicpg(\\d+)").matcher(rtfContent);
        if (matcher.find()) {
            int codePage = Integer.parseInt(matcher.group(1));
            try {
                return rtfCharset(codePage);
            } catch (Exception e) {
                LOGGER.warn("Failed to detect charset from ansicpg: {}", codePage, e);
                return null;
            }
        }
        return null;
    }

    @Nullable
    public static Charset rtfCharset(int rtfCharsetNumber) {
        // RTF specific table lifted from wikipedia: https://en.wikipedia.org/wiki/Rich_Text_Format
        // Augmented by info from here http://ftp.artifax.net/ArtRep/2.0/Help/rtf.htm
        // And by info from here: https://learn.microsoft.com/en-us/windows/win32/intl/code-page-identifiers
        switch (rtfCharsetNumber) {
            case 0:
            case 1252:
                return Charset.forName("windows-1252");
            case 1:
            case 2:
            case 77:
                // Special charsets that are not really mappable by Java
                return null;
            case 128:
                return Charset.forName("windows-932");
            case 129:
                return Charset.forName("windows-949");
            case 130:
                return Charset.forName("ms1361");
            case 134:
                return Charset.forName("windows-936");
            case 136:
                return Charset.forName("windows-950");
            case 161:
                return Charset.forName("windows-1253");
            case 162:
                return Charset.forName("windows-1254"); // Turkish (DOS)
            case 163:
                return Charset.forName("windows-1254");
            case 177:
            case 181:
                return Charset.forName("windows-1255");
            case 178:
            case 179:
            case 180:
            case 186:
                return Charset.forName("windows-1256");
            case 204:
            case 222:
                return Charset.forName("windows-1251");
            case 238:
                return Charset.forName("windows-1257");
            case 254:
                return Charset.forName("IBM437");
            case 255:
                return Charset.defaultCharset();
            case 775:
                return Charset.forName("IBM775");
            case 850:
                return Charset.forName("IBM850");
            case 852:
                return Charset.forName("IBM852");
            case 855:
                return Charset.forName("IBM855");
            case 857:
                return Charset.forName("IBM857");
            case 858:
                return Charset.forName("IBM00858");
            case 860:
                return Charset.forName("IBM860");
            case 861:
                return Charset.forName("IBM861");
            case 862:
                return Charset.forName("DOS-862");
            case 863:
                return Charset.forName("IBM863");
            case 864:
                return Charset.forName("IBM864");
            case 865:
                return Charset.forName("IBM865");
            case 866:
                return Charset.forName("cp866");
            case 869:
                return Charset.forName("IBM869");
            case 874:
                return Charset.forName("windows-874");
            case 932:
                return Charset.forName("shift_jis");
            case 936:
                return Charset.forName("GB2312");
            case 949:
                return Charset.forName("ks_c_5601-1987");
            case 950:
                return Charset.forName("big5");
            case 1200:
                return StandardCharsets.UTF_16;
            case 1201:
                return Charset.forName("unicodeFFFE");
            case 1250:
                return Charset.forName("windows-1250");
            case 1251:
                return Charset.forName("windows-1251");
            case 1253:
                return Charset.forName("windows-1253");
            case 1254:
                return Charset.forName("windows-1254");
            case 1255:
                return Charset.forName("windows-1255");
            case 1256:
                return Charset.forName("windows-1256");
            case 1257:
                return Charset.forName("windows-1257");
            case 1258:
                return Charset.forName("windows-1258");
            case 1361:
                return Charset.forName("Johab");
            case 10000:
                return Charset.forName("macintosh");
            case 10001:
                return Charset.forName("x-mac-japanese");
            case 10002:
                return Charset.forName("x-mac-chinesetrad");
            case 10003:
                return Charset.forName("x-mac-korean");
            case 10004:
                return Charset.forName("x-mac-arabic");
            case 10005:
                return Charset.forName("x-mac-hebrew");
            case 10006:
                return Charset.forName("x-mac-greek");
            case 10007:
                return Charset.forName("x-mac-cyrillic");
            case 10008:
                return Charset.forName("x-mac-chinesesimp");
            case 10010:
                return Charset.forName("x-mac-romanian");
            case 10017:
                return Charset.forName("x-mac-ukrainian");
            case 10021:
                return Charset.forName("x-mac-thai");
            case 10029:
                return Charset.forName("x-mac-ce");
            case 10079:
                return Charset.forName("x-mac-icelandic");
            case 10081:
                return Charset.forName("x-mac-turkish");
            case 10082:
                return Charset.forName("x-mac-croatian");
            case 12000:
                return Charset.forName("utf-32");
            case 12001:
                return Charset.forName("utf-32BE");
            case 20127:
                return StandardCharsets.US_ASCII;
            case 28591:
                return StandardCharsets.ISO_8859_1;
            case 28592:
                return Charset.forName("ISO-8859-2");
            case 28593:
                return Charset.forName("ISO-8859-3");
            case 28594:
                return Charset.forName("ISO-8859-4");
            case 28595:
                return Charset.forName("ISO-8859-5");
            case 28596:
                return Charset.forName("ISO-8859-6");
            case 28597:
                return Charset.forName("ISO-8859-7");
            case 28598:
                return Charset.forName("ISO-8859-8");
            case 28599:
                return Charset.forName("ISO-8859-9");
            case 28603:
                return Charset.forName("ISO-8859-13");
            case 28605:
                return Charset.forName("ISO-8859-15");
            case 50220:
                return Charset.forName("ISO-2022-JP");
            case 50225:
                return Charset.forName("ISO-2022-KR");
            case 51932:
                return Charset.forName("EUC-JP");
            case 51949:
                return Charset.forName("EUC-KR");
            case 52936:
                return Charset.forName("HZ-GB2312");
            case 54936:
                return Charset.forName("GB18030");
            case 65000:
                return Charset.forName("utf-7");
            case 65001:
                return StandardCharsets.UTF_8;
            default:
                return null;
        }
    }

}