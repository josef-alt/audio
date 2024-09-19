package org.audio.metadata;

/**
 * Data class for storing images
 */
public class CoverArt {
	private String type;
	private byte[] imageData;

	public CoverArt(String type, byte[] data) {
		this.type = type;
		this.imageData = data;
	}

	public byte[] getBinaryData() {
		return imageData;
	}
}
