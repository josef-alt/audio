package org.audio.metadata.reader;

import org.audio.metadata.Metadata;

/**
 * Reads metadata from MP4 files or M4A files marked as ftypmp4
 */
public class MP4Reader extends MetadataReader {

	/**
	 * Reads tags from given MP4/M4A files
	 * 
	 * @return metadata in key-value pairs
	 */
	public Metadata getMetadata() {
		Metadata metadata = new Metadata();

		// TODO: read MP4
		System.err.println("Unsupported format: MP4");

		return metadata;
	}
}
