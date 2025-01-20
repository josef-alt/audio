package org.audio.metadata.reader;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.audio.metadata.Constants;
import org.audio.metadata.Metadata;

/**
 * Reads metadata from OGG files.
 * 
 * <p>
 * Notes: OGG could be used with Opus, Vorbis, Theora, or Speex.
 * </p>
 */
public class OGGReader extends MetadataReader {

	/**
	 * Prevent instantiation from outside.
	 * Use {@link MetadataReader#of(Path)} to create instances.
	 */
	protected OGGReader() {
	}

	/**
	 * Mapping from VORBIS tags to {@link Constants}.
	 */
	private static final Map<String, String> VORBIS_TAGS;
	static {
		Map<String, String> tags = new HashMap<>();
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
	 * OGG headers are 27 bytes long.
	 */
	private static final int PAGE_HEADER_SIZE = 27;
	
	/**
	 * VORBIS segment header
	 */
	private static final byte[] VORBIS_HEADER = {0x76, 0x6F, 0x72, 0x62, 0x69, 0x73 };

	/**
	 * Reads metadata from given OGG files
	 * 
	 * @return metadata in key-value pairs
	 */
	public Metadata getMetadata() {
		Metadata metadata = new Metadata();

		try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ)) {
			// tracking file position
			long fileSize = channel.size();
			long position = 0;

			// reading pages
			while (position < fileSize) {
				ByteBuffer buffer = ByteBuffer.allocate(PAGE_HEADER_SIZE);
				channel.read(buffer);
				buffer.flip();

				if (buffer.remaining() < PAGE_HEADER_SIZE) {
					// error
					break;
				}

				// OggS
				byte[] capture = new byte[4];
				buffer.get(capture);
				
				// header information
				byte version = buffer.get();
				byte type = buffer.get();

				if (type == 1) {
					// continuation of previous page
				} else if (type == 2) {
					// first page
				} else if (type == 3) {
					// last page
				}

				long granule = buffer.getLong();
				int serialNumber = buffer.getInt();
				int sequenceNumber = buffer.getInt();
				int checkSum = buffer.getInt();
				int segments = buffer.get() & 0xFF;
				
				position += PAGE_HEADER_SIZE;

				// read in table of segment sizes
				ByteBuffer table = ByteBuffer.allocate(segments);
				channel.read(table);
				table.flip();
				byte[] segmentTable = new byte[segments];
				table.get(segmentTable);

				// read all segments
				for (byte segmentSize : segmentTable) {
					// ignore empty segments when entry is a multiple of 255
					if (segmentSize == 0) {
						continue;
					}
					
					int length = segmentSize & 0xFF;
					try {
						if (length > 7) {
							ByteBuffer segmentBuffer = ByteBuffer.allocate(7);
							channel.read(segmentBuffer);
							segmentBuffer.flip();

							byte[] marker = new byte[7];
							segmentBuffer.get(marker);
							if (Arrays.equals(marker, 1, 7, VORBIS_HEADER, 0, 6)) {
								FLACReader.extractVORBISData(channel, length - 7, metadata);
							}
						}
					} catch (BufferUnderflowException | NegativeArraySizeException e) {
						// TODO: first block is inconsistently sized and causes errors
					}
	
					position += length;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return metadata;
	}
}