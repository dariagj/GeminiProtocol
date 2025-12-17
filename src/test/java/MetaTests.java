import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import process.ReplyHeaderValidator;
import util.CRLFLine;

import static org.junit.jupiter.api.Assertions.*;

public class MetaTests {
	String verifyMeta(String string) throws IOException {
		String header = replyHeader(string);
		return ReplyHeaderValidator.verifyMetaPresence(header);
	}

	String replyHeader(String string) throws IOException {
		return new String(CRLFLine.readCrlfLine(new ByteArrayInputStream(string.getBytes()), 1024), StandardCharsets.UTF_8);
	}

	@Test
	void noMetaInput() throws IOException {
		assertNull(verifyMeta(("10\r\n")));
	}

	@Test
	void yesMetaInput() throws IOException {
		assertEquals("Enter your name", verifyMeta(("10 Enter your name\r\n")));
	}

	@Test
	void noMetaSuccess() throws IOException {
		assertNull(verifyMeta(("20\r\n")));
	}

	@Test
	void yesMetaSuccess() throws IOException {
		assertEquals("text/plain", verifyMeta(("20 text/plain\r\n")));
	}

	@Test
	void noMetaRedirect() throws IOException {
		assertNull(verifyMeta(("30\r\n")));
	}

	@Test
	void yesMetaRedirect() throws IOException {
		assertEquals("\\README.gmi", verifyMeta(("30 \\README.gmi\r\n")));
	}

	@Test
	void noMeta44() throws IOException {
		assertNull(verifyMeta(("44\r\n")));
	}

	@Test
	void yesMeta44() throws IOException {
		assertEquals("15", verifyMeta(("44 15\r\n")));
	}

	@Test
	void noMetaErr4() throws IOException {
		assertNull(verifyMeta(("40\r\n")));
	}

	@Test
	void yesMetaErr4() throws IOException {
		assertEquals("Temp Err", verifyMeta(("40 Temp Err\r\n")));
	}

	@Test
	void noMetaErr5() throws IOException {
		assertNull(verifyMeta(("50\r\n")));
	}

	@Test
	void yesMetaErr5() throws IOException {
		assertEquals("Bad Request", verifyMeta(("59 Bad Request\r\n")));
	}

	// Checking for Error validation
	@Test
	void checkErrorNo() throws IOException {
		Integer statCode = ReplyHeaderValidator.verifyStatCode(replyHeader("20 text/plain\r\n"));
		assertFalse(ReplyHeaderValidator.isErrorOrRedirect(statCode.toString()));
	}

	// TODO: Double check if it needs to have meta or not
	@Test
	void checkError43() throws IOException {
		Integer statCode = ReplyHeaderValidator.verifyStatCode(replyHeader("43 proxy\r\n"));
		assertFalse(ReplyHeaderValidator.isErrorOrRedirect(statCode.toString()));
	}

	@Test
	void checkError44() throws IOException {
		Integer statCode = ReplyHeaderValidator.verifyStatCode(replyHeader("44 15\r\n"));
		assertFalse(ReplyHeaderValidator.isErrorOrRedirect(statCode.toString()));
	}

	@Test
	void checkError40() throws IOException {
		Integer statCode = ReplyHeaderValidator.verifyStatCode(replyHeader("40 Temp Err\r\n"));
		assertTrue(ReplyHeaderValidator.isErrorOrRedirect(statCode.toString()));
	}

	@Test
	void checkError50() throws IOException {
		Integer statCode = ReplyHeaderValidator.verifyStatCode(replyHeader("50 Not Found\r\n"));
		assertTrue(ReplyHeaderValidator.isErrorOrRedirect(statCode.toString()));
	}

	@Test
	void checkError51() throws IOException {
		Integer statCode = ReplyHeaderValidator.verifyStatCode(replyHeader("51\r\n"));
		assertTrue(ReplyHeaderValidator.isErrorOrRedirect(statCode.toString()));
	}

	@Test
	void checkError59() throws IOException {
		Integer statCode = ReplyHeaderValidator.verifyStatCode(replyHeader("59 Bad Request\r\n"));
		assertTrue(ReplyHeaderValidator.isErrorOrRedirect(statCode.toString()));
	}
}