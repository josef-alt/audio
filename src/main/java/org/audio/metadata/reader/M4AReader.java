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

				if ((fourCC[0] & 0xFF) == 0x66 && (fourCC[1] & 0xFF) == 0x74 && (fourCC[2] & 0xFF) == 0x79
						&& (fourCC[3] & 0xFF) == 0x70) {
					// ftyp
				} else if ((fourCC[0] & 0xFF) == 0x66 && (fourCC[1] & 0xFF) == 0x72 && (fourCC[2] & 0xFF) == 0x65
						&& (fourCC[3] & 0xFF) == 0x65) {
					// free
				} else if ((fourCC[0] & 0xFF) == 0x6D && (fourCC[1] & 0xFF) == 0x64 && (fourCC[2] & 0xFF) == 0x61
						&& (fourCC[3] & 0xFF) == 0x74) {
					// mdat
				} else if ((fourCC[0] & 0xFF) == 0x6D && (fourCC[1] & 0xFF) == 0x6F && (fourCC[2] & 0xFF) == 0x6F
						&& (fourCC[3] & 0xFF) == 0x76) {
					// moov
				}

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
