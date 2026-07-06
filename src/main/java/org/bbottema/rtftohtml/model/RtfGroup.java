package org.bbottema.rtftohtml.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class RtfGroup implements RtfNode {

	private final List<RtfNode> children = new ArrayList<>();
	private final int startOffset;
	private int endOffset;

	public RtfGroup(@NotNull RtfPosition position) {
		RtfPosition requiredPosition = requireNonNull(position, "position");
		this.startOffset = requiredPosition.getStartOffset();
		this.endOffset = requiredPosition.getEndOffset();
	}

	public void addChild(@NotNull RtfNode child) {
		children.add(requireNonNull(child, "child"));
	}

	public void closeAt(int endOffset) {
		this.endOffset = endOffset;
	}

	@NotNull
	public List<RtfNode> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@NotNull
	@Override
	public RtfPosition getPosition() {
		return new RtfPosition(startOffset, endOffset);
	}
}
