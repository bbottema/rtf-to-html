package org.bbottema.rtftohtml;

import org.jetbrains.annotations.NotNull;

public interface RtfToHtmlConverter {

	@NotNull
	String toHtml(@NotNull String rtf);

	@NotNull
	String toHtml(@NotNull byte[] rtfBytes);
}
