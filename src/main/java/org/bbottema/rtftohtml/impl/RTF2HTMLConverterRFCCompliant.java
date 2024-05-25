package org.bbottema.rtftohtml.impl;

import org.bbottema.rtftohtml.RTF2HTMLConverter;
import org.bbottema.rtftohtml.impl.util.CharsetHelper;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.bbottema.rtftohtml.impl.util.ByteUtil.hexToString;

/**
 * The last and most comprehensive converter that follows the RTF RFC and produces the most correct outcome.
 * <p>
 * <strong>Note:</strong> unlike {@link RTF2HTMLConverterClassic}, this converter doesn't wrap the result in
 * basic HTML tags if they're not already present in the RTF source.
 * <p>
 * The resulting source and rendered result is on par with software such as Outlook.
 */
public class RTF2HTMLConverterRFCCompliant implements RTF2HTMLConverter {

    public static final RTF2HTMLConverter INSTANCE = new RTF2HTMLConverterRFCCompliant();

    private static final Pattern CONTROL_WORD = Pattern.compile("\\\\(([^a-zA-Z])|(([a-zA-Z]+)(-?[\\d]*) ?))");
    private static final Pattern ENCODED_CHARACTER = Pattern.compile("\\\\'([0-9a-fA-F]{2})");

    private RTF2HTMLConverterRFCCompliant() {}

    @NotNull
    public String rtf2html(@NotNull String rtf) {
        Map<Integer, FontTableEntry> fontTable = new HashMap<>();
        Charset charset = CharsetHelper.detectCharsetFromRtfContent(rtf);

        // RTF processing requires stack holding current settings, each group adds new settings to stack
        LinkedList<Group> groupStack = new LinkedList<>();
        groupStack.add(new Group());

        Matcher controlWordMatcher = CONTROL_WORD.matcher(rtf);
        Matcher encodedCharMatcher = ENCODED_CHARACTER.matcher(rtf);
        StringBuilder result = new StringBuilder();
        int length = rtf.length();
        int charIndex = 0;

        while (charIndex < length) {
            char c = rtf.charAt(charIndex);
            Group currentGroup = groupStack.getFirst();
            if (c == '\r' || c == '\n') {
                charIndex++;
            } else if (c == '{') {  //entering group
                groupStack.addFirst(currentGroup.copy());
                charIndex++;
            } else if (c == '}') {  //exiting group
                groupStack.removeFirst();
                //Not outputting anything after last closing brace matching opening brace.
                if (groupStack.size() == 1) {
                    break;
                }
                charIndex++;
            } else if (c == '\\') {
                // matching ansi-encoded sequences like \'f5\'93
                encodedCharMatcher.region(charIndex, length);
                if (encodedCharMatcher.lookingAt()) {
                    StringBuilder encodedSequence = new StringBuilder();
                    while (encodedCharMatcher.lookingAt()) {
                        encodedSequence.append(encodedCharMatcher.group(1));
                        charIndex += 4;
                        encodedCharMatcher.region(charIndex, length);
                    }

                    Charset effectiveCharset = charset;
                    if (currentGroup.fontTableIndex != null) {
                        FontTableEntry entry = fontTable.get(currentGroup.fontTableIndex);
                        if (entry != null && entry.charset != null) {
                            effectiveCharset = entry.charset;
                        }
                    }

                    String decoded = hexToString(encodedSequence.toString(), effectiveCharset);
                    appendIfNotIgnoredGroup(result, decoded, currentGroup);
                    continue;
                }

                // set matcher to current char position and match from it
                controlWordMatcher.region(charIndex, length);
                if (!controlWordMatcher.lookingAt()) {
                    throw new IllegalStateException("RTF file has invalid structure. Failed to match character '" +
                            c + "' at [" + charIndex + "/" + length + "] to a control symbol or word.");
                }

                //checking for control symbol or control word
                //control word can have optional number following it and the optional space as well
                Integer controlNumber = null;
                String controlWord = controlWordMatcher.group(2); // group(2) matches control symbol
                if (controlWord == null) {
                    controlWord = controlWordMatcher.group(4); // group(4) matches control word
                    String controlNumberString = controlWordMatcher.group(5);
                    if (!"".equals(controlNumberString)) {
                        controlNumber = Integer.valueOf(controlNumberString);
                    }
                }
                charIndex += controlWordMatcher.end() - controlWordMatcher.start();

                switch (controlWord) {
                    case "par":
                        appendIfNotIgnoredGroup(result, "\n", currentGroup);
                        break;
                    case "tab":
                        appendIfNotIgnoredGroup(result, "\t", currentGroup);
                        break;
                    case "htmlrtf":
                        //htmlrtf starts ignored text area, htmlrtf0 ends it
                        //Though technically this is not a group, it's easier to treat it as such to ignore everything in between
                        currentGroup.htmlRtf = controlNumber == null;
                        break;
                    case "ansicpg":
                        //charset definition is important for decoding ansi encoded values
                        charset = CharsetHelper.findCharsetForCodePage(requireNonNull(controlNumber).toString());
                        break;
                    case "fonttbl": // skipping these groups' contents - these are font and color settings
                    case "colortbl":
                        currentGroup.ignore = true;
                        break;
                    case "f":
                        // font table index. Might be a new one, or an existing one
                        currentGroup.fontTableIndex = controlNumber;
                        break;
                    case "fcharset":
                        if (controlNumber != null && currentGroup.fontTableIndex != null) {
                            Charset possibleCharset = CharsetHelper.rtfCharset(controlNumber);
                            if (possibleCharset != null) {
                                FontTableEntry entry = fontTable.get(currentGroup.fontTableIndex);
                                if (entry == null) {
                                    entry = new FontTableEntry();
                                    fontTable.put(currentGroup.fontTableIndex, entry);
                                }
                                entry.charset = possibleCharset;
                            }
                        }
                        break;
                    case "uc":
                        // This denotes a number of characters to skip after unicode symbols
                        currentGroup.unicodeCharLength = controlNumber == null ? 1 : controlNumber;
                        break;
                    case "u":
                        // Unicode symbols
                        if (controlNumber != null) {
                            char unicodeSymbol = (char) controlNumber.intValue();
                            appendIfNotIgnoredGroup(result, Character.toString(unicodeSymbol), currentGroup);
                            charIndex += currentGroup.unicodeCharLength;
                        }
                        break;
                    case "{":  // Escaped characters
                    case "}":
                    case "\\":
                        appendIfNotIgnoredGroup(result, controlWord, currentGroup);
                        break;
                    default:
                }

            } else {
                appendIfNotIgnoredGroup(result, c + "", currentGroup);
                charIndex++;
            }
        }
        return result.toString();
    }

    private void appendIfNotIgnoredGroup(StringBuilder result, String symbol, Group group) {
        if (!group.ignore && !group.htmlRtf) {
            result.append(symbol);
        }
    }

    private static class Group {
        boolean ignore = false;
        int unicodeCharLength = 1;
        boolean htmlRtf = false;
        Integer fontTableIndex = null;

        Group copy() {
            Group newGroup = new Group();
            newGroup.ignore = this.ignore;
            newGroup.unicodeCharLength = this.unicodeCharLength;
            newGroup.htmlRtf = this.htmlRtf;
            // Don't inherit fontTableIndex from parent group.
            return newGroup;
        }
    }

    private static class FontTableEntry {
        Charset charset = null;
    }
}