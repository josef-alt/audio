package org.audio.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Utils for later use by my audio library
 */
public class FileUtils {

	/**
	 * Represent the most common audio formats
	 */
	public enum Format {
		// TODO other common formats
		MP3, MP4, M4A, DASH, WAV, WMA, FLAC, OGG, UNKNOWN;
	}

	/**
	 * Header size for determining audio format.
	 * This is arbitrarily chosen and may need to be tweaked later.
	 */
	private static final int HEADER_SIZE = 32;

	/**
	 * File format according to the file extension
	 * 
	 * @param path location of audio file in question.
	 * @return {@link Format} enum representing the associated file type.
	 */
	public static Format determineFormatByName(Path path) {
		String fileName = path.getFileName().toString().toLowerCase();
		int dot = fileName.lastIndexOf('.');

		if (dot == -1) {
			return Format.UNKNOWN;
		}

		switch (fileName.substring(dot + 1)) {
			case "mp3":
				return Format.MP3;
			case "mp4":
				return Format.MP4;
			case "m4a":
				return Format.M4A;
			case "wma":
				return Format.WMA;
			default:
				return Format.UNKNOWN;
		}
	}

	/**
	 * File format according to the first few bytes
	 * 
	 * @param path location of audio file in question.
	 * @return {@link Format} enum representing the associated file type.
	 */
	public static Format determineFormatByHeader(Path path) {
		byte[] header = getHeader(path);

		// MP3
		if ((header[0] & 0xFF) == 0x49 && (header[1] & 0xFF) == 0x44 && (header[2] & 0xFF) == 0x33) {
			// Sample:
			// ID3.......TALB..

			// technically this just means the data is in id3 format, but for now we'll call
			// that mp3
			return Format.MP3;
		}
		// WAVE
		else if ((header[0] & 0xFF) == 0x52 && (header[1] & 0xFF) == 0x49 && (header[2] & 0xFF) == 0x46
				&& (header[3] & 0xFF) == 0x46) {
			// RIFF
			// Sample:
			// RIFFú.“.WAVEfmt

			if ((header[8] & 0xFF) == 0x57 && (header[9] & 0xFF) == 0x41 && (header[10] & 0xFF) == 0x56) {
				return Format.WAV;
			}
		}
		// MP4 / M4A
		else if ((header[4] & 0xFF) == 0x66 && (header[5] & 0xFF) == 0x74 && (header[6] & 0xFF) == 0x79
				&& (header[7] & 0xFF) == 0x70) {
			// ftyp
			if ((header[8] & 0xFF) == 0x4D && (header[9] & 0xFF) == 0x34 && (header[10] & 0xFF) == 0x41) {
				// Sample:
				// ....ftypM4A ....
				// M4A isomiso2..À]
				return Format.M4A;
			} else if ((header[8] & 0xFF) == 0x64 && (header[9] & 0xFF) == 0x61 && (header[10] & 0xFF) == 0x73
					&& (header[11] & 0xFF) == 0x68) {
				// Sample:
				// ....ftypdash....
				// iso6mp41...žmoov
				return Format.DASH;
			} else if ((header[8] & 0xFF) == 0x6D && (header[9] & 0xFF) == 0x70 && (header[10] & 0xFF) == 0x34) {
				// Sample:
				// ....ftypmp42....
				// mp41isom..ßAmoov
				return Format.MP4;
			}
		}
		// WMA
		else if ((header[0] & 0xFF) == 0x30 && (header[1] & 0xFF) == 0x26 && (header[2] & 0xFF) == 0xB2
				&& (header[3] & 0xFF) == 0x75 && (header[4] & 0xFF) == 0x8E && (header[5] & 0xFF) == 0x66
				&& (header[6] & 0xFF) == 0xCF && (header[7] & 0xFF) == 0x11 && (header[8] & 0xFF) == 0xA6
				&& (header[9] & 0xFF) == 0xD9 && (header[10] & 0xFF) == 0x00 && (header[11] & 0xFF) == 0xAA
				&& (header[12] & 0xFF) == 0x00 && (header[13] & 0xFF) == 0x62 && (header[14] & 0xFF) == 0xCE
				&& (header[15] & 0xFF) == 0x6C) {
			return Format.WMA;
		} else if ((header[0] & 0xFF) == 0x66 && (header[1] & 0xFF) == 0x4C && (header[2] & 0xFF) == 0x61
				&& (header[3] & 0xFF) == 0x43) {
			return Format.FLAC;
		} else if ((header[0] & 0xFF) == 0x4F && (header[1] & 0xFF) == 0x67 && (header[2] & 0xFF) == 0x67
				&& (header[3] & 0xFF) == 0x53) {
			return Format.OGG;
		}

		return Format.UNKNOWN;
	}

	/**
	 * Extract the first {@code HEADER_SIZE} bytes from the specified file
	 * 
	 * @param path the {@link Path} to the file to be read; must not be {@code null}
	 * @throws IllegalArgumentException if {@code path} is {@code null}
	 * @return byte array containing the first {@code HEADER_SIZE} bytes of
	 *         {@code path}
	 */
	public static byte[] getHeader(Path path) {
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
