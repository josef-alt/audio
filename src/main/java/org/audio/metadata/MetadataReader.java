package org.audio.metadata;

import java.nio.file.Path;

import org.audio.utils.FileUtils;

/**
 * Abstract class representing generic metadata reader.
 */
public abstract class MetadataReader {
	protected Path source;

	/**
	 * Reads and returns metadata from {@code source}.
	 * 
	 * @return {@code Metadata} instance populated with found values
	 */
	public abstract Metadata getMetadata();

	/**
	 * Sets the source for this reader
	 * 
	 * @param source file for parsing
	 */
	public void setSource(Path source) {
		this.source = source;
	}

	/**
	 * Creates a MetadataReader of the appropriate type based on the file header.
	 * 
	 * @param source file to read metadata from
	 * @return instance of a MetadataReader subclass
	 * @throws IllegalArgumentException if audio file is not recognized
	 */
	public static MetadataReader of(Path source) {
		MetadataReader reader;

		switch (FileUtils.determineFormatByHeader(source)) {
			case MP3:
				reader = new ID3TagReader();
				break;
			case FLAC:
				reader = new FLACReader();
				break;
			case WAV:
				reader = new WAVEReader();
				break;
			default:
				throw new IllegalArgumentException("Unrecognized file format");
		}

		reader.setSource(source);
		return reader;
	}
}
