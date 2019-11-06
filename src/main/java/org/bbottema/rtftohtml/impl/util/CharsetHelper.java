package org.bbottema.rtftohtml.impl.util;

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
}