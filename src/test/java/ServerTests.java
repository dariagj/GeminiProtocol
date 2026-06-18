import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import process.Reply;
import process.Request;
import process.ServerRequestHandler;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTests {

	@TempDir
	Path tempDir;

	private Reply handle(String uriPath) throws Exception {
		ServerRequestHandler handler = new ServerRequestHandler(tempDir.toString());
		Request request = new Request(new URI("gemini://localhost" + uriPath));
		return handler.handleRequest(request);
	}

	private String replyHeader(Reply reply) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		reply.deliverReply(baos);
		return baos.toString().split("\r\n", 2)[0];
	}

	private byte[] replyBody(Reply reply) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		reply.deliverReply(baos);
		byte[] all = baos.toByteArray();
		for (int i = 0; i < all.length - 1; i++) {
			if (all[i] == '\r' && all[i + 1] == '\n') {
				byte[] body = new byte[all.length - i - 2];
				System.arraycopy(all, i + 2, body, 0, body.length);
				return body;
			}
		}
		return new byte[0];
	}

	@Test
	void gmiFileReturns20WithGeminiMime() throws Exception {
		Files.writeString(tempDir.resolve("page.gmi"), "# Hello");
		assertTrue(replyHeader(handle("/page.gmi")).startsWith("20 text/gemini"));
	}

	@Test
	void txtFileReturns20WithPlainMime() throws Exception {
		Files.writeString(tempDir.resolve("readme.txt"), "Hello");
		assertTrue(replyHeader(handle("/readme.txt")).startsWith("20 text/plain"));
	}

	@Test
	void otherExtensionReturns20WithOctetStream() throws Exception {
		Files.write(tempDir.resolve("data.bin"), new byte[]{1, 2, 3});
		assertTrue(replyHeader(handle("/data.bin")).startsWith("20 application/octet-stream"));
	}

	@Test
	void fileContentsAreReturned() throws Exception {
		Files.writeString(tempDir.resolve("page.gmi"), "# Hello");
		assertArrayEquals("# Hello".getBytes(), replyBody(handle("/page.gmi")));
	}

	@Test
	void missingFileReturns51() throws Exception {
		assertTrue(replyHeader(handle("/missing.gmi")).startsWith("51"));
	}

	@Test
	void directoryWithGmiIndexReturns20() throws Exception {
		Path sub = Files.createDirectory(tempDir.resolve("sub"));
		Files.writeString(sub.resolve("index.gmi"), "# Index");
		assertTrue(replyHeader(handle("/sub")).startsWith("20 text/gemini"));
	}

	@Test
	void directoryWithTxtIndexReturns20() throws Exception {
		Path sub = Files.createDirectory(tempDir.resolve("sub"));
		Files.writeString(sub.resolve("index.txt"), "Index");
		assertTrue(replyHeader(handle("/sub")).startsWith("20 text/plain"));
	}

	@Test
	void directoryPrefersGmiOverTxt() throws Exception {
		Path sub = Files.createDirectory(tempDir.resolve("sub"));
		Files.writeString(sub.resolve("index.gmi"), "# Gemini");
		Files.writeString(sub.resolve("index.txt"), "Plain");
		assertTrue(replyHeader(handle("/sub")).startsWith("20 text/gemini"));
	}

	@Test
	void directoryWithNoIndexReturns51() throws Exception {
		Files.createDirectory(tempDir.resolve("empty"));
		assertTrue(replyHeader(handle("/empty")).startsWith("51"));
	}

	@Test
	void pathTraversalReturns51() throws Exception {
		assertTrue(replyHeader(handle("/../secret")).startsWith("51"));
	}

	@Test
	void caseInsensitiveLookup() throws Exception {
		Files.writeString(tempDir.resolve("page.gmi"), "# Hello");
		assertTrue(replyHeader(handle("/PAGE.GMI")).startsWith("20"));
	}
}
