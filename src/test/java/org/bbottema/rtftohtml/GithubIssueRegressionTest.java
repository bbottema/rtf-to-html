package org.bbottema.rtftohtml;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GithubIssueRegressionTest {

	@Test
	public void testIssue2SkipsOutlookMetadataAndDecodesJapaneseText() {
		String rtf = "{\\rtf1\\ansi\\fbidis\\ansicpg932\\deff0\\deftab720\\fromtext"
				+ "{\\fonttbl{\\f0\\fswiss\\fcharset128 Times New Roman;}{\\f1\\fswiss\\fcharset2 Symbol;}}"
				+ "{\\colortbl;\\red192\\green192\\blue192;}"
				+ "{\\*\\generator Microsoft Exchange Server;}"
				+ "{\\*\\formatConverter converted from text;}"
				+ "\\viewkind5\\viewscale100"
				+ "{\\*\\bkmkstart BM_BEGIN}\\pard\\plain\\f0{\\fs22 Hello\\line"
				+ "\\'82\\'B1\\'82\\'F1\\'82\\'C9\\'82\\'BF\\'82\\'CD\\line"
				+ "}}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).contains("Hello\nこんにちは\n");
		assertThat(html).doesNotContain("Microsoft Exchange Server", "converted from text", "BM_BEGIN");
		assertThat(html).startsWith("<html><body><pre style=\"white-space:pre-wrap\">");
	}

	@Test
	public void testIssue3RendersStrikethroughFormatting() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1\\ansi\\strike - strikethrough\\strike0}");

		assertThat(html).isEqualTo("<html><body><p>"
				+ "<span style=\"text-decoration:line-through;\">- strikethrough</span>"
				+ "</p></body></html>");
	}

	@Test
	public void testIssue4RendersParagraphJustification() {
		String html = StandardRtfToHtmlConverter.INSTANCE.toHtml("{\\rtf1\\ansi"
				+ "\\pard\\qr justify right\\par"
				+ "\\pard\\qc justify center\\par"
				+ "\\pard justify left\\f1\\par}");

		assertThat(html).isEqualTo("<html><body>"
				+ "<p style=\"text-align:right\">justify right</p>"
				+ "<p style=\"text-align:center\">justify center</p>"
				+ "<p>justify left</p>"
				+ "</body></html>");
	}

	@Test
	public void testIssue5ExposesRtfImagesThroughTheImageHandler() {
		final RtfImage[] imageRef = new RtfImage[1];
		StandardRtfToHtmlConverter converter = new StandardRtfToHtmlConverter(RtfToHtmlOptions.builder()
				.imageHandler(image -> {
					imageRef[0] = image;
					return "images/picture.png";
				})
				.build());

		String html = converter.toHtml("{\\rtf1\\ansi{\\pict\\pngblip\\picw10\\pich20 89504e47}}");

		assertThat(imageRef[0].getFormat()).isEqualTo("png");
		assertThat(imageRef[0].getBytes()).containsExactly((byte) 0x89, 0x50, 0x4e, 0x47);
		assertThat(html).isEqualTo("<html><body><p>"
				+ "<img src=\"images/picture.png\" width=\"10\" height=\"20\">"
				+ "</p></body></html>");
	}

	@Test
	public void testIssue6PreservesOutlookFromTextParagraphBreaks() {
		String rtf = "{\\rtf1\\ansi\\ansicpg1252\\fromtext \\fbidis \\deff0{\\fonttbl\r\n"
				+ "\r\n"
				+ "{\\f0\\fswiss Arial;}\r\n"
				+ "\r\n"
				+ "{\\f1\\fmodern Courier New;}\r\n"
				+ "\r\n"
				+ "{\\f2\\fnil\\fcharset2 Symbol;}\r\n"
				+ "\r\n"
				+ "{\\f3\\fmodern\\fcharset0 Courier New;}}\r\n"
				+ "\r\n"
				+ "{\\colortbl\\red0\\green0\\blue0;\\red0\\green0\\blue255;}\r\n"
				+ "\r\n"
				+ "\\uc1\\pard\\plain\\deftab360 \\f0\\fs20 Hello there\\par\r\n"
				+ "\r\n"
				+ "\\par\r\n"
				+ "\r\n"
				+ "This is a plain text email. I'd like to keep \\par\r\n"
				+ "\r\n"
				+ "\\par\r\n"
				+ "\r\n"
				+ "The line breaks when using emailToEml()\\par\r\n"
				+ "\r\n"
				+ "\\par\r\n"
				+ "\r\n"
				+ "All the best\\par\r\n"
				+ "\r\n"
				+ "Andy\\par\r\n"
				+ "\r\n"
				+ "}";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).isEqualTo("<html><body><pre style=\"white-space:pre-wrap\">"
				+ "Hello there\n"
				+ "\n"
				+ "This is a plain text email. I'd like to keep \n"
				+ "\n"
				+ "The line breaks when using emailToEml()\n"
				+ "\n"
				+ "All the best\n"
				+ "Andy\n"
				+ "</pre></body></html>");
	}

	@Test
	public void testIssue7PreservesLineControlsInsideOutlookHtmlPreBlocks() {
		String rtf = "{\\rtf1\\ansi\\ansicpg1252\\fromhtml1 \\fbidis \\deff0"
				+ "{\\*\\htmltag128 <pre>}"
				+ "\\htmlrtf {"
				+ "\\pard\\plain\\f1\\fs20 \\htmlrtf0 \\htmlrtf {"
				+ "\\htmlrtf0 \\htmlrtf {\\cf4 \\fs20 \\f5 \\htmlrtf0 "
				+ "Received: from ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) by\\line\r\n"
				+ " ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) with Microsoft SMTP Server\\line\r\n"
				+ " (TLS) id 15.0.1076.9; Mon, 11 Apr 2016 09:08:47 -0400\\line\r\n"
				+ "Received:}}\\htmlrtf0"
				+ "{\\*\\htmltag136 </pre>}\\htmlrtf }\\htmlrtf0 "
				+ "}*\\";

		String html = OutlookRtfToHtmlConverter.INSTANCE.toHtml(rtf);

		assertThat(html).isEqualTo("<pre>Received: from ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) by\n"
				+ " ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) with Microsoft SMTP Server\n"
				+ " (TLS) id 15.0.1076.9; Mon, 11 Apr 2016 09:08:47 -0400\n"
				+ "Received:</pre>");
	}
}
