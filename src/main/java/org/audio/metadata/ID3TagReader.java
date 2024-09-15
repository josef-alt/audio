package org.audio.metadata;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
		tags.put("SYLT", "Synchronized lyric/text");
		tags.put("SYTC", "Synchronized tempo codes");
		tags.put("TALB", "Album/Movie/Show title");
		tags.put("TBPM", "BPM (beats per minute)");
		tags.put("TCOM", "Composer");
		tags.put("TCON", "Content type");
		tags.put("TCOP", "Copyright message");
		tags.put("TDAT", "Date");
		tags.put("TDLY", "Playlist delay");
		tags.put("TENC", "Encoded by");
		tags.put("TEXT", "Lyricist/Text writer");
		tags.put("TFLT", "File type");
		tags.put("TIME", "Time");
		tags.put("TIT1", "Content group description");
		tags.put("TIT2", "Title/songname/content description");
		tags.put("TIT3", "Subtitle/Description refinement");
		tags.put("TKEY", "Initial key");
		tags.put("TLAN", "Language(s)");
		tags.put("TLEN", "Length");
		tags.put("TMED", "Media type");
		tags.put("TOAL", "Original album/movie/show title");
		tags.put("TOFN", "Original filename");
		tags.put("TOLY", "Original lyricist(s)/text writer(s)");
		tags.put("TOPE", "Original artist(s)/performer(s)");
		tags.put("TORY", "Original release year");
		tags.put("TOWN", "File owner/licensee");
		tags.put("TPE1", "Lead performer(s)/Soloist(s)");
		tags.put("TPE2", "Band/orchestra/accompaniment");
		tags.put("TPE3", "Conductor/performer refinement");
		tags.put("TPE4", "Interpreted, remixed, or otherwise modified by");
		tags.put("TPOS", "Part of a set");
		tags.put("TPUB", "Publisher");
		tags.put("TRCK", "Track number/Position in set");
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
		tags.put("USLT", "Unsychronized lyric/text transcription");
		tags.put("WCOM", "Commercial information");
		tags.put("WCOP", "Copyright/Legal information");
		tags.put("WOAF", "Official audio file webpage");
		tags.put("WOAR", "Official artist/performer webpage");
		tags.put("WOAS", "Official audio source webpage");
		tags.put("WORS", "Official internet radio station homepage");
		tags.put("WPAY", "Payment");
		tags.put("WPUB", "Publishers official webpage");
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
	 * @param source path to audio file
	 * @return metadata in key-value pairs
	 */
	public static Map<String, String> getMetadata(Path source) {
		HashMap<String, String> metadata = new HashMap<>();

		// determine ID3 version
		if (checkHeader(source)) {
			ID3v2(source, metadata);
		}

		return Collections.unmodifiableMap(metadata);
	}

	/**
	 * Loads {@code metadata} with tags found in {@code source} based on ID3v2
	 * specifications
	 * 
	 * @param source   path to audio file
	 * @param metadata map to be populated with any found metadata
	 */
	private static void ID3v2(Path source, Map<String, String> metadata) {
		try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ)) {
			ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
			int nRead = channel.read(buffer);
			buffer.flip();

			int id3_length = getSizeFromHeader(buffer);

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

				int size = buffer.getInt();
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
					metadata.put(tag, new String(frameData, offset, length - offset));
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

		int size = 0;
		size |= (data[0] & 0x7F) << 21;
		size |= (data[1] & 0x7F) << 14;
		size |= (data[2] & 0x7F) << 7;
		size |= (data[3] & 0x7F);
		return size;
	}
}
