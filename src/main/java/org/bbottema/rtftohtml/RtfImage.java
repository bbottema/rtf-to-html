package org.bbottema.rtftohtml;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * Image payload extracted from an RTF {@code \pict} group.
 */
public final class RtfImage {

	private final String format;
	private final byte[] bytes;
	private final Integer widthPixels;
	private final Integer heightPixels;
	private final Integer widthGoalTwips;
	private final Integer heightGoalTwips;

	public RtfImage(@Nullable String format, @NotNull byte[] bytes,
					@Nullable Integer widthPixels, @Nullable Integer heightPixels,
					@Nullable Integer widthGoalTwips, @Nullable Integer heightGoalTwips) {
		this.format = format;
		this.bytes = Arrays.copyOf(requireNonNull(bytes, "bytes"), bytes.length);
		this.widthPixels = widthPixels;
		this.heightPixels = heightPixels;
		this.widthGoalTwips = widthGoalTwips;
		this.heightGoalTwips = heightGoalTwips;
	}

	@Nullable
	public String getFormat() {
		return format;
	}

	@NotNull
	public byte[] getBytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}

	@Nullable
	public Integer getWidthPixels() {
		return widthPixels;
	}

	@Nullable
	public Integer getHeightPixels() {
		return heightPixels;
	}

	@Nullable
	public Integer getWidthGoalTwips() {
		return widthGoalTwips;
	}

	@Nullable
	public Integer getHeightGoalTwips() {
		return heightGoalTwips;
	}
}
