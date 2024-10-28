package org.audio.metadata.reader;

import java.nio.file.Path;

import org.audio.metadata.Metadata;
import org.audio.utils.FileUtils;

/**
 * Reads metadata from M4A files. Supports DASH, M4A, and MP4.
 */
public class M4AReader extends MetadataReader{
	/**
	 * Reviewing many, many M4A files has led me to believe there
	 * are three distinct structures used, each will have to be
	 * handled separately.
	 */
	enum M4AType {
		/**
		 * Sample Dash Header:
		 * 00 00 00 18 66 74 79 70 64 61 73 68 00 00 00 00
		 */
		FTYPDASH,
		/**
		 * Sample M4A Header:
		 * 00 00 00 1C 66 74 79 70 4D 34 41 20 00 00 02 00
		 */
		FTYPM4A,
		/**
		 * Sample MP4 Header:
		 * 00 00 00 18 66 74 79 70 6D 70 34 32 00 00 00 00
		 */
		FTYPMP4,

		/**
		 * Used for unsupported/unrecognized files
		 */
		UNKNOWN;
	}

	/**
	 * Reads tags from given M4A files
	 * 
	 * @return metadata in key-value pairs
	 */
	public Metadata getMetadata() {
		Metadata metadata = new Metadata();
		// determine format
		switch (getM4AType(source)) {
			case FTYPDASH:
				extractDASH(metadata);
				break;
			case FTYPM4A:
				System.err.println("Unsupported M4A type (M4A)");
				break;
			case FTYPMP4:
				System.err.println("Unsupported M4A type (MP4)");
				break;
			case UNKNOWN:
				System.err.println("Unknown M4A type");
				break;
		}
		return metadata;
	}

	/**
	 * Extract metadata from DASH M4A file.
	 * 
	 * @param metadata instance to be populated with metadata from {@code source}.
	 */
	private void extractDASH(Metadata metadata) {
		// TODO: implement DASH reader
	}

	/**
	 * Determine which ftyp this file represents.
	 * 
	 * @param source path to audio file
	 * @return M4AType enum corresponding to {@code source}'s header
	 */
	private M4AType getM4AType(Path source) {
		// TODO: overload header with header size parameter
		byte[] header = FileUtils.getHeader(source);

		if (header != null) {
			// check for 'ftyp' marker
			if ((header[4] & 0xFF) != 0x66 || (header[5] & 0xFF) != 0x74 || (header[6] & 0xFF) != 0x79
					|| (header[7] & 0xFF) != 0x70) {
				return M4AType.UNKNOWN;
			}

			// check for known 'ftyp's
			if ((header[8] & 0xFF) == 0x64 && (header[9] & 0xFF) == 0x61 && (header[10] & 0xFF) == 0x73
					&& (header[11] & 0xFF) == 0x68) {
				return M4AType.FTYPDASH;
			} else if ((header[8] & 0xFF) == 0x4D && (header[9] & 0xFF) == 0x34 && (header[10] & 0xFF) == 0x41) {
				return M4AType.FTYPM4A;
			} else if ((header[8] & 0xFF) == 0x6D && (header[9] & 0xFF) == 0x70 && (header[10] & 0xFF) == 0x34) {
				return M4AType.FTYPMP4;
			}
		}
		return M4AType.UNKNOWN;
	}
}