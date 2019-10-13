package org.bbottema.rtftohtml;

import org.jetbrains.annotations.NotNull;

public interface RTF2HTMLConverter {
	@NotNull
	String rtf2html(@NotNull String rtf);
}