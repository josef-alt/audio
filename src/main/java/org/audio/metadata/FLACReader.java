package org.audio.metadata;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FLACReader extends MetadataReader {

	private static int FLAC_HEADER_SIZE = 4;
	private static int BLOCK_HEADER_SIZE = 4;
	private static final Map<String, String> VORBIS_TAGS;
	static {
		Map<String, String> tags = new HashMap<>();

		// there is no official standard set of tags
		// below are some of the proposed tags matched to the corresponding
		// description used in ID3TagReader
		tags.put("TITLE", "Song Title");
		tags.put("ALBUM", "Album title");
		tags.put("TRACKNUMBER", "Track number");
		tags.put("ARTIST", "Lead performer(s)");
		tags.put("COPYRIGHT", "Copyright/Legal information");
		tags.put("GENRE", "Content type");
		tags.put("DATE", "Date");
		tags.put("ISRC", "international standard recording code");
		VORBIS_TAGS = Collections.unmodifiableMap(tags);
	}

	/**
	 * Reads metadata from given flac files
	 * 
	 * @param source path to audio file
	 * @return metadata in key-value pairs
	 */
	public Metadata getMetadata() {
		Metadata metadata = new Metadata();

		try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ)) {

			// 32-bit flaC stream marker
			ByteBuffer buffer = ByteBuffer.allocate(FLAC_HEADER_SIZE);
			int nRead = channel.read(buffer);
			buffer.flip();

			// stream info block
			// optional metadata blocks
			boolean lastBlock = true;
			do {
				buffer = ByteBuffer.allocate(BLOCK_HEADER_SIZE);
				nRead = channel.read(buffer);
				buffer.flip();

				int flags = buffer.get() & 0xFF;
				int lastFlag = (flags & 0xFF) >> 7;
				lastBlock = lastFlag == 1;

				int blockType = flags & 0x7F;
				int blockLength = buffer.getInt(0) & 0xFFFFFF;

				buffer = ByteBuffer.allocate(blockLength);
				channel.read(buffer);
				buffer.flip();

				if (blockType == 0) {
					// STREAMINFO
				} else if (blockType == 1) {
					// PADDING
				} else if (blockType == 2) {
					// APPLICATION
				} else if (blockType == 3) {
					// SEEKTABLE
				} else if (blockType == 4) {
					// VORBIS_COMMENT
				} else if (blockType == 5) {
					// CUESHEET
				} else if (blockType == 6) {
					// PICTURE
				}
			} while (!lastBlock);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return metadata;
	}
}
