package org.bbottema.rtftohtml.impl.util;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.Charset.*;


/**
 * @see <a href="https://en.wikipedia.org/wiki/Rich_Text_Format">Rich Text Format (wikipedia.org)</a>
 * @see <a href="http://ftp.artifax.net/ArtRep/2.0/Help/rtf.htm">RTF (ftp.artifax.net)</a>
 * @see <a href="https://learn.microsoft.com/en-us/windows/win32/intl/code-page-identifiers">Code Page Identifiers (learn.microsoft.com)</a>
 */
public enum CodePage {
    WINDOWS_1252(0, "windows-1252", "ANSI Latin 1; Western European (Windows)"),
    WINDOWS_932(128, "windows-932", "ANSI/OEM Japanese; Japanese (Shift-JIS)"),
    WINDOWS_949(129, "windows-949", "ANSI/OEM Korean (Unified Hangul Code)"),
    MS1361(130, "ms1361", ""),
    WINDOWS_936(134, "windows-936", "ANSI/OEM Simplified Chinese (PRC, Singapore); Chinese Simplified (GB2312)"),
    WINDOWS_950(136, "windows-950", "ANSI/OEM Traditional Chinese (Taiwan; Hong Kong SAR, PRC); Chinese Traditional (Big5)"),
    WINDOWS_1253(161, "windows-1253", "ANSI Greek; Greek (Windows)"),
    WINDOWS_1254_1(162, "windows-1254", "Turkish (DOS)"),
    WINDOWS_1254_2(163, "windows-1254", "ANSI Turkish; Turkish (Windows)"),
    WINDOWS_1255(177, "windows-1255", "ANSI Hebrew; Hebrew (Windows)"),
    WINDOWS_1256(178, "windows-1256", "ANSI Arabic; Arabic (Windows)"),
    WINDOWS_1251(204, "windows-1251", "ANSI Cyrillic; Cyrillic (Windows)"),
    WINDOWS_1257(238, "windows-1257", "ANSI Baltic; Baltic (Windows)"),
    IBM437(254, "IBM437", "OEM United States"),
    DEFAULT(255, defaultCharset().name(), ""),
    IBM775(775, "IBM775", "OEM Baltic; Baltic (DOS)"),
    IBM850(850, "IBM850", "OEM Multilingual Latin 1; Western European (DOS)"),
    IBM852(852, "IBM852", "OEM Latin 2; Central European (DOS)"),
    IBM855(855, "IBM855", "OEM Cyrillic (primarily Russian)"),
    IBM857(857, "IBM857", "OEM Turkish; Turkish (DOS)"),
    IBM00858(858, "IBM00858", "OEM Multilingual Latin 1 + Euro symbol"),
    IBM860(860, "IBM860", "OEM Portuguese; Portuguese (DOS)"),
    IBM861(861, "IBM861", "OEM Icelandic; Icelandic (DOS)"),
    DOS_862(862, "DOS-862", "OEM Hebrew; Hebrew (DOS)"),
    IBM863(863, "IBM863", "OEM French Canadian; French Canadian (DOS)"),
    IBM864(864, "IBM864", "OEM Arabic; Arabic (864)"),
    IBM865(865, "IBM865", "OEM Nordic; Nordic (DOS)"),
    CP866(866, "cp866", "OEM Russian; Cyrillic (DOS)"),
    IBM869(869, "IBM869", "OEM Modern Greek; Greek, Modern (DOS)"),
    WINDOWS_874(874, "windows-874", "Thai (Windows)"),
    SHIFT_JIS(932, "shift_jis", "ANSI/OEM Japanese; Japanese (Shift-JIS)"),
    GB2312(936, "GB2312", "ANSI/OEM Simplified Chinese (PRC, Singapore); Chinese Simplified (GB2312)"),
    KS_C_5601_1987(949, "ks_c_5601-1987", "ANSI/OEM Korean (Unified Hangul Code)"),
    BIG5(950, "big5", "ANSI/OEM Traditional Chinese (Taiwan; Hong Kong SAR, PRC); Chinese Traditional (Big5)"),
    UTF_16(1200, StandardCharsets.UTF_16.name(), "Unicode UTF-16, little endian byte order (BMP of ISO 10646); available only to managed applications"),
    UNICODE_FFFE(1201, "unicodeFFFE", "Unicode UTF-16, big endian byte order; available only to managed applications"),
    WINDOWS_1250(1250, "windows-1250", "ANSI Central European; Central European (Windows)"),
    WINDOWS_1251_1(1251, "windows-1251", "ANSI Cyrillic; Cyrillic (Windows)"),
    WINDOWS_1253_1(1253, "windows-1253", "ANSI Greek; Greek (Windows)"),
    WINDOWS_1254_3(1254, "windows-1254", "ANSI Turkish; Turkish (Windows)"),
    WINDOWS_1255_1(1255, "windows-1255", "ANSI Hebrew; Hebrew (Windows)"),
    WINDOWS_1256_1(1256, "windows-1256", "ANSI Arabic; Arabic (Windows)"),
    WINDOWS_1257_1(1257, "windows-1257", "ANSI Baltic; Baltic (Windows)"),
    WINDOWS_1258(1258, "windows-1258", "ANSI/OEM Vietnamese; Vietnamese (Windows)"),
    JOHAB(1361, "Johab", "Korean (Johab)"),
    MACINTOSH(10000, "macintosh", "MAC Roman; Western European (Mac)"),
    X_MAC_JAPANESE(10001, "x-mac-japanese", "Japanese (Mac)"),
    X_MAC_CHINESETRAD(10002, "x-mac-chinesetrad", "MAC Traditional Chinese (Big5); Chinese Traditional (Mac)"),
    X_MAC_KOREAN(10003, "x-mac-korean", "Korean (Mac)"),
    X_MAC_ARABIC(10004, "x-mac-arabic", "Arabic (Mac)"),
    X_MAC_HEBREW(10005, "x-mac-hebrew", "Hebrew (Mac)"),
    X_MAC_GREEK(10006, "x-mac-greek", "Greek (Mac)"),
    X_MAC_CYRILLIC(10007, "x-mac-cyrillic", "Cyrillic (Mac)"),
    X_MAC_CHINESE_SIMP(10008, "x-mac-chinesesimp", "MAC Simplified Chinese (GB 2312); Chinese Simplified (Mac)"),
    X_MAC_ROMANIAN(10010, "x-mac-romanian", "Romanian (Mac)"),
    X_MAC_UKRAINIAN(10017, "x-mac-ukrainian", "Ukrainian (Mac)"),
    X_MAC_THAI(10021, "x-mac-thai", "Thai (Mac)"),
    X_MAC_CE(10029, "x-mac-ce", "MAC Latin 2; Central European (Mac)"),
    X_MAC_ICELANDIC(10079, "x-mac-icelandic", "Icelandic (Mac)"),
    X_MAC_TURKISH(10081, "x-mac-turkish", "Turkish (Mac)"),
    X_MAC_CROATIAN(10082, "x-mac-croatian", "Croatian (Mac)"),
    UTF_32(12000, "utf-32", "Unicode UTF-32, little endian byte order; available only to managed applications"),
    UTF_32BE(12001, "utf-32BE", "Unicode UTF-32, big endian byte order; available only to managed applications"),
    US_ASCII(20127, StandardCharsets.US_ASCII.name(), "US-ASCII (7-bit)"),
    ISO_8859_1(28591, StandardCharsets.ISO_8859_1.name(), "ISO 8859-1 Latin 1; Western European (ISO)"),
    ISO_8859_2(28592, "ISO-8859-2", "ISO 8859-2 Central European; Central European (ISO)"),
    ISO_8859_3(28593, "ISO-8859-3", "ISO 8859-3 Latin 3"),
    ISO_8859_4(28594, "ISO-8859-4", "ISO 8859-4 Baltic"),
    ISO_8859_5(28595, "ISO-8859-5", "ISO 8859-5 Cyrillic"),
    ISO_8859_6(28596, "ISO-8859-6", "ISO 8859-6 Arabic"),
    ISO_8859_7(28597, "ISO-8859-7", "ISO 8859-7 Greek"),
    ISO_8859_8(28598, "ISO-8859-8", "ISO 8859-8 Hebrew; Hebrew (ISO-Visual)"),
    ISO_8859_9(28599, "ISO-8859-9", "ISO 8859-9 Turkish"),
    ISO_8859_13(28603, "ISO-8859-13", "ISO 8859-13 Estonian"),
    ISO_8859_15(28605, "ISO-8859-15", "ISO 8859-15 Latin 9"),
    ISO_2022_JP(50220, "ISO-2022-JP", "ISO 2022 Japanese with no halfwidth Katakana; Japanese (JIS)"),
    ISO_2022_KR(50225, "ISO-2022-KR", "ISO 2022 Korean"),
    EUC_JP(51932, "EUC-JP", "EUC Japanese"),
    EUC_KR(51949, "EUC-KR", "EUC Korean"),
    HZ_GB2312(52936, "HZ-GB2312", "HZ-GB2312 Simplified Chinese; Chinese Simplified (HZ)"),
    GB18030(54936, "GB18030", "Windows XP and later: GB18030 Simplified Chinese (4 byte); Chinese Simplified (GB18030)"),
    UTF_7(65000, "utf-7", "Unicode (UTF-7)"),
    UTF_8(65001, StandardCharsets.UTF_8.name(), "Unicode (UTF-8)");

    private final int codePage;
    private final String charsetName;
    private final Charset charset;
    private final String remarks;

    private static final Map<Integer, CodePage> CODE_PAGE_MAP = new HashMap<>();

    static {
        for (CodePage codePage : values()) {
            CODE_PAGE_MAP.put(codePage.codePage, codePage);
        }
    }

    CodePage(int codePage, String charsetName, String remarks) {
        this.codePage = codePage;
        this.charsetName = charsetName;
        this.charset = toCharSet(charsetName);
        this.remarks = remarks;
    }

    private static @NotNull Charset toCharSet(String charsetName) {
        try {
            return Charset.forName(charsetName);
        } catch (UnsupportedCharsetException e) {
            return Charset.defaultCharset();
        }
    }

    public static Charset getCharsetByCodePage(int codePage) {
        return CODE_PAGE_MAP.containsKey(codePage) ? CODE_PAGE_MAP.get(codePage).charset : null;
    }

    public String getRemarks() {
        return remarks;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getCharsetName() {
        return charsetName;
    }
}
