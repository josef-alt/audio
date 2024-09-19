package org.audio;
import java.nio.file.Path;

import org.audio.metadata.ID3TagReader;
import org.audio.metadata.Metadata;
import org.audio.utils.FileUtils;
import org.audio.utils.FileUtils.Format;

public class AudioFile {
	private Path source;
	private Metadata metadata;

	public AudioFile(Path src) {
		source = src;

		// TODO new thread
		if (FileUtils.determineFormatByHeader(source) == Format.MP3) {
			metadata = ID3TagReader.getMetadata(source);
		}
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
