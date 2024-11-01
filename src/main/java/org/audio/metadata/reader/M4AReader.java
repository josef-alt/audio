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
					// skip contents and move to next chunk
					channel.position(channel.position() + chunkSize - CHUNK_HEADER_SIZE);
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

			// unknown number of compatible brands
			// bytesToRead - majorBrand.length - minorVersion.length bytes
			// each compatible brand should be four bytes
			int comptatibleCount = (bytesToRead - 8) / 4;
			for (int c = 0; c < comptatibleCount; c++) {
				byte[] compatible = new byte[4];
				chunkBuffer.get(compatible);
				// TODO: add to metadata?
			}
		}
	}
}
