package org.audio;
import java.nio.file.Path;

import org.audio.metadata.FLACReader;
import org.audio.metadata.ID3TagReader;
import org.audio.metadata.Metadata;
import org.audio.utils.FileUtils;

public class AudioFile {
	private Path source;
	private Metadata metadata;

	public AudioFile(Path src) {
		source = src;

		// TODO new thread
		switch (FileUtils.determineFormatByHeader(source)) {
			case MP3:
				metadata = ID3TagReader.getMetadata(source);
				break;
			case FLAC:
				metadata = FLACReader.getMetadata(source);
				break;
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
