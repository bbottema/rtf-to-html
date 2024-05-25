package org.bbottema.rtftohtml;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public interface RTF2HTMLConverter {

	Pattern CONTROL_WORD = Pattern.compile("\\\\(([^a-zA-Z])|(([a-zA-Z]+)(-?[\\d]*) ?))");
	Pattern ENCODED_CHARACTER = Pattern.compile("\\\\'([0-9a-fA-F]{2})");

	@NotNull
	String rtf2html(@NotNull String rtf);
}