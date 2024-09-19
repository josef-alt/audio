package org.audio.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hopefully this will alleviate some of the issues encountered trying
 * to store metadata as a simple map.
 */
public class Metadata {

	/**
	 * TODO: This needs some work, most text fields cannot have multiple entries
	 */
	public Map<String, List<String>> textFields;
	public List<CoverArt> images;

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
		textFields.putIfAbsent(tag, new ArrayList<>());
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
