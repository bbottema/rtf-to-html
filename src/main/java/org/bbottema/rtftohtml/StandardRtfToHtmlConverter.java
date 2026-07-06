package org.bbottema.rtftohtml;

import org.bbottema.rtftohtml.internal.RtfToHtmlEngine;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * General-purpose RTF to HTML converter.
 */
public final class StandardRtfToHtmlConverter implements RtfToHtmlConverter {

	public static final StandardRtfToHtmlConverter INSTANCE = new StandardRtfToHtmlConverter();

	private final RtfParser parser = new RtfParser();
	private final RtfToHtmlOptions options;

	public StandardRtfToHtmlConverter() {
		this(RtfToHtmlOptions.defaults());
	}

	public StandardRtfToHtmlConverter(@NotNull RtfToHtmlOptions options) {
		this.options = requireNonNull(options, "options");
	}

	@NotNull
	@Override
	public String toHtml(@NotNull String rtf) {
		return new RtfToHtmlEngine(options).renderStandard(parser.parse(requireNonNull(rtf, "rtf")));
	}

	@NotNull
	@Override
	public String toHtml(@NotNull byte[] rtfBytes) {
		return new RtfToHtmlEngine(options).renderStandard(parser.parse(requireNonNull(rtfBytes, "rtfBytes")));
	}
}
