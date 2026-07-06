package org.bbottema.rtftohtml.legacy;

import org.bbottema.rtftohtml.RtfToHtmlConverter;
import org.jetbrains.annotations.NotNull;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

/**
 * Legacy converter based on Swing's built-in RTF reader.
 */
public final class JEditorPaneRtfToHtmlConverter implements RtfToHtmlConverter {

	public static final JEditorPaneRtfToHtmlConverter INSTANCE = new JEditorPaneRtfToHtmlConverter();

	private JEditorPaneRtfToHtmlConverter() {
	}

	@NotNull
	@Override
	public String toHtml(@NotNull final String rtf) {
		final JEditorPane p = new JEditorPane();
		p.setContentType("text/rtf");
		final EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
		try {
			kitRtf.read(new StringReader(requireNonNull(rtf, "rtf")), p.getDocument(), 0);
			final Writer writer = new StringWriter();
			final EditorKit editorKitForContentType = p.getEditorKitForContentType("text/html");
			editorKitForContentType.write(writer, p.getDocument(), 0, p.getDocument().getLength());
			return writer.toString();
		} catch (IOException | BadLocationException e) {
			throw new RtfToHtmlException("Could not convert RTF to HTML.", e);
		}
	}

	@NotNull
	@Override
	public String toHtml(@NotNull byte[] rtfBytes) {
		return toHtml(new String(requireNonNull(rtfBytes, "rtfBytes"), StandardCharsets.ISO_8859_1));
	}
}
