package com.planwith.planwith_fo_member.application.member;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;

final class ProfileImageProcessor {

	static final long MAX_BYTES = 5L * 1024 * 1024;
	static final int MIN_STORED_PX = 512;
	static final int MAX_STORED_PX = 1024;

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"image/jpeg",
			"image/jpg",
			"image/png",
			"image/webp"
	);

	record Processed(byte[] bytes, String contentType) {
	}

	private ProfileImageProcessor() {
	}

	static Processed process(String rawContentType, byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE, "이미지 파일이 필요합니다.");
		}
		if (bytes.length > MAX_BYTES) {
			throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE, "이미지 용량은 5MB 이하여야 합니다.");
		}
		String contentType = normalizeContentType(rawContentType);
		if (contentType == null) {
			throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE, "JPG, JPEG, PNG, WebP만 업로드할 수 있습니다.");
		}
		if (!hasMagicBytes(contentType, bytes)) {
			throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE, "실제 이미지 파일이 아닙니다.");
		}

		BufferedImage decoded = readImage(bytes);
		if (decoded == null) {
			if ("image/webp".equals(contentType)) {
				return new Processed(bytes, contentType);
			}
			throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE, "실제 이미지 파일이 아닙니다.");
		}

		BufferedImage square = cropToSquare(decoded);
		int storedSize = storedSize(Math.min(square.getWidth(), square.getHeight()));
		BufferedImage resized = resize(square, storedSize);
		return writeImage(resized, contentType);
	}

	static String normalizeContentType(String rawContentType) {
		if (rawContentType == null || rawContentType.isBlank()) {
			return null;
		}
		String contentType = rawContentType.toLowerCase(Locale.ROOT).split(";", 2)[0].trim();
		if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
			return null;
		}
		return storedContentType(contentType);
	}

	private static String storedContentType(String contentType) {
		return switch (contentType) {
			case "image/png" -> "image/png";
			case "image/webp" -> "image/webp";
			default -> "image/jpeg";
		};
	}

	private static boolean hasMagicBytes(String contentType, byte[] bytes) {
		return switch (contentType) {
			case "image/png" -> startsWith(bytes, (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47);
			case "image/webp" -> isWebp(bytes);
			default -> startsWith(bytes, (byte) 0xFF, (byte) 0xD8);
		};
	}

	private static boolean isWebp(byte[] bytes) {
		return startsWith(bytes, (byte) 'R', (byte) 'I', (byte) 'F', (byte) 'F')
				&& bytes.length >= 12
				&& bytes[8] == 'W'
				&& bytes[9] == 'E'
				&& bytes[10] == 'B'
				&& bytes[11] == 'P';
	}

	private static boolean startsWith(byte[] bytes, byte... prefix) {
		if (bytes.length < prefix.length) {
			return false;
		}
		for (int i = 0; i < prefix.length; i++) {
			if (bytes[i] != prefix[i]) {
				return false;
			}
		}
		return true;
	}

	private static BufferedImage readImage(byte[] bytes) {
		try {
			return ImageIO.read(new ByteArrayInputStream(bytes));
		}
		catch (IOException exception) {
			return null;
		}
	}

	private static BufferedImage cropToSquare(BufferedImage image) {
		int size = Math.min(image.getWidth(), image.getHeight());
		int x = (image.getWidth() - size) / 2;
		int y = (image.getHeight() - size) / 2;
		BufferedImage square = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = square.createGraphics();
		graphics.drawImage(image, 0, 0, size, size, x, y, x + size, y + size, null);
		graphics.dispose();
		return square;
	}

	private static int storedSize(int sourceSize) {
		return Math.min(MAX_STORED_PX, Math.max(MIN_STORED_PX, sourceSize));
	}

	private static BufferedImage resize(BufferedImage source, int size) {
		if (source.getWidth() == size && source.getHeight() == size) {
			return source;
		}
		BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = resized.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.drawImage(source, 0, 0, size, size, null);
		graphics.dispose();
		return resized;
	}

	private static Processed writeImage(BufferedImage image, String contentType) {
		String format = switch (contentType) {
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			default -> "jpeg";
		};
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			if (ImageIO.write(image, format, output) && output.size() > 0) {
				return new Processed(output.toByteArray(), storedContentType(contentType));
			}
			output.reset();
			if (!ImageIO.write(image, "jpeg", output) || output.size() == 0) {
				throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
			}
			return new Processed(output.toByteArray(), "image/jpeg");
		}
		catch (BusinessException exception) {
			throw exception;
		}
		catch (IOException exception) {
			throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
		}
	}
}
