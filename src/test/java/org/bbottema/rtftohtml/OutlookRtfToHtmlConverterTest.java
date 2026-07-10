package org.bbottema.rtftohtml;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bbottema.rtftohtml.impl.TestUtils.classpathFileToString;
import static org.bbottema.rtftohtml.impl.TestUtils.normalizeText;

public class OutlookRtfToHtmlConverterTest {

	@Test
	public void testSimpleConversion() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/simple-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/outlook/simple-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}

	@Test
	public void testComplexRtfConversion() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/complex-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/outlook/complex-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}

	@Test
	public void testChineseRtfConversion() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/chinese-exotic-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/outlook/chinese-exotic-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}

	@Test
	public void testChineseRtfConversion_WithCharsetOverrideFromUsedFont() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/chinese-fontbased-charset-override.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/outlook/chinese-fontbased-charset-override-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}

	@Test
	public void testUnicodeRtfConversion() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/unicode-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/outlook/unicode-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}

	@Test
	public void testMixedCharsets() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/mixed-charsets-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/outlook/mixed-charsets-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}

	@Test
	public void testHebrewCharset() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/hebrew-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/outlook/hebrew-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}

	@Test
	public void testRussianCharset() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/russian-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/outlook/russian-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}

	@Test
	public void testNewlinesConversion() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/newlines-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/outlook/newlines-test.html");

		assertThat(removeFinalLineBreak(normalizeText(html))).isEqualTo(removeFinalLineBreak(normalizeText(expectedHtml)));
	}

	@Test
	public void testBulletNumbers() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/bulletnumber-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/outlook/bulletnumber-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}

	@Test
	public void testOutlookFromTextSkipsMetadataAndDecodesJapaneseByFontCharset() {
		String rtf = "{\\rtf1\\ansi\\ansicpg932\\fromtext"
				+ "{\\fonttbl{\\f0\\fswiss\\fcharset128 MS PGothic;}}"
				+ "{\\*\\generator Microsoft Exchange;}"
				+ "{\\*\\formatConverter Outlook;}"
				+ "{\\*\\bkmkstart BM_BEGIN}"
				+ "\\f0 \\'82\\'b1\\'82\\'f1\\'82\\'c9\\'82\\'bf\\'82\\'cd\\line next}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).contains("こんにちは\nnext");
		assertThat(html).doesNotContain("generator", "formatConverter", "BM_BEGIN");
	}

	@Test
	public void testLineBreaksArePreservedInsideOutlookHtmlPreContent() {
		String rtf = "{\\rtf1\\ansi\\ansicpg1252\\fromhtml1{\\fonttbl{\\f0 Arial;}}"
				+ "{\\*\\htmltag84 <pre>}\\htmlrtf {\\htmlrtf0 first\\line second}"
				+ "{\\*\\htmltag90 </pre>}}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).isEqualTo("<pre>first\nsecond</pre>");
	}

	@Test
	public void testOutlookFromHtmlCopiesHtmlTagsAndSuppressesHtmlRtfFallback() {
		String rtf = "{\\rtf1\\ansi\\ansicpg1252\\fromhtml1"
				+ "{\\*\\htmltag18 <p title=\"x\">}"
				+ "\\htmlrtf ignored fallback\\htmlrtf0 Visible"
				+ "{\\*\\htmltag4 </p>}}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).isEqualTo("<p title=\"x\">Visible</p>");
	}

	@Test
	public void testOutlookFromHtmlDecodesVisibleBytesOutsideHtmlTags() {
		String rtf = "{\\rtf1\\ansi\\ansicpg1252\\fromhtml1"
				+ "{\\*\\htmltag3 <p>}"
				+ "caf\\'e9"
				+ "{\\*\\htmltag4 </p>}}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).isEqualTo("<p>café</p>");
	}

	@Test
	public void testOutlookFromHtmlCanEmitResolvedPictureReferences() {
		OutlookRtfToHtmlConverter converter = new OutlookRtfToHtmlConverter(RtfToHtmlOptions.builder()
				.imageHandler(image -> "cid:from-html.png")
				.build());
		String rtf = "{\\rtf1\\ansi\\fromhtml1{\\pict\\pngblip 89504e47}}";

		String html = converter.toHtml(rtf);

		assertThat(html).isEqualTo("<img src=\"cid:from-html.png\">");
	}

	@Test
	public void testOutlookFromTextEscapesHtmlSpecialCharacters() {
		String rtf = "{\\rtf1\\ansi\\ansicpg1252\\fromtext <tag attr=\"x\"> & text}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).isEqualTo("<html><body><div style=\"white-space:pre-wrap\">"
				+ "&lt;tag attr=\"x\"&gt; &amp; text"
				+ "</div></body></html>");
	}

	@Test
	public void testOutlookFromTextDoesNotUsePreWrapper() {
		String rtf = "{\\rtf1\\ansi\\ansicpg1252\\fromtext first\\line second}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).isEqualTo("<html><body><div style=\"white-space:pre-wrap\">first\nsecond</div></body></html>");
		assertThat(html).doesNotContain("<pre");
	}

	@Test
	public void testGenericRtfRendersStrikeAndAlignment() {
		String rtf = "{\\rtf1\\ansi\\ansicpg1252"
				+ "\\pard\\qc Center\\par"
				+ "\\pard\\qr Right\\par"
				+ "\\pard Plain \\strike gone\\strike0 .}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).contains("<p style=\"text-align:center\">Center</p>");
		assertThat(html).contains("<p style=\"text-align:right\">Right</p>");
		assertThat(html).contains("Plain <span style=\"text-decoration:line-through;\">gone</span>.");
	}

	@Test
	public void testOutlookConverterFallsBackToStandardRendererWhenNoOutlookMarkerExists() {
		String rtf = "{\\rtf1\\ansi Plain \\i italic\\i0}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).isEqualTo(StandardRtfToHtmlConverter.INSTANCE.toHtml(rtf));
	}

	@Test
	public void testStandardConverterTreatsOutlookControlsAsRegularRtf() {
		String rtf = "{\\rtf1\\ansi\\fromtext Plain \\b bold\\b0}";

		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).isEqualTo("<html><body><p>Plain <strong>bold</strong></p></body></html>");
	}

	@Test
	public void testPictGroupsAreExposedToTheImageHandler() {
		final RtfImage[] capturedImage = new RtfImage[1];
		OutlookRtfToHtmlConverter converter = new OutlookRtfToHtmlConverter(RtfToHtmlOptions.builder()
				.imageHandler(image -> {
					capturedImage[0] = image;
					return "cid:image.png";
				})
				.build());

		String html = converter.toHtml("{\\rtf1\\ansi{\\pict\\pngblip\\picw10\\pich20 89504e47}}");

		assertThat(capturedImage[0].getFormat()).isEqualTo("png");
		assertThat(capturedImage[0].getWidthPixels()).isEqualTo(10);
		assertThat(capturedImage[0].getHeightPixels()).isEqualTo(20);
		assertThat(capturedImage[0].getBytes()).containsExactly((byte) 0x89, 0x50, 0x4e, 0x47);
		assertThat(html).contains("<img src=\"cid:image.png\" width=\"10\" height=\"20\">");
	}

	@Test
	public void testFontCpgOverridesFontCharset() {
		String rtf = "{\\rtf1\\ansi\\ansicpg1252\\fromtext"
				+ "{\\fonttbl{\\f0 Arial;}{\\f1\\fcharset128\\cpg936 Microsoft YaHei;}}"
				+ "\\f1 \\'c4\\'e3\\'ba\\'c3}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).contains("你好");
	}

	@Test
	public void testUnicodeFallbackCharactersAreSkipped() {
		String rtf = "{\\rtf1\\ansi\\ansicpg1252\\fromtext\\uc1 Unicode \\u945 ? done}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).contains("Unicode α done");
		assertThat(html).doesNotContain("α ?");
	}

	@Test
	public void testByteInputPreservesRawRtfBytesUntilCodePageDecoding() {
		byte[] prefix = "{\\rtf1\\ansi\\ansicpg1252\\fromtext caf".getBytes(StandardCharsets.ISO_8859_1);
		byte[] suffix = "}".getBytes(StandardCharsets.ISO_8859_1);
		byte[] rtf = new byte[prefix.length + 1 + suffix.length];
		System.arraycopy(prefix, 0, rtf, 0, prefix.length);
		rtf[prefix.length] = (byte) 0xe9;
		System.arraycopy(suffix, 0, rtf, prefix.length + 1, suffix.length);

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).contains("café");
	}

	@Test
	public void testBinaryPayloadsAreSkippedOutsidePictureGroups() {
		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(
				"{\\rtf1\\ansi\\ansicpg1252\\fromtext before \\bin3 ABC after}");

		assertThat(html).contains("before  after");
		assertThat(html).doesNotContain("ABC");
	}

	private static String removeFinalLineBreak(String text) {
		return text.endsWith("\n") ? text.substring(0, text.length() - 1) : text;
	}
}
