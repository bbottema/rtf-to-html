package org.bbottema.rtftohtml;

import org.bbottema.rtftohtml.model.RtfBinary;
import org.bbottema.rtftohtml.model.RtfControlSymbol;
import org.bbottema.rtftohtml.model.RtfControlWord;
import org.bbottema.rtftohtml.model.RtfDocument;
import org.bbottema.rtftohtml.model.RtfGroup;
import org.bbottema.rtftohtml.model.RtfHexBytes;
import org.bbottema.rtftohtml.model.RtfNode;
import org.bbottema.rtftohtml.model.RtfText;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RtfParserTest {

	@Test
	public void testParsesGroupsControlsTextHexAndBinaryPayloads() {
		RtfDocument document = new RtfParser().parse("{\\rtf1\\ansi text{\\b bold} \\'e9\\bin3 ABC}");

		RtfGroup rtfRoot = (RtfGroup) document.getRoot().getChildren().get(0);

		assertThat(((RtfControlWord) rtfRoot.getChildren().get(0)).getName()).isEqualTo("rtf");
		assertThat(((RtfControlWord) rtfRoot.getChildren().get(0)).getParameter()).isEqualTo(1);
		assertThat(((RtfText) rtfRoot.getChildren().get(2)).getText()).isEqualTo("text");
		assertThat(((RtfGroup) rtfRoot.getChildren().get(3)).getChildren()).hasSize(2);
		assertThat(((RtfHexBytes) rtfRoot.getChildren().get(5)).getBytes()).containsExactly((byte) 0xe9);
		assertThat(((RtfBinary) rtfRoot.getChildren().get(6)).getBytes()).containsExactly((byte) 'A', (byte) 'B', (byte) 'C');
	}

	@Test
	public void testTracksGroupSourceOffsets() {
		RtfDocument document = new RtfParser().parse("x{\\rtf1 {nested}}y");

		RtfGroup rtfRoot = (RtfGroup) document.getRoot().getChildren().get(1);
		RtfGroup nested = (RtfGroup) rtfRoot.getChildren().get(1);

		assertThat(rtfRoot.getPosition().getStartOffset()).isEqualTo(1);
		assertThat(rtfRoot.getPosition().getEndOffset()).isEqualTo(17);
		assertThat(nested.getPosition().getStartOffset()).isEqualTo(8);
		assertThat(nested.getPosition().getEndOffset()).isEqualTo(16);
	}

	@Test
	public void testParsesControlSymbolsAndSignedControlParameters() {
		RtfDocument document = new RtfParser().parse("{\\rtf1 escaped \\{\\}\\~\\-\\_\\\\ unicode \\u-10179?}");
		RtfGroup rtfRoot = (RtfGroup) document.getRoot().getChildren().get(0);

		assertThat(((RtfControlSymbol) rtfRoot.getChildren().get(2)).getSymbol()).isEqualTo('{');
		assertThat(((RtfControlSymbol) rtfRoot.getChildren().get(3)).getSymbol()).isEqualTo('}');
		assertThat(((RtfControlSymbol) rtfRoot.getChildren().get(4)).getSymbol()).isEqualTo('~');
		assertThat(((RtfControlSymbol) rtfRoot.getChildren().get(5)).getSymbol()).isEqualTo('-');
		assertThat(((RtfControlSymbol) rtfRoot.getChildren().get(6)).getSymbol()).isEqualTo('_');
		assertThat(((RtfControlSymbol) rtfRoot.getChildren().get(7)).getSymbol()).isEqualTo('\\');
		assertThat(((RtfControlWord) rtfRoot.getChildren().get(9)).getName()).isEqualTo("u");
		assertThat(((RtfControlWord) rtfRoot.getChildren().get(9)).getParameter()).isEqualTo(-10179);
	}

	@Test
	public void testParsesInputStreamsThroughBytePreservingMode() {
		byte[] rtfBytes = "{\\rtf1\\ansi raw ".getBytes(StandardCharsets.ISO_8859_1);
		byte[] fullBytes = new byte[rtfBytes.length + 2];
		System.arraycopy(rtfBytes, 0, fullBytes, 0, rtfBytes.length);
		fullBytes[rtfBytes.length] = (byte) 0xe9;
		fullBytes[rtfBytes.length + 1] = (byte) '}';

		RtfDocument document = new RtfParser().parse(new ByteArrayInputStream(fullBytes));
		RtfGroup rtfRoot = (RtfGroup) document.getRoot().getChildren().get(0);

		assertThat(document.isBytePreservingInput()).isTrue();
		assertThat(((RtfText) rtfRoot.getChildren().get(2)).getText()).isEqualTo("raw é");
	}

	@Test
	public void testSourceTextOutsideTheRtfGroupRemainsInspectable() {
		RtfDocument document = new RtfParser().parse("prefix{\\rtf1 body}suffix");

		List<RtfNode> rootChildren = document.getRoot().getChildren();

		assertThat(((RtfText) rootChildren.get(0)).getText()).isEqualTo("prefix");
		assertThat(((RtfGroup) rootChildren.get(1)).getChildren()).hasSize(2);
		assertThat(((RtfText) rootChildren.get(2)).getText()).isEqualTo("suffix");
	}

	@Test
	public void testUnclosedGroupsKeepOpenEndedPosition() {
		RtfDocument document = new RtfParser().parse("{\\rtf1 {unclosed}");
		RtfGroup rtfRoot = (RtfGroup) document.getRoot().getChildren().get(0);
		RtfGroup unclosed = (RtfGroup) rtfRoot.getChildren().get(1);

		assertThat(rtfRoot.getPosition().getEndOffset()).isEqualTo(-1);
		assertThat(unclosed.getPosition().getEndOffset()).isEqualTo(17);
	}

	@Test
	public void testGroupsExposeImmutableChildren() {
		RtfDocument document = new RtfParser().parse("{\\rtf1 body}");
		RtfGroup rtfRoot = (RtfGroup) document.getRoot().getChildren().get(0);

		assertThatThrownBy(() -> rtfRoot.getChildren().clear())
				.isInstanceOf(UnsupportedOperationException.class);
	}
}
