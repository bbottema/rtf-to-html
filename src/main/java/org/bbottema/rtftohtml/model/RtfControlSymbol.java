package org.bbottema.rtftohtml.model;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public final class RtfControlSymbol implements RtfNode {

	private final char symbol;
	private final RtfPosition position;

	public RtfControlSymbol(char symbol, @NotNull RtfPosition position) {
		this.symbol = symbol;
		this.position = requireNonNull(position, "position");
	}

	public char getSymbol() {
		return symbol;
	}

	@NotNull
	@Override
	public RtfPosition getPosition() {
		return position;
	}
}
