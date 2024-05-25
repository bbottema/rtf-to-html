package org.bbottema.rtftohtml.impl.util.charsetdetection;

import org.bbottema.rtftohtml.RTF2HTMLConverter;
import org.bbottema.rtftohtml.impl.util.CodePage;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.regex.Matcher;

class RtfCharsetDetectorByUsedCharacters {

    @Nullable
    public static Charset detectCharsetByUsedCharacters(String rtfContent) {
        Matcher encodedCharMatcher = RTF2HTMLConverter.ENCODED_CHARACTER.matcher(rtfContent);
        while (encodedCharMatcher.find()) {
            int encodedChar = Integer.parseInt(encodedCharMatcher.group(1), 16);
            if (encodedChar >= 0x80 && encodedChar <= 0xFF) {
                if (isPotentialCyrillic(encodedChar)) {
                    return CodePage.WINDOWS_1251.getCharset();
                }
                if (isPotentialChinese(encodedChar)) {
                    return CodePage.GB18030.getCharset();
                }
                if (isPotentialJapanese(encodedChar)) {
                    return CodePage.WINDOWS_932.getCharset();
                }
                if (isPotentialKorean(encodedChar)) {
                    return CodePage.WINDOWS_949.getCharset();
                }
            }
        }
        return null;
    }

    private static boolean isPotentialCyrillic(int encodedChar) {
        return (encodedChar >= 0xC0 && encodedChar <= 0xFF); // Basic range for Cyrillic in windows-1251
    }

    private static boolean isPotentialChinese(int encodedChar) {
        return (encodedChar >= 0xB0 && encodedChar <= 0xF7); // Range for GB2312/GB18030
    }

    private static boolean isPotentialJapanese(int encodedChar) {
        return (encodedChar >= 0xA1 && encodedChar <= 0xDF); // Range for Shift-JIS (windows-932)
    }

    private static boolean isPotentialKorean(int encodedChar) {
        return (encodedChar >= 0xA1 && encodedChar <= 0xFE); // Range for EUC-KR (windows-949)
    }
}
