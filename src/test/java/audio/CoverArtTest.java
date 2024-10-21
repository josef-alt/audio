package audio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.audio.AudioFile;
import org.audio.metadata.CoverArt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testing image extraction, still needs work
 */
public class CoverArtTest {

	/**
	 * path to test directory
	 */
	private static final String testDirectory = "src/test/resources";

	/**
	 * expected album art for comparison
	 */
	private static Map<String, byte[]> expected;

	/**
	 * Prepares for tests by pre-loading all expected images.
	 * 
	 * @throws IOException if images directory is inaccessible
	 */
	@BeforeAll
	public static void setUp() throws IOException {
		Path imageDir = Path.of(testDirectory, "images");
		expected = new HashMap<>();

		Files.list(imageDir).forEach(file -> {
			try {
				expected.put(file.getFileName().toString(), Files.readAllBytes(file));
			} catch (IOException e) {
				System.err.printf("Failed to load image: %s%n", file.toString());
			}
		});
	}

	/**
	 * Testing files that have mb.jpg as their cover art
	 * 
	 * @param filePath relative path to test file.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "flac/mb with jpg.flac", "mp3/sample with jpg.mp3", "wav/sample2.WAV" })
	void test_mbJPG(String filePath) {
		AudioFile af = new AudioFile(Path.of(testDirectory, filePath));
		List<CoverArt> cover = af.getMetadata().images;

		if (cover.isEmpty()) {
			fail("No cover art found");
		}

		byte[] extracted = cover.get(0).getBinaryData();
		assertArrayEquals(expected.get("mb.jpg"), extracted);
	}

	/**
	 * Testing files that have mb.png as their cover art
	 * 
	 * @param filePath relative path to test file.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "flac/mb with png.flac", "mp3/sample with png.mp3", "wav/sample3.WAV" })
	void test_mbPNG(String filePath) {
		AudioFile af = new AudioFile(Path.of(testDirectory, filePath));
		List<CoverArt> cover = af.getMetadata().images;

		if (cover.isEmpty()) {
			fail("No cover art found");
		}

		byte[] extracted = cover.get(0).getBinaryData();
		assertArrayEquals(expected.get("mb.png"), extracted);
	}
}
