package org.audio.metadata.reader;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.audio.metadata.Constants;
import org.audio.metadata.Metadata;

/**
 * Reads metadata from OGG files.
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
	 * Reads metadata from given OGG files
	 * 
	 * @return metadata in key-value pairs
	 */
	public Metadata getMetadata() {
		Metadata metadata = new Metadata();

		// TODO: figure out ogg parsing
		// I imagine this will be very similar to FLAC

		return metadata;
	}
}