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
import org.audio.utils.ImageExtractor;

/**
 * Reads metadata from M4A files marked with ftypM4A
 */
public class M4AReader extends MetadataReader{

	/**
	 * Prevent instantiation from outside.
	 * Use {@link MetadataReader#of(Path)} to create instances.
	 */
	protected M4AReader() {
	}

	/**
	 * Mappings from M4A four character code tags to {@link Constants}.
	 */
	private static final Map<String, String> M4A_TAGS;
	static {
		Map<String, String> tags = new HashMap<>();
		tags.put("\u00A9ART", Constants.ARTIST_NAME);
		tags.put("aART", Constants.ALBUM_ARTIST_NAME);
		tags.put("\u00A9alb", Constants.ALBUM_NAME);
		tags.put("\u00A9wrt", Constants.COMPOSER);
		tags.put("\u00A9nam", Constants.TITLE);
		tags.put("trck", Constants.TRACK_NUMBER);
		tags.put("disk", Constants.DISC_NUMBER);
		tags.put("cprt", Constants.COPYRIGHT);
		tags.put("\u00A9too", Constants.ENCODING_INFO);
		tags.put("\u00A9day", Constants.YEAR);
		tags.put("gnre", Constants.GENRE);
		tags.put("\u00A9gen", Constants.GENRE);
		M4A_TAGS = Collections.unmodifiableMap(tags);
	}

	/**
	 * Chunk headers are a four byte character code followed by four byte chunk size
	 */
	private static final int CHUNK_HEADER_SIZE = 8;

	/**
	 * Reads tags from given M4A files
	 * 
	 * @return metadata in key-value pairs
	 */
	public Metadata getMetadata() {
		Metadata metadata = new Metadata();

		try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ)) {

			// read until end of file
			ByteBuffer buffer = ByteBuffer.allocate(CHUNK_HEADER_SIZE);
			while (channel.read(buffer) == CHUNK_HEADER_SIZE) {
				// switch buffer to read mode
				buffer.flip();

				// parse header
				int chunkSize = buffer.getInt();
				byte[] fourCC = new byte[4];
				buffer.get(fourCC);

				if ((fourCC[0] & 0xFF) == 0x66 && (fourCC[1] & 0xFF) == 0x74 && (fourCC[2] & 0xFF) == 0x79
						&& (fourCC[3] & 0xFF) == 0x70) {
					// ftyp
					parseHeader(channel, chunkSize);
				} else if ((fourCC[0] & 0xFF) == 0x66 && (fourCC[1] & 0xFF) == 0x72 && (fourCC[2] & 0xFF) == 0x65
						&& (fourCC[3] & 0xFF) == 0x65) {
					// free
					// skip contents and move to next chunk
					channel.position(channel.position() + chunkSize - CHUNK_HEADER_SIZE);
				} else if ((fourCC[0] & 0xFF) == 0x6D && (fourCC[1] & 0xFF) == 0x64 && (fourCC[2] & 0xFF) == 0x61
						&& (fourCC[3] & 0xFF) == 0x74) {
					// mdat
					// skip contents and move to next chunk
					channel.position(channel.position() + chunkSize - CHUNK_HEADER_SIZE);
				} else if ((fourCC[0] & 0xFF) == 0x6D && (fourCC[1] & 0xFF) == 0x6F && (fourCC[2] & 0xFF) == 0x6F
						&& (fourCC[3] & 0xFF) == 0x76) {
					// moov
					parseMOOV(channel, chunkSize, metadata);
				}

				buffer.clear();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return metadata;
	}

	/**
	 * Parse 'ftyp' chunk at start of m4a file.
	 * 
	 * <p>
	 * Currently the data parsed by this function is not used or stored anywhere.
	 * This function mainly serves to reposition {@code channel} without totally
	 * disregarding the encountered structure. By implementing this I hope to
	 * ensure that everything is being read properly, rather than just jumping to
	 * the next expected position. Additionally, I might want to use this data at
	 * some future point.
	 * </p>
	 * 
	 * @param channel   channel to audio file in read mode
	 * @param chunkSize number of bytes in the ftyp header including four-cc and
	 *                  chunk size
	 * @throws IOException if channel is inaccessible or buffer runs out of data
	 *                     unexpectedly
	 */
	private void parseHeader(FileChannel channel, int chunkSize) throws IOException {
		int bytesToRead = chunkSize - CHUNK_HEADER_SIZE;
		ByteBuffer chunkBuffer = ByteBuffer.allocate(bytesToRead);
		if (channel.read(chunkBuffer) == bytesToRead) {
			chunkBuffer.flip();

			// four byte string representing the format
			byte[] majorBrand = new byte[4];
			chunkBuffer.get(majorBrand);

			// this is a binary coded decimal
			byte[] minorVersion = new byte[4];
			chunkBuffer.get(minorVersion);

			// discard compatible types for now
			chunkBuffer.position(chunkBuffer.position() + bytesToRead - 8);
		}
	}

	/**
	 * Helper function for parsing Movie (moov) chunk. This chunk contains player
	 * information (duration, time scale, volume, rate, etc) and display
	 * information (artist, title, etc).
	 * 
	 * @param channel   channel to audio file in read mode
	 * @param chunkSize number of bytes in the moov block including the header
	 * @param metadata  instance to be populated with extracted data
	 * @throws IOException if channel is inaccessible or buffer runs out of data
	 */
	private void parseMOOV(FileChannel channel, int chunkSize, Metadata metadata) throws IOException {
		int bytesToRead = chunkSize - CHUNK_HEADER_SIZE;
		ByteBuffer chunkBuffer = ByteBuffer.allocate(bytesToRead);
		if (channel.read(chunkBuffer) == bytesToRead) {
			chunkBuffer.flip();

			// read unknown number of sub-chunks
			while (chunkBuffer.remaining() >= CHUNK_HEADER_SIZE) {
				int size = chunkBuffer.getInt();
				byte[] fourCC = new byte[4];
				chunkBuffer.get(fourCC);

				// look for user-data block
				if ((fourCC[0] & 0xFF) == 0x75 && (fourCC[1] & 0xFF) == 0x64 && (fourCC[2] & 0xFF) == 0x74
						&& (fourCC[3] & 0xFF) == 0x61) {
					parseUserData(chunkBuffer, metadata);
				} else {
					chunkBuffer.position(chunkBuffer.position() + size - CHUNK_HEADER_SIZE);
				}
			}
		}
	}

	/**
	 * Helper function for parsing user data (udta) chunk and extracting data to
	 * populate {@code metadata} instance.
	 * 
	 * @param buffer   buffer containing entire chunk
	 * @param metadata instance to be populated with metadata
	 */
	private void parseUserData(ByteBuffer buffer, Metadata metadata) {
		// size of metadata block
		int size;

		// meta type
		byte[] type = new byte[4];

		// files may contain other types of chunks that are not currently supported
		// read until we find the start of the meta chunk
		do {
			size = buffer.getInt();
			buffer.get(type);

			// skip chunk if Xtra or some other unsupported type
			if (!isMetaHeader(type)) {
				buffer.position(buffer.position() + size - CHUNK_HEADER_SIZE);
			}
		} while (!isMetaHeader(type));

		// sample offset table version
		int version = buffer.get();

		// offset table flags
		int flags = buffer.get();
		flags = (flags << 8) | buffer.get();
		flags = (flags << 8) | buffer.get();

		// parse unknown number of user data entries
		while (buffer.remaining() >= CHUNK_HEADER_SIZE) {
			size = buffer.getInt();
			buffer.get(type);

			// look for ilst block
			if ((type[0] & 0xFF) == 0x69 && (type[1] & 0xFF) == 0x6C && (type[2] & 0xFF) == 0x73
					&& (type[3] & 0xFF) == 0x74) {
				int bytesToRead = size;
				int bytesRead = CHUNK_HEADER_SIZE;
				while (bytesRead < bytesToRead) {
					size = buffer.getInt();
					buffer.get(type);

					int length = buffer.getInt();
					int dataMarker = buffer.getInt();

					// skipping next 64 bits for now
					buffer.getLong();

					byte[] data = new byte[length - 16];
					buffer.get(data);

					// save user data to our metadata instance
					// because many of the tags begin with 0xA9, we need to make sure to
					// use a character encoding that will support this.
					String key = new String(type, StandardCharsets.ISO_8859_1);
					if (key.equals("covr")) {
						metadata.addImage(ImageExtractor.extractImage(data));
					} else {
						// convert four-cc to constant name
						if (M4A_TAGS.containsKey(key)) {
							key = M4A_TAGS.get(key);
						}
						metadata.addTextField(key, new String(data));
					}

					// move to the next entry
					bytesRead += size;
				}
			} else {
				// TODO: handle other chunk types
				buffer.position(buffer.position() + size - CHUNK_HEADER_SIZE);
			}
		}
	}

	/**
	 * Return whether or not an array represents the header of the meta-data
	 * section.
	 * 
	 * @param header four byte character code
	 * @return true if {@code header} equals 'meta'
	 */
	private static boolean isMetaHeader(byte[] header) {
		return (header[0] & 0xFF) == 0x6D && (header[1] & 0xFF) == 0x65 && (header[2] & 0xFF) == 0x74
				&& (header[3] & 0xFF) == 0x61;
	}
}
