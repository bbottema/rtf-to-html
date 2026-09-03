package org.bbottema.rtftohtml.internal;

import org.bbottema.rtftohtml.impl.util.CodePage;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;

/**
 * Maps the Windows character-set identifiers used by {@code \fcharsetN} to Java charsets.
 * These identifiers are a different namespace from the code-page numbers used by
 * {@code \ansicpgN} and {@code \cpgN}.
 *
 * @see <a href="https://interoperability.blob.core.windows.net/files/Archive_References/%5BMSFT-RTF%5D.pdf">Rich Text Format Specification 1.9.1</a>
 */
final class RtfCharset {

	private RtfCharset() {
	}

	@Nullable
	static Charset find(int rtfCharset) {
		switch (rtfCharset) {
			case 0:
				return CodePage.WINDOWS_1252.getCharset();
			case 77:
				return CodePage.MACINTOSH.getCharset();
			case 128:
				return CodePage.WINDOWS_932.getCharset();
			case 129:
				return CodePage.WINDOWS_949.getCharset();
			case 130:
				return CodePage.MS1361.getCharset();
			case 134:
				return CodePage.WINDOWS_936.getCharset();
			case 136:
				return CodePage.WINDOWS_950.getCharset();
			case 161:
				return CodePage.WINDOWS_1253.getCharset();
			case 162:
				return CodePage.WINDOWS_1254_1.getCharset();
			case 163:
				return CodePage.WINDOWS_1258.getCharset();
			case 177:
			case 181:
				return CodePage.WINDOWS_1255.getCharset();
			case 178:
			case 179:
			case 180:
				return CodePage.WINDOWS_1256.getCharset();
			case 186:
				return CodePage.WINDOWS_1257_1.getCharset();
			case 204:
				return CodePage.WINDOWS_1251.getCharset();
			case 222:
				return CodePage.WINDOWS_874.getCharset();
			case 238:
				return CodePage.WINDOWS_1250.getCharset();
			case 254:
				return CodePage.IBM437.getCharset();
			case 255:
				return CodePage.DEFAULT.getCharset();
			default:
				return null;
		}
	}
}
