package org.audio.utils;

import java.util.Arrays;

import org.audio.metadata.CoverArt;

/**
 * Common logic for extracting cover art from byte arrays.
 */
public class ImageExtractor {

	/**
	 * Extract embedded images from byte array. Supports JPEG, JFIF, PNG, WEBP.
	 * 
	 * @param data full metadata frame containing header, mime type, and image data
	 * @return CoverArt instance corresponding to given array
	 */
	public static CoverArt extractImage(byte[] data) {
		String mimeType = "image/";
		String subType = "";

		int imageStart = 0;
		int imageEnd = data.length;
		for (int idx = 0; idx < data.length; ++idx) {
			if (prefixMatches(data, idx, MIME_IMAGE_PNG)) {
				subType = "png";
			} else if (prefixMatches(data, idx, MIME_IMAGE_JPEG)) {
				// skip 'jpeg', separator, picture type, separator
				imageStart = idx + 7;
				subType = "jpeg";

				break;
			} else if (prefixMatches(data, idx, JPEG_HEADER_PREFIX)) {
				// sans image/jpeg the prefix FF D8 indicates generic JPEG
				// FF D8 FF DB
				// FF D8 FF E0 00 10 4A 46 49 46 00 01
				// FF D8 FF EE

				// FF D8 FF E1 x x 45 78 69 66 00 00
				// JPEG with Exif data

				// FF D8 FF E0
				// standard JPEG/JFIF

				// TODO: possibly handle different sub-types separately
				imageStart = 0;
				subType = "jpeg";

				break;
			} else if (prefixMatches(data, idx, MIME_IMAGE_WEBP)) {
				// skip 'webp', separator, picture type, separator
				// TODO: can probably combine any mime types that don't have additional
				// header/footer checks necessary
				imageStart = idx + 7;
				subType = "webp";

				break;
			}

			// PNG is the only mime type that has additional checks needed (so far)
			if (subType.equals("png") || subType.isEmpty()) {
				if (prefixMatches(data, idx, PNG_HEADER)) {
					imageStart = idx;
					subType = "png";
				}
				if (prefixMatches(data, idx, PNG_FOOTER)) {
					imageEnd = idx + PNG_FOOTER.length;
					break;
				}
			}
		}

		byte[] imageData = Arrays.copyOfRange(data, imageStart, imageEnd);
		return new CoverArt(mimeType + subType, imageData);
	}

	/**
	 * MIME type image/png. Some formats allow leaving off 'image/', so I am only
	 * looking for the sub-type.
	 */
	private static final byte[] MIME_IMAGE_PNG = "png".getBytes();

	/**
	 * 8 byte header found at the start of PNG image data.
	 */
	private static final byte[] PNG_HEADER = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

	/**
	 * 8 byte footer found at the end of PNG image data.
	 */
	private static final byte[] PNG_FOOTER = { 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82 };

	/**
	 * MIME type for JPEG
	 */
	private static final byte[] MIME_IMAGE_JPEG = "jpeg".getBytes();

	/**
	 * 3 byte common prefix for various types of JPEG headers
	 */
	private static final byte[] JPEG_HEADER_PREFIX = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };

	/**
	 * MIME type for WEBP
	 */
	private static final byte[] MIME_IMAGE_WEBP = "webp".getBytes();

	/**
	 * Determine if {@code} query matches {@code data} at {@code index}
	 * 
	 * @param data  search space
	 * @param index start index
	 * @param query search term
	 * @return true if {@code query} is found at {@code index}
	 */
	private static boolean prefixMatches(byte[] data, int index, byte[] query) {
		int offset = 0;

		for (; offset < query.length; offset++) {
			if (data[index + offset] != query[offset]) {
				break;
			}
		}

		return offset == query.length;
	}
}
