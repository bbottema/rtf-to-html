package org.bbottema.rtftohtml.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bbottema.rtftohtml.impl.TestUtils.classpathFileToString;
import static org.bbottema.rtftohtml.impl.TestUtils.normalizeText;

public class RTF2HTMLConverterJEditorPaneTest {
    
    @Test
    public void testSimpleConversion() {
        String html = RTF2HTMLConverterJEditorPane.INSTANCE.rtf2html(classpathFileToString("test-messages/input/simple-test.rtf"));
        String expectedHtml = classpathFileToString("test-messages/output/swing/simple-test.html");
        
        assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
    }
    
    @Test
    public void testComplexRtfConversion() {
        String html = RTF2HTMLConverterClassic.INSTANCE.rtf2html(classpathFileToString("test-messages/input/complex-test.rtf"));
        String expectedHtml = classpathFileToString("test-messages/output/swing/complex-test.html");
        
        assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
    }
    
    @Test
    public void testChineseRtfConversion() {
        String html = RTF2HTMLConverterClassic.INSTANCE.rtf2html(classpathFileToString("test-messages/input/chinese-exotic-test.rtf"));
        String expectedHtml = classpathFileToString("test-messages/output/swing/chinese-exotic-test.html");
        
        assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
    }

    @Test
    public void testUnicodeRtfConversion() {
        String html = RTF2HTMLConverterClassic.INSTANCE.rtf2html(classpathFileToString("test-messages/input/unicode-test.rtf"));
        String expectedHtml = classpathFileToString("test-messages/output/swing/unicode-test.html");

        assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
    }
    
    @Test
    public void testNewlinesConversion() {
        String html = RTF2HTMLConverterJEditorPane.INSTANCE.rtf2html(classpathFileToString("test-messages/input/newlines-test.rtf"));
        String expectedHtml = classpathFileToString("test-messages/output/swing/newlines-test.html");
        
        assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
    }
}