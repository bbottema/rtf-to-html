package org.bbottema.rtftohtml.impl;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bbottema.rtftohtml.impl.TestUtils.classpathFileToString;
import static org.bbottema.rtftohtml.impl.TestUtils.normalizeText;

public class RTF2HTMLConverterClassicTest {
    
    @Test
    public void testSimpleConversion() {
        String html = RTF2HTMLConverterClassic.INSTANCE.rtf2html(classpathFileToString("test-messages/input/simple-test.rtf"));
        String expectedHtml = classpathFileToString("test-messages/output/classic/simple-test.html");
        
        assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
    }
    
    @Test
    public void testComplexRtfConversion() {
        String html = RTF2HTMLConverterClassic.INSTANCE.rtf2html(classpathFileToString("test-messages/input/complex-test.rtf"));
        String expectedHtml = classpathFileToString("test-messages/output/classic/complex-test.html");
        
        assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
    }
    
    @Test
    public void testChineseRtfConversion()  {
        String html = RTF2HTMLConverterClassic.INSTANCE.rtf2html(classpathFileToString("test-messages/input/chinese-exotic-test.rtf"));
        String expectedHtml = classpathFileToString("test-messages/output/classic/chinese-exotic-test.html");
        
        assertThat(normalizeText(html)).isEqualTo(normalizeText(expectedHtml));
    }
}