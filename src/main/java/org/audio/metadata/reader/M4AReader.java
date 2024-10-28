package org.audio.metadata.reader;

import org.audio.metadata.Metadata;

/**
 * Reads metadata from M4A files marked with ftypM4A
 */
public class M4AReader extends MetadataReader{

	/**
	 * Reads tags from given M4A files
	 * 
	 * @return metadata in key-value pairs
	 */
	public Metadata getMetadata() {
		Metadata metadata = new Metadata();

		// TODO: read M4A

		return metadata;
	}
}
