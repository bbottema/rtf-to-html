package org.bbottema.rtftohtml.model;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public final class RtfText implements RtfNode {

	private final String text;
	private final RtfPosition position;

	public RtfText(@NotNull String text, @NotNull RtfPosition position) {
		this.text = requireNonNull(text, "text");
		this.position = requireNonNull(position, "position");
	}

	@NotNull
	public String getText() {
		return text;
	}

	@NotNull
	@Override
	public RtfPosition getPosition() {
		return position;
	}
}
