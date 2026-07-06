package org.bbottema.rtftohtml;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Options shared by the standard and Outlook-aware RTF to HTML converters.
 */
public final class RtfToHtmlOptions {

	private static final RtfToHtmlOptions DEFAULTS = builder().build();

	private final RtfImageHandler imageHandler;

	private RtfToHtmlOptions(@NotNull Builder builder) {
		this.imageHandler = builder.imageHandler;
	}

	@NotNull
	public static RtfToHtmlOptions defaults() {
		return DEFAULTS;
	}

	@NotNull
	public static Builder builder() {
		return new Builder();
	}

	@NotNull
	public RtfImageHandler getImageHandler() {
		return imageHandler;
	}

	public static final class Builder {

		private RtfImageHandler imageHandler = RtfImageHandler.SKIP;

		private Builder() {
		}

		@NotNull
		public Builder imageHandler(@NotNull RtfImageHandler imageHandler) {
			this.imageHandler = requireNonNull(imageHandler, "imageHandler");
			return this;
		}

		@NotNull
		public RtfToHtmlOptions build() {
			return new RtfToHtmlOptions(this);
		}
	}
}
