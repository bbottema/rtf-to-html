package org.bbottema.rtftohtml;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardRtfToHtmlConverterTest {

	@Test
	public void testRendersPlainTextAndEscapesHtmlCharacters() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1 A < B & C > D}");

		assertThat(html).isEqualTo("<html><body><p>A &lt; B &amp; C &gt; D</p></body></html>");
	}

	@Test
	public void testRendersEscapedControlSymbols() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1 \\{brace\\} \\\\slash\\~soft\\-hyphen\\_dash}");

		assertThat(html).isEqualTo("<html><body><p>{brace} \\slash\u00A0soft\u00ADhyphen\u2011dash</p></body></html>");
	}

	@Test
	public void testRendersParagraphLineAndTabControls() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1 first\\line second\\tab tabbed\\par after}");

		assertThat(html).isEqualTo("<html><body><p>first<br>second\ttabbed</p><p>after</p></body></html>");
	}

	@Test
	public void testRendersCharacterFormattingAndResets() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1"
				+ "\\b bold\\b0  normal "
				+ "\\i italic\\i0  "
				+ "\\ul under\\ulnone  "
				+ "\\strike gone\\strike0  "
				+ "\\fs21 sized\\plain  plain}");

		assertThat(html).isEqualTo("<html><body><p><strong>bold</strong> normal "
				+ "<em>italic</em> "
				+ "<span style=\"text-decoration:underline;\">under</span> "
				+ "<span style=\"text-decoration:line-through;\">gone</span> "
				+ "<span style=\"font-size:10.5pt;\">sized</span> plain</p></body></html>");
	}

	@Test
	public void testGroupScopedFormattingDoesNotLeak() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1 plain {\\b bold} plain}");

		assertThat(html).isEqualTo("<html><body><p>plain <strong>bold</strong> plain</p></body></html>");
	}

	@Test
	public void testRendersParagraphAlignmentPerParagraph() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1\\pard\\qc centered\\par\\pard\\qr right\\par\\pard left}");

		assertThat(html).isEqualTo("<html><body>"
				+ "<p style=\"text-align:center\">centered</p>"
				+ "<p style=\"text-align:right\">right</p>"
				+ "<p>left</p>"
				+ "</body></html>");
	}

	@Test
	public void testSkipsHiddenAndIgnorableDestinations() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1 visible "
				+ "{\\*\\generator hidden metadata}"
				+ "{\\*\\unknown ignored}"
				+ "\\v invisible\\v0 shown}");

		assertThat(html).isEqualTo("<html><body><p>visible shown</p></body></html>");
	}

	@Test
	public void testSkipsColorTablesAndListTextDestinations() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1"
				+ "{\\colortbl;\\red255\\green0\\blue0;}"
				+ "{\\pntext 1.\\tab}"
				+ "List item}");

		assertThat(html).isEqualTo("<html><body><p>List item</p></body></html>");
	}

	@Test
	public void testDecodesUnicodeAndSkipsTextFallback() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1\\ansi\\uc2 alpha \\u945 ab done}");

		assertThat(html).isEqualTo("<html><body><p>alpha α done</p></body></html>");
	}

	@Test
	public void testDecodesUnicodeAndSkipsHexFallback() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1\\ansi\\uc1 alpha \\u945 \\'3f done}");

		assertThat(html).isEqualTo("<html><body><p>alpha α done</p></body></html>");
	}

	@Test
	public void testDecodesNegativeUnicodeControlWords() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1\\ansi\\uc1 euro \\u-57172 ?}");

		assertThat(html).isEqualTo("<html><body><p>euro €</p></body></html>");
	}

	@Test
	public void testDecodesEscapedBytesUsingDocumentCodePage() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1\\ansi\\ansicpg1251 \\'cf\\'f0\\'e8\\'e2\\'e5\\'f2}");

		assertThat(html).isEqualTo("<html><body><p>Привет</p></body></html>");
	}

	@Test
	public void testDecodesEscapedBytesUsingFontCodePageOverride() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1\\ansi\\ansicpg1252"
				+ "{\\fonttbl{\\f0 Arial;}{\\f1\\fcharset128\\cpg936 Microsoft YaHei;}}"
				+ "\\f1 \\'c4\\'e3\\'ba\\'c3}");

		assertThat(html).isEqualTo("<html><body><p>你好</p></body></html>");
	}

	@Test
	public void testByteInputPreservesRawBytesUntilCodePageDecoding() {
		byte[] prefix = "{\\rtf1\\ansi\\ansicpg1252 caf".getBytes(StandardCharsets.ISO_8859_1);
		byte[] suffix = "}".getBytes(StandardCharsets.ISO_8859_1);
		byte[] rtf = new byte[prefix.length + 1 + suffix.length];
		System.arraycopy(prefix, 0, rtf, 0, prefix.length);
		rtf[prefix.length] = (byte) 0xe9;
		System.arraycopy(suffix, 0, rtf, prefix.length + 1, suffix.length);

		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).isEqualTo("<html><body><p>café</p></body></html>");
	}

	@Test
	public void testExposesPictureGroupsToImageHandler() {
		final RtfImage[] captured = new RtfImage[1];
		StandardRtfToHtmlConverter converter = new StandardRtfToHtmlConverter(RtfToHtmlOptions.builder()
				.imageHandler(image -> {
					captured[0] = image;
					return "cid:\"<&";
				})
				.build());

		String html = converter.toHtml("{\\rtf1\\ansi{\\pict\\jpegblip\\picw10\\pich20\\picwgoal1440\\pichgoal720\\bin3 ABC}}");

		assertThat(captured[0].getFormat()).isEqualTo("jpeg");
		assertThat(captured[0].getBytes()).containsExactly((byte) 'A', (byte) 'B', (byte) 'C');
		assertThat(captured[0].getWidthPixels()).isEqualTo(10);
		assertThat(captured[0].getHeightPixels()).isEqualTo(20);
		assertThat(captured[0].getWidthGoalTwips()).isEqualTo(1440);
		assertThat(captured[0].getHeightGoalTwips()).isEqualTo(720);
		assertThat(html).isEqualTo("<html><body><p><img src=\"cid:&quot;&lt;&amp;\" width=\"10\" height=\"20\"></p></body></html>");
	}

	@Test
	public void testSkipsPictureGroupsWhenImageHandlerReturnsNoSource() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1\\ansi before {\\pict\\pngblip 89504e47} after}");

		assertThat(html).isEqualTo("<html><body><p>before  after</p></body></html>");
	}

	@Test
	public void testIgnoresSourceResidueAfterTheFirstRtfGroup() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("prefix{\\rtf1 body}suffix");

		assertThat(html).isEqualTo("<html><body><p>body</p></body></html>");
	}
}
