package org.bbottema.rtftohtml.internal;

import org.bbottema.rtftohtml.RtfImage;
import org.bbottema.rtftohtml.RtfToHtmlOptions;
import org.bbottema.rtftohtml.impl.util.CharsetHelper;
import org.bbottema.rtftohtml.impl.util.CodePage;
import org.bbottema.rtftohtml.model.RtfBinary;
import org.bbottema.rtftohtml.model.RtfControlSymbol;
import org.bbottema.rtftohtml.model.RtfControlWord;
import org.bbottema.rtftohtml.model.RtfDocument;
import org.bbottema.rtftohtml.model.RtfGroup;
import org.bbottema.rtftohtml.model.RtfHexBytes;
import org.bbottema.rtftohtml.model.RtfNode;
import org.bbottema.rtftohtml.model.RtfText;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.bbottema.rtftohtml.impl.util.ByteUtil.hexStringToByteArray;

public final class RtfToHtmlEngine {

	private final RtfToHtmlOptions options;

	public RtfToHtmlEngine(@NotNull RtfToHtmlOptions options) {
		this.options = requireNonNull(options, "options");
	}

	@NotNull
	public String renderStandard(@NotNull RtfDocument document) {
		return render(requireNonNull(document, "document"), Mode.GENERIC);
	}

	@NotNull
	public String renderOutlook(@NotNull RtfDocument document) {
		return render(requireNonNull(document, "document"), detectMode(document));
	}

	@NotNull
	private String render(@NotNull RtfDocument document, @NotNull Mode mode) {
		Conversion conversion = new Conversion(mode, document.isBytePreservingInput());
		walkDocument(document, conversion);
		return conversion.result();
	}

	private void walkDocument(@NotNull RtfDocument document, @NotNull Conversion conversion) {
		for (RtfNode child : document.getRoot().getChildren()) {
			if (child instanceof RtfGroup) {
				walkGroup((RtfGroup) child, new State(), conversion);
				return;
			}
		}
		walkGroup(document.getRoot(), new State(), conversion);
	}

	private Mode detectMode(@NotNull RtfDocument document) {
		String source = document.getSource();
		if (source.matches("(?s).*\\\\fromhtml\\d*\\b.*")) {
			return Mode.FROM_HTML;
		}
		if (source.matches("(?s).*\\\\fromtext\\b.*")) {
			return Mode.FROM_TEXT;
		}
		return Mode.GENERIC;
	}

	private void walkGroup(@NotNull RtfGroup group, @NotNull State state, @NotNull Conversion conversion) {
		for (RtfNode node : group.getChildren()) {
			if (node instanceof RtfGroup) {
				walkGroup((RtfGroup) node, state.copyForGroup(), conversion);
			} else if (node instanceof RtfControlWord) {
				handleControlWord((RtfControlWord) node, state, conversion);
			} else if (node instanceof RtfControlSymbol) {
				handleControlSymbol((RtfControlSymbol) node, state, conversion);
			} else if (node instanceof RtfHexBytes) {
				appendBytes(((RtfHexBytes) node).getBytes(), state, conversion);
			} else if (node instanceof RtfBinary) {
				appendBinary(((RtfBinary) node).getBytes(), state, conversion);
			} else if (node instanceof RtfText) {
				appendSourceText(((RtfText) node).getText(), state, conversion);
			}
		}
		onGroupEnd(state, conversion);
	}

	private void handleControlWord(@NotNull RtfControlWord control, @NotNull State state,
								   @NotNull Conversion conversion) {
		if (consumeUnicodeFallbackControl(conversion)) {
			return;
		}

		String word = control.getName();
		Integer parameter = control.getParameter();
		if (setDestination(word, state)) {
			return;
		}

		if (state.pendingIgnorableDestination) {
			state.destination = Destination.SKIP;
			state.pendingIgnorableDestination = false;
			return;
		}

		if (state.destination == Destination.SKIP || state.destination == Destination.PN_TEXT) {
			return;
		}

		if ("ansicpg".equals(word) && parameter != null) {
			conversion.defaultCharset = findCharsetForCodePage(parameter, conversion.defaultCharset);
			return;
		}

		if (state.destination == Destination.FONT_TABLE) {
			handleFontTableControl(word, parameter, state, conversion);
			return;
		}

		if (state.destination == Destination.COLOR_TABLE) {
			return;
		}

		if (state.destination == Destination.PICT) {
			handlePictControl(word, parameter, state);
			return;
		}

		if ("uc".equals(word)) {
			state.unicodeFallbackLength = parameter == null ? 1 : Math.max(0, parameter);
			return;
		}

		if ("u".equals(word) && parameter != null) {
			appendUnicode(parameter, state, conversion);
			return;
		}

		if ("f".equals(word) && parameter != null) {
			state.currentFont = parameter;
			return;
		}

		if ("htmlrtf".equals(word)) {
			state.htmlRtf = parameter == null || parameter != 0;
			return;
		}

		if (state.destination == Destination.HTML_TAG) {
			handleHtmlTagControl(word, conversion);
			return;
		}

		if ("par".equals(word)) {
			appendParagraphBreak(state, conversion);
		} else if ("line".equals(word)) {
			appendLineBreak(state, conversion);
		} else if ("tab".equals(word)) {
			appendLiteralText("\t", state, conversion);
		} else {
			handleGenericFormatting(word, parameter, state);
		}
	}

	private boolean setDestination(@NotNull String word, @NotNull State state) {
		if ("fonttbl".equals(word)) {
			state.destination = Destination.FONT_TABLE;
			state.pendingIgnorableDestination = false;
			return true;
		}
		if ("colortbl".equals(word)) {
			state.destination = Destination.COLOR_TABLE;
			state.pendingIgnorableDestination = false;
			return true;
		}
		if (word.startsWith("htmltag")) {
			state.destination = Destination.HTML_TAG;
			state.pendingIgnorableDestination = false;
			return true;
		}
		if ("pntext".equals(word)) {
			state.destination = Destination.PN_TEXT;
			state.pendingIgnorableDestination = false;
			return true;
		}
		if ("pict".equals(word)) {
			state.destination = Destination.PICT;
			state.pictBuilder = new PictBuilder();
			state.ownsPictBuilder = true;
			state.pendingIgnorableDestination = false;
			return true;
		}
		if (isKnownSkippedDestination(word)) {
			state.destination = Destination.SKIP;
			state.pendingIgnorableDestination = false;
			return true;
		}
		return false;
	}

	private boolean isKnownSkippedDestination(@NotNull String word) {
		return "stylesheet".equals(word)
				|| "info".equals(word)
				|| "generator".equals(word)
				|| "formatConverter".equals(word)
				|| "listtable".equals(word)
				|| "listoverridetable".equals(word)
				|| "revtbl".equals(word)
				|| "xmlnstbl".equals(word)
				|| "themedata".equals(word)
				|| "colorschememapping".equals(word)
				|| "datastore".equals(word);
	}

	private void handleControlSymbol(@NotNull RtfControlSymbol control, @NotNull State state,
									 @NotNull Conversion conversion) {
		if (consumeUnicodeFallbackControl(conversion)) {
			return;
		}

		char symbol = control.getSymbol();
		if (symbol == '*') {
			state.pendingIgnorableDestination = true;
		} else if (symbol == '{' || symbol == '}' || symbol == '\\') {
			appendLiteralText(Character.toString(symbol), state, conversion);
		} else if (symbol == '~') {
			appendLiteralText("\u00A0", state, conversion);
		} else if (symbol == '-') {
			appendLiteralText("\u00AD", state, conversion);
		} else if (symbol == '_') {
			appendLiteralText("\u2011", state, conversion);
		}
	}

	private void handleFontTableControl(@NotNull String word, Integer parameter, @NotNull State state,
										@NotNull Conversion conversion) {
		if ("f".equals(word) && parameter != null) {
			state.currentFont = parameter;
			state.fontEntry = conversion.fontTable.get(parameter);
			if (state.fontEntry == null) {
				state.fontEntry = new FontEntry();
				conversion.fontTable.put(parameter, state.fontEntry);
			}
		} else if ("fcharset".equals(word) && parameter != null && state.fontEntry != null) {
			state.fontEntry.charset = CodePage.getCharsetByCodePage(parameter);
		} else if ("cpg".equals(word) && parameter != null && state.fontEntry != null) {
			state.fontEntry.charset = findCharsetForCodePage(parameter, state.fontEntry.charset);
		}
	}

	private void handlePictControl(@NotNull String word, Integer parameter, @NotNull State state) {
		PictBuilder pict = state.pictBuilder;
		if (pict == null) {
			return;
		}

		if ("pngblip".equals(word)) {
			pict.format = "png";
		} else if ("jpegblip".equals(word)) {
			pict.format = "jpeg";
		} else if ("emfblip".equals(word)) {
			pict.format = "emf";
		} else if ("wmetafile".equals(word)) {
			pict.format = "wmf";
		} else if ("macpict".equals(word)) {
			pict.format = "pict";
		} else if ("picw".equals(word)) {
			pict.widthPixels = parameter;
		} else if ("pich".equals(word)) {
			pict.heightPixels = parameter;
		} else if ("picwgoal".equals(word)) {
			pict.widthGoalTwips = parameter;
		} else if ("pichgoal".equals(word)) {
			pict.heightGoalTwips = parameter;
		}
	}

	private void handleHtmlTagControl(@NotNull String word, @NotNull Conversion conversion) {
		if ("par".equals(word) || "line".equals(word)) {
			conversion.html.append('\n');
		} else if ("tab".equals(word)) {
			conversion.html.append('\t');
		}
	}

	private void handleGenericFormatting(@NotNull String word, Integer parameter, @NotNull State state) {
		if ("plain".equals(word)) {
			state.bold = false;
			state.italic = false;
			state.underline = false;
			state.strike = false;
			state.hidden = false;
			state.fontSizeHalfPoints = null;
		} else if ("pard".equals(word)) {
			state.alignment = null;
		} else if ("b".equals(word)) {
			state.bold = parameter == null || parameter != 0;
		} else if ("i".equals(word)) {
			state.italic = parameter == null || parameter != 0;
		} else if ("ul".equals(word)) {
			state.underline = parameter == null || parameter != 0;
		} else if ("ulnone".equals(word)) {
			state.underline = false;
		} else if ("strike".equals(word) || "striked".equals(word)) {
			state.strike = parameter == null || parameter != 0;
		} else if ("v".equals(word)) {
			state.hidden = parameter == null || parameter != 0;
		} else if ("fs".equals(word) && parameter != null) {
			state.fontSizeHalfPoints = parameter;
		} else if ("qc".equals(word)) {
			state.alignment = "center";
		} else if ("qr".equals(word)) {
			state.alignment = "right";
		} else if ("qj".equals(word)) {
			state.alignment = "justify";
		} else if ("ql".equals(word)) {
			state.alignment = null;
		}
	}

	private void appendUnicode(int controlParameter, @NotNull State state, @NotNull Conversion conversion) {
		int codeUnit = controlParameter < 0 ? controlParameter + 65536 : controlParameter;
		appendLiteralText(Character.toString((char) codeUnit), state, conversion);
		conversion.unicodeFallbackToSkip = state.unicodeFallbackLength;
	}

	private void appendParagraphBreak(@NotNull State state, @NotNull Conversion conversion) {
		if (isNonOutputDestination(state)) {
			return;
		}
		if (conversion.mode == Mode.GENERIC) {
			conversion.generic.closeParagraph();
		} else if (!state.htmlRtf && !state.hidden) {
			conversion.text().append('\n');
		}
	}

	private void appendLineBreak(@NotNull State state, @NotNull Conversion conversion) {
		if (isNonOutputDestination(state)) {
			return;
		}
		if (conversion.mode == Mode.GENERIC) {
			conversion.generic.appendBreak(state);
		} else if (!state.htmlRtf && !state.hidden) {
			conversion.text().append('\n');
		}
	}

	private void appendBytes(@NotNull byte[] bytes, @NotNull State state, @NotNull Conversion conversion) {
		byte[] effectiveBytes = consumeUnicodeFallbackBytes(bytes, conversion);
		if (effectiveBytes.length == 0) {
			return;
		}
		if (state.destination == Destination.PICT && state.pictBuilder != null) {
			state.pictBuilder.append(effectiveBytes);
			return;
		}
		appendDecodedText(new String(effectiveBytes, charsetFor(state, conversion)), state, conversion);
	}

	private void appendBinary(@NotNull byte[] bytes, @NotNull State state, @NotNull Conversion conversion) {
		byte[] effectiveBytes = consumeUnicodeFallbackBytes(bytes, conversion);
		if (effectiveBytes.length == 0) {
			return;
		}
		if (state.destination == Destination.PICT && state.pictBuilder != null) {
			state.pictBuilder.append(effectiveBytes);
		}
	}

	private void appendSourceText(@NotNull String text, @NotNull State state, @NotNull Conversion conversion) {
		appendText(text, state, conversion, true, true);
	}

	private void appendDecodedText(@NotNull String text, @NotNull State state, @NotNull Conversion conversion) {
		appendText(text, state, conversion, false, false);
	}

	private void appendLiteralText(@NotNull String text, @NotNull State state, @NotNull Conversion conversion) {
		appendText(text, state, conversion, false, false);
	}

	private void appendText(@NotNull String text, @NotNull State state, @NotNull Conversion conversion,
							boolean consumeFallback, boolean decodeBytePreservingInput) {
		if (text.isEmpty()) {
			return;
		}

		String effectiveSourceText = consumeFallback ? consumeUnicodeFallbackText(text, conversion) : text;
		if (effectiveSourceText.isEmpty()) {
			return;
		}

		if (state.destination == Destination.PICT && state.pictBuilder != null) {
			state.pictBuilder.appendHexText(effectiveSourceText);
			return;
		}

		if (state.destination == Destination.FONT_TABLE) {
			if (state.fontEntry != null) {
				state.fontEntry.name.append(effectiveSourceText);
			}
			return;
		}

		if (isNonOutputDestination(state)) {
			return;
		}

		String effectiveText = decodeBytePreservingInput && conversion.bytePreservingInput
				? decodeBytePreservingText(effectiveSourceText, charsetFor(state, conversion))
				: effectiveSourceText;

		if (state.destination == Destination.HTML_TAG) {
			conversion.html.append(effectiveText);
		} else if (state.htmlRtf || state.hidden) {
			return;
		} else if (conversion.mode == Mode.GENERIC) {
			conversion.generic.appendText(effectiveText, state);
		} else {
			conversion.text().append(effectiveText);
		}
	}

	private boolean isNonOutputDestination(@NotNull State state) {
		return state.destination == Destination.FONT_TABLE
				|| state.destination == Destination.COLOR_TABLE
				|| state.destination == Destination.PN_TEXT
				|| state.destination == Destination.SKIP;
	}

	private boolean consumeUnicodeFallbackControl(@NotNull Conversion conversion) {
		if (conversion.unicodeFallbackToSkip == 0) {
			return false;
		}
		conversion.unicodeFallbackToSkip--;
		return true;
	}

	@NotNull
	private String consumeUnicodeFallbackText(@NotNull String text, @NotNull Conversion conversion) {
		if (conversion.unicodeFallbackToSkip == 0) {
			return text;
		}
		int charsToSkip = Math.min(text.length(), conversion.unicodeFallbackToSkip);
		conversion.unicodeFallbackToSkip -= charsToSkip;
		return text.substring(charsToSkip);
	}

	@NotNull
	private byte[] consumeUnicodeFallbackBytes(@NotNull byte[] bytes, @NotNull Conversion conversion) {
		if (conversion.unicodeFallbackToSkip == 0) {
			return bytes;
		}
		int bytesToSkip = Math.min(bytes.length, conversion.unicodeFallbackToSkip);
		conversion.unicodeFallbackToSkip -= bytesToSkip;
		return Arrays.copyOfRange(bytes, bytesToSkip, bytes.length);
	}

	private String decodeBytePreservingText(@NotNull String text, @NotNull Charset charset) {
		byte[] bytes = new byte[text.length()];
		for (int i = 0; i < text.length(); i++) {
			bytes[i] = (byte) text.charAt(i);
		}
		return new String(bytes, charset);
	}

	private Charset charsetFor(@NotNull State state, @NotNull Conversion conversion) {
		if (state.currentFont != null) {
			FontEntry fontEntry = conversion.fontTable.get(state.currentFont);
			if (fontEntry != null && fontEntry.charset != null) {
				return fontEntry.charset;
			}
		}
		return conversion.defaultCharset;
	}

	private Charset findCharsetForCodePage(int codePage, Charset fallback) {
		Charset charset = CodePage.getCharsetByCodePage(codePage);
		if (charset != null) {
			return charset;
		}
		try {
			return CharsetHelper.findCharsetForCodePage(Integer.toString(codePage));
		} catch (UnsupportedCharsetException e) {
			return fallback == null ? StandardCharsets.ISO_8859_1 : fallback;
		}
	}

	private void onGroupEnd(@NotNull State state, @NotNull Conversion conversion) {
		if (state.ownsPictBuilder && state.pictBuilder != null && !state.htmlRtf && !state.hidden) {
			RtfImage image = state.pictBuilder.toImage();
			String src = options.getImageHandler().resolveImage(image);
			if (src != null && !src.isEmpty()) {
				if (conversion.mode == Mode.GENERIC) {
					conversion.generic.appendImage(src, image, state);
				} else if (conversion.mode == Mode.FROM_HTML) {
					conversion.html.append("<img src=\"").append(escapeAttribute(src)).append("\">");
				}
			}
		}
	}

	private String escapeHtml(@NotNull String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private String escapeAttribute(@NotNull String text) {
		return escapeHtml(text).replace("\"", "&quot;");
	}

	private enum Mode {
		FROM_HTML,
		FROM_TEXT,
		GENERIC
	}

	private enum Destination {
		NORMAL,
		FONT_TABLE,
		COLOR_TABLE,
		HTML_TAG,
		PN_TEXT,
		PICT,
		SKIP
	}

	private final class Conversion {
		private final Mode mode;
		private final boolean bytePreservingInput;
		private final Map<Integer, FontEntry> fontTable = new HashMap<>();
		private final StringBuilder html = new StringBuilder();
		private final StringBuilder plainText = new StringBuilder();
		private final GenericHtmlBuilder generic = new GenericHtmlBuilder();
		private Charset defaultCharset = CodePage.WINDOWS_1252.getCharset();
		private int unicodeFallbackToSkip;

		private Conversion(@NotNull Mode mode, boolean bytePreservingInput) {
			this.mode = mode;
			this.bytePreservingInput = bytePreservingInput;
		}

		private StringBuilder text() {
			return mode == Mode.FROM_HTML ? html : plainText;
		}

		private String result() {
			if (mode == Mode.FROM_HTML) {
				return html.toString();
			}
			if (mode == Mode.FROM_TEXT) {
				return "<html><body><pre style=\"white-space:pre-wrap\">"
						+ escapeHtml(plainText.toString())
						+ "</pre></body></html>";
			}
			return generic.result();
		}
	}

	private static final class State {
		private Destination destination = Destination.NORMAL;
		private boolean pendingIgnorableDestination;
		private boolean htmlRtf;
		private boolean hidden;
		private int unicodeFallbackLength = 1;
		private Integer currentFont;
		private FontEntry fontEntry;
		private PictBuilder pictBuilder;
		private boolean ownsPictBuilder;
		private boolean bold;
		private boolean italic;
		private boolean underline;
		private boolean strike;
		private Integer fontSizeHalfPoints;
		private String alignment;

		private State copyForGroup() {
			State copy = new State();
			copy.destination = destination;
			copy.htmlRtf = htmlRtf;
			copy.hidden = hidden;
			copy.unicodeFallbackLength = unicodeFallbackLength;
			copy.currentFont = currentFont;
			copy.fontEntry = fontEntry;
			copy.pictBuilder = pictBuilder;
			copy.bold = bold;
			copy.italic = italic;
			copy.underline = underline;
			copy.strike = strike;
			copy.fontSizeHalfPoints = fontSizeHalfPoints;
			copy.alignment = alignment;
			return copy;
		}
	}

	private static final class FontEntry {
		private Charset charset;
		private final StringBuilder name = new StringBuilder();
	}

	private static final class PictBuilder {
		private String format;
		private final StringBuilder hex = new StringBuilder();
		private Integer widthPixels;
		private Integer heightPixels;
		private Integer widthGoalTwips;
		private Integer heightGoalTwips;

		private void append(@NotNull byte[] bytes) {
			for (byte b : bytes) {
				hex.append(String.format("%02x", b & 0xff));
			}
		}

		private void appendHexText(@NotNull String text) {
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F') {
					hex.append(c);
				}
			}
		}

		private RtfImage toImage() {
			String evenHex = hex.length() % 2 == 0 ? hex.toString() : hex.substring(0, hex.length() - 1);
			byte[] bytes = evenHex.isEmpty() ? new byte[0] : hexStringToByteArray(evenHex);
			return new RtfImage(format, bytes, widthPixels, heightPixels, widthGoalTwips, heightGoalTwips);
		}
	}

	private final class GenericHtmlBuilder {
		private final StringBuilder body = new StringBuilder();
		private boolean paragraphOpen;

		private void appendText(@NotNull String text, @NotNull State state) {
			if (text.isEmpty()) {
				return;
			}
			ensureParagraph(state);
			body.append(applyCharacterFormatting(escapeHtml(text), state));
		}

		private void appendBreak(@NotNull State state) {
			ensureParagraph(state);
			body.append("<br>");
		}

		private void appendImage(@NotNull String src, @NotNull RtfImage image, @NotNull State state) {
			ensureParagraph(state);
			body.append("<img src=\"").append(escapeAttribute(src)).append("\"");
			if (image.getWidthPixels() != null) {
				body.append(" width=\"").append(image.getWidthPixels()).append("\"");
			}
			if (image.getHeightPixels() != null) {
				body.append(" height=\"").append(image.getHeightPixels()).append("\"");
			}
			body.append(">");
		}

		private void closeParagraph() {
			if (paragraphOpen) {
				body.append("</p>");
				paragraphOpen = false;
			} else {
				body.append("<p></p>");
			}
		}

		private String result() {
			if (paragraphOpen) {
				closeParagraph();
			}
			return "<html><body>" + body + "</body></html>";
		}

		private void ensureParagraph(@NotNull State state) {
			if (paragraphOpen) {
				return;
			}
			body.append("<p");
			if (state.alignment != null) {
				body.append(" style=\"text-align:").append(state.alignment).append("\"");
			}
			body.append(">");
			paragraphOpen = true;
		}

		private String applyCharacterFormatting(@NotNull String escapedText, @NotNull State state) {
			String formatted = escapedText;
			String style = characterStyle(state);
			if (!style.isEmpty()) {
				formatted = "<span style=\"" + style + "\">" + formatted + "</span>";
			}
			if (state.bold) {
				formatted = "<strong>" + formatted + "</strong>";
			}
			if (state.italic) {
				formatted = "<em>" + formatted + "</em>";
			}
			return formatted;
		}

		private String characterStyle(@NotNull State state) {
			StringBuilder style = new StringBuilder();
			if (state.underline || state.strike) {
				style.append("text-decoration:");
				if (state.underline) {
					style.append("underline");
				}
				if (state.underline && state.strike) {
					style.append(' ');
				}
				if (state.strike) {
					style.append("line-through");
				}
				style.append(';');
			}
			if (state.fontSizeHalfPoints != null) {
				style.append("font-size:").append(formatHalfPoints(state.fontSizeHalfPoints)).append("pt;");
			}
			return style.toString();
		}

		private String formatHalfPoints(int halfPoints) {
			if (halfPoints % 2 == 0) {
				return Integer.toString(halfPoints / 2);
			}
			return (halfPoints / 2) + ".5";
		}
	}
}
