package org.audio.metadata;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Read metadata from audio files according to wave specifications.
 */
public class WAVEReader extends MetadataReader {

	/**
	 * WAVE format specifies 44 byte file header
	 */
	private static final int WAVE_HEADER_SIZE = 44;

	/**
	 * Each RIFF chunk contains a 4 byte tag followed by a 4 byte size
	 */
	private static final int CHUNK_HEADER_SIZE = 8;

	// some common tags and their meanings
	private static final Map<String, String> WAV_TAGS;
	static {
		Map<String, String> tags = new HashMap<>();
		tags.put("IARL", Constants.FILE_WEBPAGE);
		tags.put("IART", Constants.ARTIST_NAME);
		tags.put("ICOP", Constants.COPYRIGHT);
		tags.put("ICRD", Constants.YEAR);
		tags.put("IGNR", Constants.GENRE);
		tags.put("INAM", Constants.TITLE);
		tags.put("IPRD", Constants.ALBUM_NAME);
		tags.put("ISFT", Constants.ENCODING_INFO);

		WAV_TAGS = Collections.unmodifiableMap(tags);
	}

	/**
	 * Reads metadata from given wave files
	 * 
	 * @return metadata in key-value pairs
	 */
	public Metadata getMetadata() {
		Metadata metadata = new Metadata();

		try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ)) {

			/**
			 * RIFF-WAVE file header
			 * 4-byte 'RIFF' marker
			 * 4-byte file size
			 * 4-byte 'WAVE' marker
			 * 3-byte 'fmt' marker
			 * 4-byte format length
			 * 2-byte format type
			 * 2-byte number of channels
			 * 4-byte sample rate
			 * 4-byte sample data
			 * 2-byte sample data
			 * 4-byte data chunk header
			 * 4-byte length of data chunk
			 */
			ByteBuffer buffer = ByteBuffer.allocate(WAVE_HEADER_SIZE);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			int nRead = channel.read(buffer);
			buffer.flip();

			byte[] riffMarker = new byte[4];
			buffer.get(riffMarker);

			int fileSize = buffer.getInt();

			byte[] waveMarker = new byte[4];
			buffer.get(waveMarker);

			byte[] fmtMarker = new byte[4];
			buffer.get(fmtMarker);

			int fmtLength = buffer.getInt();
			short fmtType = buffer.getShort();
			short numChannels = buffer.getShort();
			int sampleRate = buffer.getInt();
			int sampleData = buffer.getInt();
			short monoStereoFlag = buffer.getShort();
			short bitsPerSample = buffer.getShort();

			byte[] dataMarker = new byte[4];
			buffer.get(dataMarker);

			int dataSize = buffer.getInt();

			// skip over data portion
			long position = channel.position();
			channel.position(position + dataSize);

			// read optional chunks
			buffer = ByteBuffer.allocate(CHUNK_HEADER_SIZE);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			while (channel.read(buffer) == CHUNK_HEADER_SIZE) {
				buffer.flip();

				byte[] fourCC = new byte[4];
				buffer.get(fourCC);
				int chunkSize = buffer.getInt();

				if ((fourCC[0] & 0xFF) == 0x4C && (fourCC[1] & 0xFF) == 0x49 && (fourCC[2] & 0xFF) == 0x53
						&& (fourCC[3] & 0xFF) == 0x54) {
					// LIST block

					ByteBuffer chunkBuffer = ByteBuffer.allocate(chunkSize);
					chunkBuffer.order(ByteOrder.LITTLE_ENDIAN);
					nRead = channel.read(chunkBuffer);
					chunkBuffer.flip();

					parseListChunk(chunkBuffer, metadata);
				} else if ((fourCC[0] & 0xFF) == 0x69 && (fourCC[1] & 0xFF) == 0x64 && (fourCC[2] & 0xFF) == 0x33
						&& (fourCC[3] & 0xFF) == 0x20) {
					// id3 block
					ID3TagReader.extractID3v2Data(channel, metadata);
				} else {
					// unsupported block
					System.err.printf("Unsupported block type: %s%n", new String(fourCC));
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
	 * Parse LIST chunk for metadata
	 *
	 * @param chunkBuffer byte buffer containing list chunk
	 * @param metadata    instance to be populated
	 */
	private void parseListChunk(ByteBuffer chunkBuffer, Metadata metadata) {
		if (chunkBuffer.remaining() > 4) {
			byte[] listType = new byte[4];
			chunkBuffer.get(listType);

			// check for INFO chunk
		}

		while (chunkBuffer.remaining() > 8) {
			byte[] fourCC = new byte[4];
			chunkBuffer.get(fourCC);

			int size = chunkBuffer.getInt();
			byte[] data = new byte[size];
			chunkBuffer.get(data);

			// convert from four byte character code to standard name, if possible
			String key = new String(fourCC);
			if (WAV_TAGS.containsKey(key)) {
				key = WAV_TAGS.get(key);
			}
			metadata.addTextField(key, new String(data, 0, size - 1));

			if (chunkBuffer.hasRemaining()) {
				// TODO: figure out why some list elements have an extra 0
				byte next = chunkBuffer.get();
				if (next == 0x00) {
					// Skip a byte
				} else {
					chunkBuffer.position(chunkBuffer.position() - 1);
				}
			}
		}
	}
}
