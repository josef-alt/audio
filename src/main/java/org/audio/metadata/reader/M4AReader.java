package org.audio.metadata.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.audio.metadata.Metadata;

/**
 * Reads metadata from M4A files marked with ftypM4A
 */
public class M4AReader extends MetadataReader{

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

				System.out.println(new String(fourCC) + " for " + chunkSize + " bytes");
				// TODO: parse contents

				// skip contents and move to next chunk
				channel.position(channel.position() + chunkSize - CHUNK_HEADER_SIZE);
				buffer.clear();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return metadata;
	}
}
