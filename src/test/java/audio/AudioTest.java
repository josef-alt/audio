package audio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import utils.MediaUtils;
import utils.MediaUtils.Format;

public class AudioTest {

	private static final String testDirectory = "src/test/resources";

	@Test
	void readHeader() {
		Path file = Path.of(testDirectory, "mp3", "While My Guitar Gently Weeps.mp3");

		if (Files.exists(file)) {
			assertArrayEquals(new byte[] { 73, 68, 51, 3, 0, 0, 0, 0, 17, 20, 84, 65, 76, 66, 0, 0, 0, 17, 0, 0, 0, 84,
					104, 101, 32, 87, 104, 105, 116, 101, 32, 65 }, MediaUtils.getHeader(file));
		} else {
			System.out.println(file);
			fail("Could not read file");
		}
	}

	/**
	 * Helper function for testing determineFormatByHeader against a folder of
	 * same-type files.
	 * 
	 * @param directory folder containing files of {@code expected} format
	 * @param expected  return type for all test files contained in
	 *                  {@code directory}
	 */
	void test_determineFormatByHeader(Path directory, Format expected) {
		try (Stream<Path> testCases = Files.list(directory)) {
			testCases.forEach(test -> {
				try {
					assertEquals(expected, MediaUtils.determineFormatByHeader(test));
				} catch (Exception e) {
					fail("Error processing file: " + test + "\n" + e.getMessage());
				}
			});
		} catch (IOException e) {
			fail("Unable to run " + expected + " tests: " + e.getMessage());
		}
	}

	@Test
	void identifyMP3() {
		test_determineFormatByHeader(Path.of(testDirectory, "mp3"), Format.MP3);
	}

	@Test
	void identifyWAV() {
		test_determineFormatByHeader(Path.of(testDirectory, "wav"), Format.WAV);
	}

	@Test
	void identifyWMA() {
		test_determineFormatByHeader(Path.of(testDirectory, "wma"), Format.WMA);
	}
}
