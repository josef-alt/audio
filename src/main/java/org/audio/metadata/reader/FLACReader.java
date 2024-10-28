package org.audio.metadata.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.audio.metadata.Constants;
import org.audio.metadata.CoverArt;
import org.audio.metadata.Metadata;

public class FLACReader extends MetadataReader {

	private static final int FLAC_HEADER_SIZE = 4;
	private static final int BLOCK_HEADER_SIZE = 4;
	private static final Map<String, String> VORBIS_TAGS;
	static {
		Map<String, String> tags = new HashMap<>();

		// there is no official standard set of tags
		// below are some of the proposed tags matched to the corresponding
		// description used in ID3TagReader
		tags.put("TITLE", Constants.TITLE);
		tags.put("ALBUM", Constants.ALBUM_NAME);
		tags.put("TRACKNUMBER", Constants.TRACK_NUMBER);
		tags.put("ARTIST", Constants.ARTIST_NAME);
		tags.put("COPYRIGHT", Constants.COPYRIGHT);
		tags.put("GENRE", Constants.GENRE);
		tags.put("DATE", Constants.DATE);
		tags.put("ISRC", Constants.ISRC);
		VORBIS_TAGS = Collections.unmodifiableMap(tags);
	}

	/**
	 * Reads metadata from given flac files
	 * 
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
					// steps taken directly from https://xiph.org/vorbis/doc/v-comment.html

					// vorbis uses little endian
					buffer.order(ByteOrder.LITTLE_ENDIAN);

					// 1) [vendor_length] = read an unsigned integer of 32 bits
					int vendor_length = buffer.getInt();

					// 2) [vendor_string] = read a UTF-8 vector as [vendor_length] octets
					// because vorbis uses utf-8, endianness shouldn't matter here
					byte[] vendor_string = new byte[vendor_length];
					buffer.get(vendor_string);

					// 3) [user_comment_list_length] = read an unsigned integer of 32 bits
					int user_comment_list_length = buffer.getInt();

					// 4) iterate [user_comment_list_length] times {
					for (int c = 0; c < user_comment_list_length; ++c) {
						// 5) [length] = read an unsigned integer of 32 bits
						int length = buffer.getInt();

						// 6) this iteration's user comment = read a UTF-8 vector as [length] octets
						byte[] comment = new byte[length];
						buffer.get(comment);

						int equalSign = -1;
						for (int i = 0; i < length; ++i) {
							if (comment[i] == '=') {
								equalSign = i;
								break;
							}
						}

						if (equalSign != -1) {
							String tag = new String(comment, 0, equalSign);
							String value = new String(comment, equalSign + 1, length - equalSign - 1);

							// convert vorbis tags to a descriptor common across all supported formats
							if (VORBIS_TAGS.containsKey(tag)) {
								tag = VORBIS_TAGS.get(tag);
							}

							metadata.addTextField(tag, value);
						}
					}

					// steps 7-9 are not applicable to FLAC as framing_bit is not used

					// explicit return to big endian even though buffer will be reassigned
					buffer.order(ByteOrder.BIG_ENDIAN);

				} else if (blockType == 5) {
					// CUESHEET
				} else if (blockType == 6) {
					// PICTURE

					int pictureType = buffer.getInt();
					int mimeLength = buffer.getInt();
					byte[] mimeType = new byte[mimeLength];
					buffer.get(mimeType);

					int descriptionLength = buffer.getInt();
					byte[] description = new byte[descriptionLength];
					buffer.get(description);

					int imageWidth = buffer.getInt();
					int imageHeight = buffer.getInt();

					int colorDepth = buffer.getInt();
					int numColors = buffer.getInt();
					int imageLength = buffer.getInt();
					byte[] image = new byte[imageLength];
					buffer.get(image);

					CoverArt cover = new CoverArt(new String(mimeType), image);
					metadata.addImage(cover);
				}
			} while (!lastBlock);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return metadata;
	}
}