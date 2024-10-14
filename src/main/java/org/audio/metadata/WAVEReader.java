package org.audio.metadata;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 * Read metadata from audio files according to wave specifications.
 */
public class WAVEReader extends MetadataReader {

	/**
	 * WAVE format specifies 44 byte file header
	 */
	private static int WAVE_HEADER_SIZE = 44;

	/**
	 * Each RIFF chunk contains a 4 byte tag followed by a 4 byte size
	 */
	private static int CHUNK_HEADER_SIZE = 8;

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

				ByteBuffer chunkBuffer = ByteBuffer.allocate(chunkSize);
				nRead = channel.read(chunkBuffer);
				if ((fourCC[0] & 0xFF) == 0x4C && (fourCC[1] & 0xFF) == 0x49 && (fourCC[2] & 0xFF) == 0x53
						&& (fourCC[3] & 0xFF) == 0x54) {
					// LIST block
					System.err.printf("Unimplemented: %s%n", new String(fourCC));
				} else if ((fourCC[0] & 0xFF) == 0x69 && (fourCC[1] & 0xFF) == 0x64 && (fourCC[2] & 0xFF) == 0x33
						&& (fourCC[3] & 0xFF) == 0x20) {
					// id3 block
					System.err.printf("Unimplemented: %s%n", new String(fourCC));
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

}
