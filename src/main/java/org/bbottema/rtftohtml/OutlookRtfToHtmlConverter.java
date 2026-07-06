package org.bbottema.rtftohtml;

import org.bbottema.rtftohtml.internal.RtfToHtmlEngine;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Outlook-aware RTF to HTML converter.
 * <p>
 * This implementation handles Outlook/Exchange encapsulated HTML and plain-text RTF according to
 * MS-OXRTFEX, and falls back to the standard renderer for non-Outlook documents.
 */
public final class OutlookRtfToHtmlConverter implements RtfToHtmlConverter {

	public static final OutlookRtfToHtmlConverter INSTANCE = new OutlookRtfToHtmlConverter();

	private final RtfParser parser = new RtfParser();
	private final RtfToHtmlOptions options;

	public OutlookRtfToHtmlConverter() {
		this(RtfToHtmlOptions.defaults());
	}

	public OutlookRtfToHtmlConverter(@NotNull RtfToHtmlOptions options) {
		this.options = requireNonNull(options, "options");
	}

	@NotNull
	@Override
	public String toHtml(@NotNull String rtf) {
		return new RtfToHtmlEngine(options).renderOutlook(parser.parse(requireNonNull(rtf, "rtf")));
	}

	@NotNull
	@Override
	public String toHtml(@NotNull byte[] rtfBytes) {
		return new RtfToHtmlEngine(options).renderOutlook(parser.parse(requireNonNull(rtfBytes, "rtfBytes")));
	}
}
