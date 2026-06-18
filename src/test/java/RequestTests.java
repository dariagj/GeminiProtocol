import org.junit.jupiter.api.Test;
import util.UriValidator;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class RequestTests {

	UriValidator validator = new UriValidator();

	@Test
	void validUri() throws Exception {
		assertTrue(validator.verifyUri(new URI("gemini://example.com/")));
	}

	@Test
	void validUriNoPath() throws Exception {
		assertTrue(validator.verifyUri(new URI("gemini://example.com")));
	}

	@Test
	void validUriWithQuery() throws Exception {
		assertTrue(validator.verifyUri(new URI("gemini://example.com/?q=hello")));
	}

	@Test
	void validUriNonDefaultPort() throws Exception {
		assertTrue(validator.verifyUri(new URI("gemini://example.com:1966/")));
	}

	@Test
	void wrongSchemeRejected() throws Exception {
		assertFalse(validator.verifyUri(new URI("https://example.com/")));
	}

	@Test
	void fragmentRejected() throws Exception {
		URI uri = new URI("gemini", null, "example.com", -1, "/page", null, "section");
		assertFalse(validator.verifyUri(uri));
	}

	@Test
	void userinfoRejected() throws Exception {
		URI uri = new URI("gemini", "user", "example.com", -1, "/", null, null);
		assertFalse(validator.verifyUri(uri));
	}

	@Test
	void portZeroRejected() throws Exception {
		assertFalse(validator.verifyUri(new URI("gemini://example.com:0/")));
	}

	@Test
	void portOutOfRangeRejected() throws Exception {
		assertFalse(validator.verifyUri(new URI("gemini://example.com:99999/")));
	}

	@Test
	void doubleSlashInPathRejected() throws Exception {
		assertFalse(validator.verifyUri(new URI("gemini://example.com//bad")));
	}

	@Test
	void emptyHostRejected() throws Exception {
		// gemini:///path has an empty authority, so host is ""
		assertFalse(validator.verifyUri(new URI("gemini:///path")));
	}

	@Test
	void schemeIsCaseInsensitive() throws Exception {
		assertTrue(validator.verifyUri(new URI("GEMINI://example.com/")));
	}
}
