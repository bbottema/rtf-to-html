package org.bbottema.rtftohtml.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class RtfControlWord implements RtfNode {

	private final String name;
	private final Integer parameter;
	private final RtfPosition position;

	public RtfControlWord(@NotNull String name, @Nullable Integer parameter, @NotNull RtfPosition position) {
		this.name = requireNonNull(name, "name");
		this.parameter = parameter;
		this.position = requireNonNull(position, "position");
	}

	@NotNull
	public String getName() {
		return name;
	}

	@Nullable
	public Integer getParameter() {
		return parameter;
	}

	@NotNull
	@Override
	public RtfPosition getPosition() {
		return position;
	}
}
