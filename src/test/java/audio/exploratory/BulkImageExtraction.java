package audio.exploratory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.audio.AudioFile;
import org.audio.metadata.Constants;
import org.audio.metadata.Metadata;

/**
 * A simple exploratory script for extracting and saving cover art from all
 * audio files in a folder to find any incompatible files.
 */
public class BulkImageExtraction {

	/**
	 * Name of folder containing sample audio files
	 */
	private static final String inputDirectory = "E:/Media/Music/with art/";

	/**
	 * Name of output directory for extracted images
	 */
	private static final String outputDirectory = "E:/Media/Music/dummy folder/";

	/**
	 * Sample script using the audio library to extract the first image from a batch
	 * of audio files.
	 * 
	 * <p>
	 * This 'test' will never fail, and is instead intended to locate audio files
	 * that could be used for future, functional tests.
	 * </p>
	 */
	public static void main(String[] args) {
		Path inputPath = Path.of(inputDirectory);

		// ensure output directory exists and abort if unable to create
		try {
			if (createIfMissing(Path.of(outputDirectory))) {
				System.out.printf("Created output directory (%s)%n", outputDirectory);
			}
		} catch (IOException e) {
			System.err.printf("Failed to create output directory (%s)%n%s%n", outputDirectory, e.toString());
			return;
		}

		try {
			Files.list(inputPath).forEach(file -> {
				AudioFile af = new AudioFile(file);
				Metadata meta = af.getMetadata();
				Map<String, List<String>> textFields = meta.getTextFields();

				// check that all necessary information is present
				if (meta.getImages().isEmpty()) {
					System.err.printf("No image found in %s%n", file.toString());
					return;
				}
				if (!textFields.containsKey(Constants.ALBUM_NAME) || !textFields.containsKey(Constants.ARTIST_NAME)) {
					System.err.printf("Missing track information needed for labeling %s%n", file.toString());
					return;
				}
				if (meta.getImages().get(0).getMimeType().endsWith("/")) {
					System.err.printf("No mime type found for %s%n", file.toString());
					System.out.println(Arrays.toString(meta.getImages().get(0).getBinaryData()).substring(0, 50));
				}

				// writing extracted image to file
				String outputFile = getOutputFileName(textFields.get(Constants.ARTIST_NAME).get(0),
						textFields.get(Constants.ALBUM_NAME).get(0), meta.getImages().get(0).getMimeType());
				try {
					Files.write(Path.of(outputDirectory, outputFile), meta.getImages().get(0).getBinaryData());
				} catch (IOException e) {
					System.err.printf("Failed to write file %s for input %s%n", outputFile, file.toString());
				}
			});
		} catch (NotDirectoryException e) {
			System.err.printf("Found a file but expected a directory: %s%n", inputPath.toString());
		} catch (IOException e) {
			System.err.printf("An error occurred: %s%n", e.toString());
		}
	}

	/**
	 * Check to see if {@code directory} exists, create it if not.
	 * 
	 * @param directory folder to be created
	 * @throws IOException if an error occurs or the parent directory does not exist
	 */
	private static boolean createIfMissing(Path directory) throws IOException {
		if (!Files.exists(directory)) {
			Files.createDirectory(Path.of(outputDirectory));
			return true;
		}
		return false;
	}

	/**
	 * Remove any characters other than word characters and whitespace to make sure
	 * file names are file system compatible.
	 * 
	 * @param original string containing special characters
	 * @return sanitized version of {@code original}
	 */
	private static String removeSpecialCharacters(String original) {
		return original.replaceAll("[^\\w\\s]", "");
	}

	/**
	 * Assemble file name based on metadata.
	 * 
	 * @param artist   artist name
	 * @param album    album name
	 * @param mimeType image type
	 * @return the name of the output file to be created
	 */
	private static String getOutputFileName(String artist, String album, String mimeType) {
		// remove image/ prefix and assign unknown images to jpeg
		mimeType = mimeType.substring(mimeType.indexOf("/") + 1);
		if (mimeType.isEmpty()) {
			mimeType = "jpg";
		}

		// make sure strings are file system compatible
		artist = removeSpecialCharacters(artist);
		album = removeSpecialCharacters(album);

		// putting it all together
		return String.format("%s. %s.%s", artist, album, mimeType);
	}
}
