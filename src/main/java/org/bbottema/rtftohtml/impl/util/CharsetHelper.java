package org.bbottema.rtftohtml.impl.util;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CharsetHelper {
	private static String[] CHARSET_PREFIXES = {"", "cp", "iso-", "ibm", "x-windows-", "ms"};

	public static final Charset WINDOWS_CHARSET = Charset.forName("CP1252");

	public static Charset findCharset(String rtfCodePage) {
		return rtfCodePage.equals("65001") || rtfCodePage.equalsIgnoreCase("cp65001") ? UTF_8 : detectCharset(rtfCodePage);
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
	
	@Nullable
	public static Charset rtfCharset(int rtfCharsetNumber) {
		// RTF specific table lifted from wikipedia: https://en.wikipedia.org/wiki/Rich_Text_Format
		switch(rtfCharsetNumber) {
			case 0:
				return Charset.forName("Windows-1252");
			case 1:
			case 2:
			case 77:
				// special Charsets that are not really mappable by java
				return null;
			case 128:
				return Charset.forName("Windows-932");
			case 129:
				return Charset.forName("Windows-949");
			case 130:
				return Charset.forName("ms1361");
			case 134:
				return Charset.forName("Windows-936");
			case 136:
				return Charset.forName("Windows-950");
			case 161:
				return Charset.forName("Windows-1253");
			case 163:
				return Charset.forName("Windows-1254");
			case 177:
				return Charset.forName("Windows-1258");
			case 178:
				return Charset.forName("Windows-1255");
			case 186:
				return Charset.forName("Windows-1256");
			case 204:
				return Charset.forName("Windows-1257");
			case 222:
				return Charset.forName("Windows-1251");
			case 238:
				return Charset.forName("Windows-1250");
			case 255:
				// "Default OEM code page for system locale"
				// This will likely not work in practise. 
				return Charset.defaultCharset();
			default:
				return null;
		}
	}
}