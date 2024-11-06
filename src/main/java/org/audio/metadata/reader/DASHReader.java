package org.audio.metadata.reader;

import java.nio.file.Path;

import org.audio.metadata.Metadata;

/**
 * Reads metadata from m4a files marked with ftypdash
 */
public class DASHReader extends MetadataReader {

	/**
	 * Prevent instantiation from outside.
	 * Use {@link MetadataReader#of(Path)} to create instances.
	 */
	protected DASHReader() {
	}

	/**
	 * Reads tags from given M4A DASH files
	 * 
	 * @return metadata in key-value pairs
	 */
	public Metadata getMetadata() {
		Metadata metadata = new Metadata();

		// TODO: read dash
		System.err.println("Unsupported format: DASH");

		return metadata;
	}
}
