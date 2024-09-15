package org.audio;
import java.nio.file.Path;
import java.util.Map;

import org.audio.utils.FileUtils;
import org.audio.utils.FileUtils.Format;

public class AudioFile {
	private Path source;
	private Map<String, String> metadata;

	public AudioFile(Path src) {
		source = src;
	}
}
