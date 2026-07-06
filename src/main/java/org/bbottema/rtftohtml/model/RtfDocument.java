package org.bbottema.rtftohtml.model;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public final class RtfDocument {

	private final RtfGroup root;
	private final String source;
	private final boolean bytePreservingInput;

	public RtfDocument(@NotNull RtfGroup root, @NotNull String source, boolean bytePreservingInput) {
		this.root = requireNonNull(root, "root");
		this.source = requireNonNull(source, "source");
		this.bytePreservingInput = bytePreservingInput;
	}

	@NotNull
	public RtfGroup getRoot() {
		return root;
	}

	@NotNull
	public String getSource() {
		return source;
	}

	public boolean isBytePreservingInput() {
		return bytePreservingInput;
	}
}
