package audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.audio.AudioFile;
import org.audio.metadata.Metadata;
import org.junit.jupiter.api.Test;

/**
 * JUnit test suite for file metadata
 */
public class MetadataTest {

	/**
	 * Path to test directory as a string because it makes subpaths easier
	 */
	private static final String testDirectory = "src/test/resources";

	/**
	 * Returns a lazy populated stream of test files from {@code testFolder} with
	 * expected output files filtered out.
	 * 
	 * @param testFolder test suite name
	 * @return stream of input files from {@code testFolder}
	 * @throws IOException if there is an issue accessing (@code testFolder}
	 */
	private static Stream<Path> testFiles(String testFolder) throws IOException {
		Path dir = Paths.get(testDirectory, testFolder);
		return Files.list(dir)
				.filter(Files::isRegularFile)
				.filter(file -> !file.toString().endsWith("_metadata"));
	}

	/**
	 * Helper function for retrieving the expected output for a test case
	 * 
	 * @param inputFile path to test case
	 * @return Map of expected metadata values
	 */
	private static Map<String, List<String>> getExpectedOutput(Path inputFile) {
		String expectedFileName = inputFile.getFileName() + "_metadata";
		Path expectedFilePath = inputFile.getParent().resolve(expectedFileName);

		try {
			return Files.readAllLines(expectedFilePath)
					.stream()
					.map(str -> str.split("="))
					.collect(Collectors.toMap(
							arr -> arr[0], 
							arr -> List.of(arr[1]), 
							(prev, curr) -> {
								prev.add(curr.get(0));
								return prev;
							}));
		} catch (IOException e) {
			return Collections.EMPTY_MAP;
		}
	}

	/**
	 * Check that all entries in the output file match
	 * {@link AudioFile#getMetadata()}.
	 * 
	 * @param path path to an individual test case
	 */
	private void checkMetadata(Path path) {
		System.out.println(" - " + path);
		Map<String, List<String>> expected = getExpectedOutput(path);
		if (expected == Collections.EMPTY_MAP) {
			System.err.println("No output file found for " + path);
			fail("No output file found for " + path);
		}

		AudioFile af = new AudioFile(path);
		Metadata meta = af.getMetadata();

		for (String key : expected.keySet()) {
			assertTrue(meta.textFields.containsKey(key));
			assertTrue(meta.textFields.get(key).containsAll(expected.get(key)));
		}
	}

	/**
	 * Helper function for checking metadata for entire folder
	 * 
	 * @param folder path to folder containing multiple test cases
	 */
	private void test(String folder) {
		try {
			System.out.println("Testing " + folder);
			testFiles(folder).forEach(this::checkMetadata);
		} catch (IOException e) {
			System.err.println("Error processing test folder: " + folder);
			fail(e.getLocalizedMessage());
		}
	}

	@Test
	void test_MP3() {
		test("mp3");
	}

	@Test
	void test_FLAC() {
		test("flac");
	}

	@Test
	void test_WAVE() {
		test("wav");
	}

	@Test
	void test_AAC() {
		test("aac");
	}

	@Test
	void test_AIF() {
		test("aif");
	}

	@Test
	void test_OGG() {
		test("ogg");
	}

	@Test
	void test_M4A() {
		test("m4a");
	}

	@Test
	void test_WMA() {
		test("wma");
	}
}
