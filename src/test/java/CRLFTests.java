import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import util.CRLFLine;

public class CRLFTests {
	byte[] replyLengthTests(String string) throws IOException {
		return CRLFLine.readCrlfLine(new ByteArrayInputStream(string.getBytes()), 3);
	}

	@Test
	void testLengthLimit() throws IOException {
		assertNull(replyLengthTests("abcde\r\n"));
	}

	@Test
	void testLengthLimit1() throws IOException {
		assertArrayEquals("abc".getBytes(), replyLengthTests("abc\r\n"));
	}

	@Test
	void testLengthLimit2() throws IOException {
		assertNull(replyLengthTests("abc"));
	}

	@Test
	void testLengthLimit3() throws IOException {
		assertArrayEquals("ab".getBytes(), replyLengthTests("ab\r\n"));
	}

	@Test
	void testLengthLimit4() throws IOException {
		assertNull(replyLengthTests("ab"));
	}

	@Test
	void testLengthLimit5() throws IOException {
		assertNull(replyLengthTests(""));
	}

	@Test
	void testLengthLimit6() throws IOException {
		assertArrayEquals("".getBytes(), replyLengthTests("\r\n"));
	}

	@Test
	void testLengthLimit7() throws IOException {
		assertArrayEquals("a b".getBytes(), replyLengthTests("a b\r\n"));
	}

	@Test
	void testCRLF() throws IOException {
		assertNull(replyLengthTests("abc\r"));
	}

	@Test
	void testCRLF1() throws IOException {
		assertNull(replyLengthTests("abc\n"));
	}

	@Test
	void testCRLF2() throws IOException {
		assertArrayEquals("ab".getBytes(), replyLengthTests("ab\r\n"));
	}
}
