package org.bbottema.rtftohtml.legacy;

import org.bbottema.rtftohtml.RtfToHtmlConverter;
import org.bbottema.rtftohtml.impl.util.CharsetHelper;
import org.bbottema.rtftohtml.impl.util.CodePage;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;
import static org.bbottema.rtftohtml.impl.util.ByteUtil.hexToString;

/**
 * Legacy regex-based converter kept for comparison with older releases.
 */
public final class ClassicRtfToHtmlConverter implements RtfToHtmlConverter {

	public static final ClassicRtfToHtmlConverter INSTANCE = new ClassicRtfToHtmlConverter();

	private static final String[] HTML_START_TAGS = { "<html", "<Html", "<HTML" };
	private static final String[] HTML_END_TAGS = { "</html>", "</Html>", "</HTML>" };

	private ClassicRtfToHtmlConverter() {
	}

	@NotNull
	@Override
	public String toHtml(@NotNull final String rtf) {
		final Charset charset = extractCodepage(requireNonNull(rtf, "rtf"));
		String plain = fetchHtmlSection(rtf);
		plain = replaceSpecialSequences(plain);
		plain = replaceHexSequences(plain, "(?:\\\\f\\d(?:\\\\'..)+)", CodePage.WINDOWS_1252.getCharset());
		plain = replaceHexSequences(plain, "(?:\\\\'..)+", charset);
		plain = cleanupRemainingSequences(plain);
		plain = replaceLineBreaks(plain);
		return plain;
	}

	@NotNull
	@Override
	public String toHtml(@NotNull byte[] rtfBytes) {
		return toHtml(new String(requireNonNull(rtfBytes, "rtfBytes"), StandardCharsets.ISO_8859_1));
	}

	private String cleanupRemainingSequences(String plain) {
		return plain
				.replaceAll("(\\\\f\\d.+?;)+", "")
				.replaceAll("\\\\\\S+", "")
				.replaceAll("BM__MailAutoSig((?s).*?(?-s))BM__MailAutoSig", "$1");
	}

	private Charset extractCodepage(String rtf) {
		Matcher codePageMatcher = compile("(?:\\\\ansicpg(?<codePage>.+?)\\\\)+").matcher(rtf);
		if (codePageMatcher.find()) {
			return CharsetHelper.findCharsetForCodePage(codePageMatcher.group("codePage"));
		}
		return CodePage.WINDOWS_1252.getCharset();
	}

	private String replaceLineBreaks(final String text) {
		return text
				.replaceAll("( <br/> ( <br/> )+)", " <br/> ")
				.replaceAll("\\r\\n", "\n")
				.replaceAll("[\\r\\u0000]", "");
	}

	private String replaceHexSequences(final String text, String sequencesToMatch, final Charset charset) {
		final StringBuilder res = new StringBuilder();
		int lastPosition = 0;

		final Matcher escapedHexGroupMatcher = compile(sequencesToMatch).matcher(text);
		while (escapedHexGroupMatcher.find()) {
			res.append(text, lastPosition, escapedHexGroupMatcher.start());

			StringBuilder hexText = new StringBuilder();

			String escapedHexGroup = escapedHexGroupMatcher.group(0);
			final Matcher unescapedHexCharacterMatcher = compile("\\\\'(..)").matcher(escapedHexGroup);
			while (unescapedHexCharacterMatcher.find()) {
				hexText.append(unescapedHexCharacterMatcher.group(1));
			}

			res.append(hexToString(hexText.toString(), charset));

			lastPosition = escapedHexGroupMatcher.end();
		}

		if (res.length() == 0) {
			res.append(text);
		} else {
			res.append(text, lastPosition, text.length());
		}

		return res.toString();
	}

	private String fetchHtmlSection(final String text) {
		int htmlStart = -1;
		int htmlEnd = -1;

		for (String htmlStartTag : HTML_START_TAGS) {
			if (htmlStart < 0) {
				htmlStart = text.indexOf(htmlStartTag);
			}
		}
		for (String htmlEndTag : HTML_END_TAGS) {
			if (htmlEnd < 0) {
				htmlEnd = text.indexOf(htmlEndTag);
				if (htmlEnd > 0) {
					htmlEnd = htmlEnd + htmlEndTag.length();
				}
			}
		}

		if (htmlStart > -1 && htmlEnd > -1) {
			return text.substring(htmlStart, htmlEnd + 1);
		}

		String html = "<html><body style=\"font-family:'Courier',monospace;font-size:10pt;\">" + text + "</body></html>";
		html = html.replaceAll("[\\n\\r]+", " ");
		html = html.replaceAll("(http://\\S+)", "<a href=\"$1\">$1</a>");
		return html.replaceAll("mailto:(\\S+@\\S+)", "<a href=\"mailto:$1\">$1</a>");
	}

	@SuppressWarnings("RegExpRedundantEscape")
	private String replaceSpecialSequences(final String text) {
		String replacedText = text;
		replacedText = replacedText.replaceAll("\\{\\\\S+ [^\\s\\\\}]*\\}", "");
		replacedText = replacedText.replaceAll("\\{HYPERLINK[^\\}]*\\}", "");
		replacedText = replacedText.replaceAll("\\{\\\\pntext[^\\}]*\\}", "");
		replacedText = replacedText.replaceAll("\\{\\\\\\*\\\\htmltag\\d+ (&[#\\w]+;)}\\\\htmlrtf.*\\\\htmlrtf0 ", "$1");
		replacedText = replacedText.replaceAll("([^\\\\])" + "\\}+", "$1");
		replacedText = replacedText.replaceAll("([^\\\\])" + "\\{+", "$1");
		replacedText = replacedText.replaceAll("\\\\\\}", "}");
		replacedText = replacedText.replaceAll("\\\\\\{", "{");
		replacedText = replacedText.replaceAll("\\\\pard*", "\n");
		replacedText = replacedText.replaceAll("\\\\tab", "\t");
		replacedText = replacedText.replaceAll("\\\\\\*\\\\\\S+", "");
		return replacedText;
	}
}
