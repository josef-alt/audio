package utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Utils for later use by my audio library
 */
public class MediaUtils {

	/**
	 * Represent the most common audio formats
	 */
	public enum Format {
		// TODO other common formats
		MP3, MP4, M4A, UNKNOWN;
	}

	/**
	 * Header size for determining audio format.
	 * This is arbitrarily chosen and may need to be tweaked later.
	 */
	private static final int HEADER_SIZE = 32;

	/**
	 * File format according to the file extension
	 */
	public static Format determineFormatByName(Path path) {
		String fileName = path.getFileName().toString().toLowerCase();
		int dot = fileName.lastIndexOf('.');

		switch (fileName.substring(dot + 1)) {
			case "mp3":
				return Format.MP3;
			case "mp4":
				return Format.MP4;
			case "m4a":
				return Format.M4A;
			default:
				return Format.UNKNOWN;
		}
	}

	/**
	 * File format according to the first few bytes
	 */
	public static Format determineFormatByHeader(Path path) {
		// TODO parse header
		return null;
	}

	/**
	 * Extract the first {@code HEADER_SIZE} bytes from the specified file
	 * 
	 * @param path the {@link Path} to the file to be read; must not be {@code null}
	 * @throws IllegalArgumentException if {@code path} is {@code null}
	 * @return byte array containing the first {@code HEADER_SIZE} bytes of {@code path}
	 */
	public byte[] getHeader(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("Path must not be null.");
		}

		byte[] header = new byte[HEADER_SIZE];
		try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
			ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
			int nRead = channel.read(buffer);

			if (nRead > 0) {
				buffer.flip();
				buffer.get(header, 0, nRead);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return header;
	}
}
