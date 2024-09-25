package org.audio.metadata;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FLACReader {

	private static int FLAC_HEADER_SIZE = 4;
	private static int BLOCK_HEADER_SIZE = 32;

	/**
	 * Reads metadata from given flac files
	 * 
	 * @param source path to audio file
	 * @return metadata in key-value pairs
	 */
	public static Metadata getMetadata(Path source) {
		Metadata metadata = new Metadata();

		try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ)) {

			// 32-bit flaC stream marker
			ByteBuffer buffer = ByteBuffer.allocate(FLAC_HEADER_SIZE);
			int nRead = channel.read(buffer);
			buffer.flip();

			// stream info block
			// optional metadata blocks

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return metadata;
	}
}
