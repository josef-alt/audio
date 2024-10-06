package org.audio.metadata;

import java.io.IOException;
import java.nio.ByteBuffer;
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
	 * Reads metadata from given wave files
	 * 
	 * @param source path to audio file
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
			int nRead = channel.read(buffer);
			buffer.flip();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return metadata;
	}

}
