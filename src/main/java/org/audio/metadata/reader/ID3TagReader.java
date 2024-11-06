package org.audio.metadata.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.audio.metadata.Constants;
import org.audio.metadata.Metadata;
import org.audio.utils.FileUtils;
import org.audio.utils.FileUtils.Format;
import org.audio.utils.ImageExtractor;

/**
 * Read metadata from audio files according to ID3v2 specifications.
 */
public class ID3TagReader extends MetadataReader {

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
		tags.put("TALB", Constants.ALBUM_NAME);
		tags.put("TBPM", "BPM");
		tags.put("TCOM", Constants.COMPOSER);
		tags.put("TCON", Constants.GENRE);
		tags.put("TCOP", Constants.COPYRIGHT);
		tags.put("TDAT", Constants.DATE);
		tags.put("TDLY", "Playlist delay");
		tags.put("TENC", "Encoded by");
		tags.put("TEXT", "Lyricist");
		tags.put("TFLT", "File type");
		tags.put("TIME", "Time");
		tags.put("TIT1", "Content group description");
		tags.put("TIT2", Constants.TITLE);
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
		tags.put("TPE1", Constants.ARTIST_NAME);
		tags.put("TPE2", Constants.ACCOMPANIMENT);
		tags.put("TPE3", Constants.CONDUCTOR);
		tags.put("TPE4", "Modified by");
		tags.put("TPOS", "Part of a set");
		tags.put("TPUB", Constants.PUBLISHER);
		tags.put("TRCK", Constants.TRACK_NUMBER);
		tags.put("TRDA", "Recording dates");
		tags.put("TRSN", "Internet radio station name");
		tags.put("TRSO", "Internet radio station owner");
		tags.put("TSIZ", "Size");
		tags.put("TSRC", Constants.ISRC);
		tags.put("TSSE", Constants.ENCODING_INFO);
		tags.put("TYER", Constants.YEAR);
		tags.put("TXXX", "User defined text information frame");
		tags.put("UFID", "Unique file identifier");
		tags.put("USER", "Terms of use");
		tags.put("USLT", "Unsychronized lyric");
		tags.put("WCOM", "Commercial information");
		tags.put("WCOP", "Copyright/Legal information");
		tags.put("WOAF", Constants.FILE_WEBPAGE);
		tags.put("WOAR", Constants.ARTIST_WEBPAGE);
		tags.put("WOAS", "Official audio source webpage");
		tags.put("WORS", "Official internet radio station homepage");
		tags.put("WPAY", "Payment");
		tags.put("WPUB", Constants.PUBLISHER_WEBPAGE);
		tags.put("WXXX", "User defined URL link frame");
		ID3_TAGS = Collections.unmodifiableMap(tags);
	}

	/**
	 * Standard ID3v2 has a 10 byte header
	 * Additional 6-10 bytes optional
	 */
	private static final int HEADER_SIZE = 10;

	/**
	 * Reads ID3 tags from given MP3 files
	 * 
	 * @return metadata in key-value pairs
	 */
	public Metadata getMetadata() {
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
	private void ID3v2(Path source, Metadata metadata) {
		try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ)) {
			extractID3v2Data(channel, metadata);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Loads {@code metadata} with tags found in {@code source} based on ID3v2.
	 * Logic extracted from above ID3v2
	 * 
	 * @param channel mp3 file channel
	 * @param metadata    instance to be populated with data
	 */
	public static void extractID3v2Data(FileChannel channel, Metadata metadata) {
		try {
			ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
			int nRead = channel.read(buffer);
			buffer.flip();

			int id3_version = buffer.get(3);
			int id3_revision = buffer.get(4);
			int id3_flags = buffer.get(5);
			int id3_length = getSizeFromHeader(buffer);

			boolean extendedHeader = (id3_flags & (1 << 6)) != 0;
			if (extendedHeader) {
				// the extended header does not provide much useful information
				nRead = channel.read(buffer);
				buffer.flip();

				int ext_size = buffer.getInt();
				int ext_flags = buffer.getChar();
				int ext_padding = buffer.getInt();
			}

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

					// TODO: ID3 allows multiple PRIV tags but this will only show the last one
					// TODO: COMR commercial frame allows image/png and image/jpeg
					if (tag.equals(ID3_TAGS.get("APIC"))) {
						metadata.addImage(ImageExtractor.extractImage(frameData));
					} else {
						String value = new String(frameData);
						// character encoding
						if (frameData.length > 0) {
							switch (frameData[0]) {
								case 0:
									// ISO 8859 1
									value = new String(frameData, 1, frameData.length - 2);
									break;
								case 1:
									// UTF 16 BOM
									// 0xFFFE = little endian
									// 0xFEFF = big endian
									boolean bigEndian = (frameData[1] & 0xFF) == 0xFE && (frameData[2] & 0xFF) == 0xFF;
									if (bigEndian) {
										// skip encoding flag and two byte BOM, cut off null terminator
										value = new String(frameData, 3, size - 5, StandardCharsets.UTF_16BE);
									} else {
										// skip encoding flag and two byte BOM, cut off null terminator
										value = new String(frameData, 3, size - 5, StandardCharsets.UTF_16LE);
									}
									break;
								case 2:
									// UTF 16 without BOM
									// given StandardCharsets.UTF_16's behavior, I could just combine cases 1 & 2
									// for now I will leave them separate for clarity

									// skip encoding flag, cut off null terminator
									value = new String(frameData, 1, size - 3, StandardCharsets.UTF_16);
									break;
								case 3:
									// UTF 8
									value = new String(frameData, 1, frameData.length - 2, StandardCharsets.UTF_8);
									break;
							}
						}

						metadata.addTextField(tag, value);
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
	private boolean checkHeader(Path src) {
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
	 * @param bytes   four byte array representing one integer
	 * @param dropMSB true if the first bit of each byte is to be ignored
	 * @return the 32 bit integer represented by {@code bytes}
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
}
