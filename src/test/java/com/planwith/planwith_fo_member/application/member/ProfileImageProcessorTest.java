package com.planwith.planwith_fo_member.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;

class ProfileImageProcessorTest {

	@Test
	void smallPngIsStoredAtLeast512px() throws Exception {
		byte[] source = pngBytes(120, 80);
		ProfileImageProcessor.Processed processed = ProfileImageProcessor.process("image/png", source);

		assertThat(processed.contentType()).isEqualTo("image/png");
		BufferedImage stored = ImageIO.read(new ByteArrayInputStream(processed.bytes()));
		assertThat(stored.getWidth()).isEqualTo(512);
		assertThat(stored.getHeight()).isEqualTo(512);
	}

	@Test
	void oversizedFileIsRejected() {
		byte[] bytes = new byte[(int) ProfileImageProcessor.MAX_BYTES + 1];
		bytes[0] = (byte) 0xFF;
		bytes[1] = (byte) 0xD8;
		assertThatThrownBy(() -> ProfileImageProcessor.process("image/jpeg", bytes))
				.isInstanceOf(BusinessException.class)
				.extracting(exception -> ((BusinessException) exception).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_PROFILE_IMAGE);
	}

	@Test
	void gifMimeIsRejected() throws Exception {
		byte[] source = pngBytes(64, 64);
		assertThatThrownBy(() -> ProfileImageProcessor.process("image/gif", source))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("JPG, JPEG, PNG, WebP");
	}

	@Test
	void jpegMagicMismatchIsRejected() {
		assertThatThrownBy(() -> ProfileImageProcessor.process("image/jpeg", new byte[] {0x00, 0x01, 0x02, 0x03}))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("실제 이미지");
	}

	private byte[] pngBytes(int width, int height) throws Exception {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		var graphics = image.createGraphics();
		graphics.setColor(Color.BLUE);
		graphics.fillRect(0, 0, width, height);
		graphics.dispose();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "png", output);
		return output.toByteArray();
	}
}
