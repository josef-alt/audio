package org.audio;
import java.nio.file.Path;

import org.audio.metadata.Metadata;
import org.audio.metadata.MetadataReader;

public class AudioFile {
	private Path source;
	private Metadata metadata;

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
