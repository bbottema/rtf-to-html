package org.bbottema.rtftohtml.model;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public final class RtfHexBytes implements RtfNode {

	private final byte[] bytes;
	private final RtfPosition position;

	public RtfHexBytes(@NotNull byte[] bytes, @NotNull RtfPosition position) {
		this.bytes = Arrays.copyOf(requireNonNull(bytes, "bytes"), bytes.length);
		this.position = requireNonNull(position, "position");
	}

	@NotNull
	public byte[] getBytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}

	@NotNull
	@Override
	public RtfPosition getPosition() {
		return position;
	}
}
