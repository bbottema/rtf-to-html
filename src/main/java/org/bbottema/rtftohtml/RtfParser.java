package org.bbottema.rtftohtml;

import org.bbottema.rtftohtml.model.RtfBinary;
import org.bbottema.rtftohtml.model.RtfControlSymbol;
import org.bbottema.rtftohtml.model.RtfControlWord;
import org.bbottema.rtftohtml.model.RtfDocument;
import org.bbottema.rtftohtml.model.RtfGroup;
import org.bbottema.rtftohtml.model.RtfHexBytes;
import org.bbottema.rtftohtml.model.RtfPosition;
import org.bbottema.rtftohtml.model.RtfText;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import static java.util.Objects.requireNonNull;
import static org.bbottema.rtftohtml.impl.util.ByteUtil.hexStringToByteArray;

public final class RtfParser {

	public RtfParser() {
	}

	@NotNull
	public RtfDocument parse(@NotNull String rtf) {
		return parse(requireNonNull(rtf, "rtf"), false);
	}

	@NotNull
	public RtfDocument parse(@NotNull byte[] rtfBytes) {
		return parse(new String(requireNonNull(rtfBytes, "rtfBytes"), StandardCharsets.ISO_8859_1), true);
	}

	@NotNull
	public RtfDocument parse(@NotNull InputStream inputStream) {
		return parse(readAll(requireNonNull(inputStream, "inputStream")));
	}

	@NotNull
	private RtfDocument parse(@NotNull String rtf, boolean bytePreservingInput) {
		RtfGroup syntheticRoot = new RtfGroup(new RtfPosition(0, rtf.length()));
		LinkedList<RtfGroup> stack = new LinkedList<>();
		stack.addFirst(syntheticRoot);

		int index = 0;
		while (index < rtf.length()) {
			char c = rtf.charAt(index);
			if (c == '\0' || c == '\r' || c == '\n') {
				index++;
			} else if (c == '{') {
				RtfGroup group = new RtfGroup(new RtfPosition(index, -1));
				stack.getFirst().addChild(group);
				stack.addFirst(group);
				index++;
			} else if (c == '}') {
				if (stack.size() > 1) {
					stack.removeFirst().closeAt(index + 1);
				}
				index++;
			} else if (c == '\\') {
				if (isHexEscape(rtf, index)) {
					HexRun hexRun = readHexRun(rtf, index);
					stack.getFirst().addChild(new RtfHexBytes(hexRun.bytes, new RtfPosition(index, hexRun.nextIndex)));
					index = hexRun.nextIndex;
				} else {
					Control control = readControl(rtf, index);
					if (control.word == null) {
						stack.getFirst().addChild(new RtfControlSymbol(control.symbol, new RtfPosition(index, control.nextIndex)));
						index = control.nextIndex;
					} else if ("bin".equals(control.word) && control.parameter != null) {
						int byteCount = Math.max(0, control.parameter);
						int endIndex = Math.min(rtf.length(), control.nextIndex + byteCount);
						byte[] bytes = new byte[endIndex - control.nextIndex];
						for (int i = control.nextIndex; i < endIndex; i++) {
							bytes[i - control.nextIndex] = (byte) rtf.charAt(i);
						}
						stack.getFirst().addChild(new RtfBinary(bytes, new RtfPosition(index, endIndex)));
						index = endIndex;
					} else {
						stack.getFirst().addChild(new RtfControlWord(control.word, control.parameter, new RtfPosition(index, control.nextIndex)));
						index = control.nextIndex;
					}
				}
			} else {
				int nextIndex = readTextEnd(rtf, index);
				stack.getFirst().addChild(new RtfText(rtf.substring(index, nextIndex), new RtfPosition(index, nextIndex)));
				index = nextIndex;
			}
		}

		return new RtfDocument(syntheticRoot, rtf, bytePreservingInput);
	}

	private byte[] readAll(@NotNull InputStream inputStream) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int read;
			while ((read = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, read);
			}
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read RTF input stream.", e);
		}
	}

	private int readTextEnd(@NotNull String rtf, int index) {
		int cursor = index;
		while (cursor < rtf.length()) {
			char c = rtf.charAt(cursor);
			if (c == '\0' || c == '\r' || c == '\n' || c == '{' || c == '}' || c == '\\') {
				break;
			}
			cursor++;
		}
		return cursor;
	}

	private boolean isHexEscape(@NotNull String rtf, int index) {
		return index + 3 < rtf.length()
				&& rtf.charAt(index) == '\\'
				&& rtf.charAt(index + 1) == '\''
				&& isHexDigit(rtf.charAt(index + 2))
				&& isHexDigit(rtf.charAt(index + 3));
	}

	private HexRun readHexRun(@NotNull String rtf, int index) {
		StringBuilder hex = new StringBuilder();
		int cursor = index;
		while (isHexEscape(rtf, cursor)) {
			hex.append(rtf.charAt(cursor + 2));
			hex.append(rtf.charAt(cursor + 3));
			cursor += 4;
		}
		return new HexRun(hexStringToByteArray(hex.toString()), cursor);
	}

	private Control readControl(@NotNull String rtf, int slashIndex) {
		int cursor = slashIndex + 1;
		if (cursor >= rtf.length()) {
			return Control.symbol('\\', slashIndex + 1);
		}

		char first = rtf.charAt(cursor);
		if (!isAsciiLetter(first)) {
			return Control.symbol(first, cursor + 1);
		}

		int wordStart = cursor;
		while (cursor < rtf.length() && isAsciiLetter(rtf.charAt(cursor))) {
			cursor++;
		}
		int wordEnd = cursor;

		boolean hasParameter = false;
		int sign = 1;
		if (cursor < rtf.length() && rtf.charAt(cursor) == '-') {
			sign = -1;
			cursor++;
		}

		int value = 0;
		while (cursor < rtf.length() && Character.isDigit(rtf.charAt(cursor))) {
			hasParameter = true;
			value = value * 10 + (rtf.charAt(cursor) - '0');
			cursor++;
		}

		if (cursor < rtf.length() && rtf.charAt(cursor) == ' ') {
			cursor++;
		}

		return Control.word(rtf.substring(wordStart, wordEnd), hasParameter ? sign * value : null, cursor);
	}

	private boolean isAsciiLetter(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	private boolean isHexDigit(char c) {
		return c >= '0' && c <= '9'
				|| c >= 'a' && c <= 'f'
				|| c >= 'A' && c <= 'F';
	}

	private static final class Control {
		private final String word;
		private final Character symbol;
		private final Integer parameter;
		private final int nextIndex;

		private Control(String word, Character symbol, Integer parameter, int nextIndex) {
			this.word = word;
			this.symbol = symbol;
			this.parameter = parameter;
			this.nextIndex = nextIndex;
		}

		private static Control word(@NotNull String word, Integer parameter, int nextIndex) {
			return new Control(word, null, parameter, nextIndex);
		}

		private static Control symbol(char symbol, int nextIndex) {
			return new Control(null, symbol, null, nextIndex);
		}
	}

	private static final class HexRun {
		private final byte[] bytes;
		private final int nextIndex;

		private HexRun(byte[] bytes, int nextIndex) {
			this.bytes = bytes;
			this.nextIndex = nextIndex;
		}
	}
}
