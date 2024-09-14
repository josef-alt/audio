package audio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import utils.FileUtils;

public class AudioIT {

	private static final FileUtils mu = new FileUtils();

	@Test
	void readHeader() {
		Path file = Path.of("While My Guitar Gently Weeps.mp3");

		if (Files.exists(file)) {
			assertArrayEquals(new byte[] { 73, 68, 51, 3, 0, 0, 0, 0, 17, 20, 84, 65, 76, 66, 0, 0, 0, 17, 0, 0, 0, 84,
					104, 101, 32, 87, 104, 105, 116, 101, 32, 65 }, mu.getHeader(file));
		} else {
			fail("Could not read file");
		}
	}
}
