package org.audio.metadata;

/**
 * Data class for storing images
 */
public class CoverArt {

	/**
	 * MIME type + sub-type. ex. image/jpeg
	 */
	private String type;

	/**
	 * Image in byte format
	 */
	private byte[] imageData;

	/**
	 * Creates a new CoverArt instance with given data.
	 * 
	 * @param type MIME type
	 * @param data image data
	 */
	public CoverArt(String type, byte[] data) {
		this.type = type;
		this.imageData = data;
	}

	/**
	 * Returns an array of bytes representing an image.
	 * 
	 * @return the image in byte[] form.
	 */
	public byte[] getBinaryData() {
		return imageData;
	}

	/**
	 * Returns the MIME type, always starts with 'image/'
	 * 
	 * @return MIME type and sub-type separated by '/'
	 */
	public String getMimeType() {
		return type;
	}
}
