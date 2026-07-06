package org.bbottema.rtftohtml;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves an extracted RTF picture to the src attribute that should be written to HTML.
 */
@FunctionalInterface
public interface RtfImageHandler {

	RtfImageHandler SKIP = new RtfImageHandler() {
		@Nullable
		@Override
		public String resolveImage(@NotNull RtfImage image) {
			return null;
		}
	};

	@Nullable
	String resolveImage(@NotNull RtfImage image);
}
