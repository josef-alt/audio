package org.audio.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data class for storing and structuring extracted metadata.
 */
public class Metadata {

	/*
	 * TODO: This needs some work, most text fields cannot have multiple entries.
	 * 
	 * <p>
	 * Reading WAV files introduced a new 'problem' where all text fields were
	 * represented twice (once in the INFO block, and once in an ID3 block).
	 * To avoid this, I could use a Set, but that would not maintain order.
	 * I could use a {@link java.util.LinkedHashSet}, but most tags can only appear
	 * once, so most of the lists should only have one element, which makes a
	 * LinkedHashSet seem like overkill. For now, I will just use {@link List}.
	 * </p>
	 */

	/**
	 * Map of textual metadata using {@link Constants} wherever possible.
	 */
	public Map<String, List<String>> textFields;

	/**
	 * List of all images embedded in audio file in order of occurrence.
	 */
	public List<CoverArt> images;

	/**
	 * Initialize new instance with empty collections
	 */
	public Metadata() {
		textFields = new HashMap<>();
		images = new ArrayList<>();
	}

	/**
	 * Adds {@code value} to the {@code tag} metadata group, creating a new group if
	 * necessary.
	 * 
	 * @param tag   simple name of text field
	 * @param value text field's value
	 */
	public void addTextField(String tag, String value) {
		// make sure that there are no duplicate entries
		if (textFields.containsKey(tag)) {
			if (textFields.get(tag).contains(value)) {
				return;
			}
		} else {
			textFields.put(tag, new ArrayList<>());
		}

		// add value to map if duplicate was not found
		textFields.get(tag).add(value);
	}

	/**
	 * Adds {@code image} to associated images list
	 * 
	 * @param image instance of CoverArt created from the {@code AudioFile} header
	 */
	public void addImage(CoverArt image) {
		images.add(image);
	}
}
