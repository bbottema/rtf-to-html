/*
 * Copyright (C) ${project.inceptionYear} Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bbottema.rtftohtml.impl;

import org.bbottema.rtftohtml.RTF2HTMLConverter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * The first and most naive approach to RTF to HTML conversion. It uses Swing's JEditorPane to
 * perform the HTML intepretation for RTF text.
 */
public class RTF2HTMLConverterJEditorPane implements RTF2HTMLConverter {
	
	public static final RTF2HTMLConverter INSTANCE = new RTF2HTMLConverterJEditorPane();
	
	private RTF2HTMLConverterJEditorPane() {}

	@NotNull
	public String rtf2html(@NotNull final String rtf) {
		final JEditorPane p = new JEditorPane();
		p.setContentType("text/rtf");
		final EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
		try {
			kitRtf.read(new StringReader(rtf), p.getDocument(), 0);
			final Writer writer = new StringWriter();
			final EditorKit editorKitForContentType = p.getEditorKitForContentType("text/html");
			editorKitForContentType.write(writer, p.getDocument(), 0, p.getDocument().getLength());
			return writer.toString();
		} catch (IOException | BadLocationException e) {
			throw new RTF2HTMLException("Could not convert RTF to HTML.", e);
		}
	}
}