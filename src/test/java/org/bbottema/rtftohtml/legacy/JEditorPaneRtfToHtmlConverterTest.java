package org.bbottema.rtftohtml.legacy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bbottema.rtftohtml.impl.TestUtils.classpathFileToString;
import static org.bbottema.rtftohtml.impl.TestUtils.normalizeText;

public class JEditorPaneRtfToHtmlConverterTest {

	@Test
	public void testSimpleConversion() {
		String html = JEditorPaneRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/simple-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/swing/simple-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}

	@Test
	public void testComplexRtfConversion() {
		String html = JEditorPaneRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/complex-test.rtf"));

		assertThat(normalizeText(html))
				.contains("<html>")
				.contains("<body>")
				.contains("</html>");
	}

	@Test
	public void testChineseRtfConversion() {
		String html = JEditorPaneRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/chinese-exotic-test.rtf"));

		assertThat(normalizeText(html))
				.contains("<html>")
				.contains("Dears")
				.contains("</html>");
	}

	@Test
	public void testUnicodeRtfConversion() {
		String html = JEditorPaneRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/unicode-test.rtf"));

		assertThat(normalizeText(html))
				.contains("<html>")
				.contains("<body>")
				.contains("</html>");
	}

	@Test
	public void testNewlinesConversion() {
		String html = JEditorPaneRtfToHtmlConverter.INSTANCE.toHtml(classpathFileToString("test-messages/input/newlines-test.rtf"));
		String expectedHtml = classpathFileToString("test-messages/output/swing/newlines-test.html");

		assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
	}
}
