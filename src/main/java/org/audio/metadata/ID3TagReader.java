package org.audio.metadata;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.audio.utils.FileUtils;
import org.audio.utils.FileUtils.Format;

/**
 * Read metadata from audio files according to ID3v2 specifications.
 */
public class ID3TagReader {

	// all the officially supported id3 tags and their meanings
	private static final Map<String, String> ID3_TAGS;
	static {
		Map<String, String> tags = new HashMap<>();
		tags.put("AENC", "Audio encryption");
		tags.put("APIC", "Attached picture");
		tags.put("COMM", "Comments");
		tags.put("COMR", "Commercial frame");
		tags.put("ENCR", "Encryption method registration");
		tags.put("EQUA", "Equalization");
		tags.put("ETCO", "Event timing codes");
		tags.put("GEOB", "General encapsulated object");
		tags.put("GRID", "Group identification registration");
		tags.put("IPLS", "Involved people list");
		tags.put("LINK", "Linked information");
		tags.put("MCDI", "Music CD identifier");
		tags.put("MLLT", "MPEG location lookup table");
		tags.put("OWNE", "Ownership frame");
		tags.put("PRIV", "Private frame");
		tags.put("PCNT", "Play counter");
		tags.put("POPM", "Popularimeter");
		tags.put("POSS", "Position synchronisation frame");
		tags.put("RBUF", "Recommended buffer size");
		tags.put("RVAD", "Relative volume adjustment");
		tags.put("RVRB", "Reverb");
		tags.put("SYLT", "Synchronized lyric");
		tags.put("SYTC", "Synchronized tempo codes");
		tags.put("TALB", "Album title");
		tags.put("TBPM", "BPM");
		tags.put("TCOM", "Composer");
		tags.put("TCON", "Content type");
		tags.put("TCOP", "Copyright message");
		tags.put("TDAT", "Date");
		tags.put("TDLY", "Playlist delay");
		tags.put("TENC", "Encoded by");
		tags.put("TEXT", "Lyricist");
		tags.put("TFLT", "File type");
		tags.put("TIME", "Time");
		tags.put("TIT1", "Content group description");
		tags.put("TIT2", "Song Title");
		tags.put("TIT3", "Subtitle");
		tags.put("TKEY", "Initial key");
		tags.put("TLAN", "Language(s)");
		tags.put("TLEN", "Length");
		tags.put("TMED", "Media type");
		tags.put("TOAL", "Original album");
		tags.put("TOFN", "Original filename");
		tags.put("TOLY", "Original lyricist(s)");
		tags.put("TOPE", "Original artist(s)");
		tags.put("TORY", "Original release year");
		tags.put("TOWN", "File owner");
		tags.put("TPE1", "Lead performer(s)");
		tags.put("TPE2", "Accompaniment");
		tags.put("TPE3", "Conductor");
		tags.put("TPE4", "Modified by");
		tags.put("TPOS", "Part of a set");
		tags.put("TPUB", "Publisher");
		tags.put("TRCK", "Track number");
		tags.put("TRDA", "Recording dates");
		tags.put("TRSN", "Internet radio station name");
		tags.put("TRSO", "Internet radio station owner");
		tags.put("TSIZ", "Size");
		tags.put("TSRC", "international standard recording code");
		tags.put("TSSE", "Software/Hardware and settings used for encoding");
		tags.put("TYER", "Year");
		tags.put("TXXX", "User defined text information frame");
		tags.put("UFID", "Unique file identifier");
		tags.put("USER", "Terms of use");
		tags.put("USLT", "Unsychronized lyric");
		tags.put("WCOM", "Commercial information");
		tags.put("WCOP", "Copyright/Legal information");
		tags.put("WOAF", "Official audio file webpage");
		tags.put("WOAR", "Official artist webpage");
		tags.put("WOAS", "Official audio source webpage");
		tags.put("WORS", "Official internet radio station homepage");
		tags.put("WPAY", "Payment");
		tags.put("WPUB", "Publishers official webpage");
		tags.put("WXXX", "User defined URL link frame");
		ID3_TAGS = Collections.unmodifiableMap(tags);
	}

	// currently using png instead of image/png because mime type can be omitted
	private static final byte[] MIME_IMAGE_PNG = "png".getBytes();
	private static final byte[] PNG_HEADER = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
	private static final byte[] PNG_FOOTER = { 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82 };

	/**
	 * Standard ID3v2 has a 10 byte header
	 * Additional 6-10 bytes optional
	 */
	private static final int HEADER_SIZE = 10;

	/**
	 * Reads ID3 tags from given MP3 files
	 * 
	 * @param source path to audio file
	 * @return metadata in key-value pairs
	 */
	public static Metadata getMetadata(Path source) {
		Metadata metadata = new Metadata();

		// determine ID3 version
		if (checkHeader(source)) {
			ID3v2(source, metadata);
		}

		return metadata;
	}

	/**
	 * Loads {@code metadata} with tags found in {@code source} based on ID3v2
	 * specifications
	 * 
	 * @param source   path to audio file
	 * @param metadata map to be populated with any found metadata
	 */
	private static void ID3v2(Path source, Metadata metadata) {
		try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ)) {
			ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
			int nRead = channel.read(buffer);
			buffer.flip();

			int id3_length = getSizeFromHeader(buffer);
			int id3_version = buffer.get(3);

			// reading all frames
			// TODO break into chunks
			buffer = ByteBuffer.allocate(id3_length);
			channel.read(buffer);
			buffer.flip();

			// iterate through all frames
			// 4 byte frame ID
			// 4 byte frame size
			// 2 byte frame flags
			while (buffer.hasRemaining()) {
				byte[] frameID = new byte[4];
				buffer.get(frameID);

				// make sure to get size according to ID3 version
				byte[] frameSize = new byte[4];
				buffer.get(frameSize);
				int size = convertBytesToInt(frameSize, id3_version == 4);

				short flags = buffer.getShort();

				byte[] frameData = new byte[size];
				buffer.get(frameData);

				// stop reading once we reach the void
				if (frameID[0] == 0 && frameID[1] == 0 && frameID[2] == 0 && frameID[3] == 0) {
					break;
				} else {
					// in the event of unrecognized frame IDs, I want to present the data as-is
					// otherwise, I want to convert the frame ID to a common name, so that metadata
					// for different file types matches
					String tag = new String(frameID);
					if (ID3_TAGS.containsKey(tag)) {
						tag = ID3_TAGS.get(tag);
					}

					// eliminate preceding and succeeding zeros
					int offset = 0;
					while (offset < frameData.length && frameData[offset] == 0) {
						offset++;
					}
					int length = frameData.length;
					while (length > 0 && frameData[length - 1] == 0) {
						length--;
					}

					// TODO: ID3 allows multiple PRIV tags but this will only show the last one
					// TODO: COMR commercial frame allows image/png and image/jpeg
					if (tag.equals(ID3_TAGS.get("APIC"))) {
						metadata.addImage(extractImage(frameData));
					} else {
						metadata.addTextField(tag, new String(frameData, offset, length - offset));
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Determine if {@code path} matches ID3 format
	 * 
	 * @param src path to audio file
	 * @return true if the audio file matches ID3v2 specifications
	 */
	private static boolean checkHeader(Path src) {
		byte[] header = FileUtils.getHeader(src);
		if (header != null) {
			return FileUtils.determineFormatByHeader(src) == Format.MP3; // check ID3 tag version
		}

		return false;
	}

	/**
	 * Determine the size of the ID3 tag header
	 * 
	 * @param buffer first {@code HEADER_SIZE} bytes of source file
	 * @return number of bytes used by ID3 tags
	 */
	private static int getSizeFromHeader(ByteBuffer buffer) {
		buffer.position(6);
		byte[] data = new byte[4];
		buffer.get(data);

		// ID3 Size is 28 bits
		// 4 bytes, where the first bit is ignored
		// 0b01111111 = 0x7F
		// should be
		// byte 0 << 21, byte 1 << 14, byte 2 << 7, byte 3
		return convertBytesToInt(data, true);
	}

	/**
	 * Converts array of bytes into a 32 bit integer according with or without MSB.
	 * ID3v2.3: 32 bits
	 * ID3V2.4: 28 bits where the first bit of each octet is ignored
	 * 
	 * @param bytes
	 * @param version
	 * @return
	 */
	private static int convertBytesToInt(byte[] bytes, boolean dropMSB) {
		int size = 0;

		if (dropMSB) {
			size |= (bytes[0] & 0x7F) << 21;
			size |= (bytes[1] & 0x7F) << 14;
			size |= (bytes[2] & 0x7F) << 7;
			size |= (bytes[3] & 0x7F);
		} else {
			size |= (bytes[0] & 0xFF) << 24;
			size |= (bytes[1] & 0xFF) << 16;
			size |= (bytes[2] & 0xFF) << 8;
			size |= (bytes[3] & 0xFF);
		}

		return size;
	}

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

		for (; offset < query.length;) {
			if (data[index + offset] == query[offset]) {
				offset++;
			} else {
				break;
			}
		}

		return offset == query.length;
	}

	/**
	 * 
	 * @param data full ID3 frame containing header, mime type, and image data
	 * @return
	 */
	private static CoverArt extractImage(byte[] data) {
		// TODO: jpeg support
		String mimeType = "image/";
		String subType = "";

		int imageStart = 0;
		int imageEnd = data.length;
		for (int idx = 0; idx < data.length; ++idx) {
			if (prefixMatches(data, idx, MIME_IMAGE_PNG)) {
				subType = "png";
			}

			if (subType.equals("png")) {
				if (prefixMatches(data, idx, PNG_HEADER)) {
					imageStart = idx;
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
}
