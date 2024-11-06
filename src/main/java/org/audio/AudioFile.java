package org.audio;
import java.nio.file.Path;

import org.audio.metadata.Metadata;
import org.audio.metadata.reader.MetadataReader;

/**
 * Entry point for all file operations.
 */
public class AudioFile {

	/**
	 * Location of audio file
	 */
	private Path source;

	/**
	 * Extracted metadata
	 */
	private Metadata metadata;

	/**
	 * Create a new instance initialized with given source file.
	 * 
	 * @param src file location
	 */
	public AudioFile(Path src) {
		source = src;

		// TODO new thread
		metadata = MetadataReader.of(source).getMetadata();
	}

	/**
	 * Retrieve metadata from {@code source} as key-value pairs
	 * 
	 * @return An unmodifiable map containing metadata
	 */
	public Metadata getMetadata() {
		return metadata;
	}
}
