package org.bbottema.rtftohtml.model;

/**
 * Character offsets in the original RTF source.
 */
public final class RtfPosition {

	private final int startOffset;
	private final int endOffset;

	public RtfPosition(int startOffset, int endOffset) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}
}
