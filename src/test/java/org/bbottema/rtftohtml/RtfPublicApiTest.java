package org.bbottema.rtftohtml;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RtfPublicApiTest {

	@Test
	public void testDefaultOptionsSkipImages() {
		assertThat(RtfToHtmlOptions.defaults().getImageHandler()
				.resolveImage(new RtfImage("png", new byte[] { 1 }, null, null, null, null)))
				.isNull();
	}

	@Test
	public void testOptionsRejectNullImageHandler() {
		assertThatThrownBy(() -> RtfToHtmlOptions.builder().imageHandler(null))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	public void testRtfImageDefensivelyCopiesInputBytes() {
		byte[] bytes = new byte[] { 1, 2, 3 };
		RtfImage image = new RtfImage("png", bytes, 10, 20, 30, 40);
		bytes[0] = 9;

		byte[] returned = image.getBytes();
		returned[1] = 9;

		assertThat(image.getBytes()).containsExactly((byte) 1, (byte) 2, (byte) 3);
		assertThat(image.getFormat()).isEqualTo("png");
		assertThat(image.getWidthPixels()).isEqualTo(10);
		assertThat(image.getHeightPixels()).isEqualTo(20);
		assertThat(image.getWidthGoalTwips()).isEqualTo(30);
		assertThat(image.getHeightGoalTwips()).isEqualTo(40);
	}

	@Test
	public void testConvertersRejectNullInput() {
		assertThatThrownBy(() -> StandardRtfToHtmlConverter.INSTANCE.toHtml((String) null))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> StandardRtfToHtmlConverter.INSTANCE.toHtml((byte[]) null))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> OutlookRtfToHtmlConverter.INSTANCE.toHtml((String) null))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> OutlookRtfToHtmlConverter.INSTANCE.toHtml((byte[]) null))
				.isInstanceOf(NullPointerException.class);
	}
}
