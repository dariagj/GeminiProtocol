import org.junit.jupiter.api.Test;
import util.ByteValidator;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class IllegalsTests {

	// --- validateRequestBytes ---

	@Test
	void requestValidAscii() {
		assertTrue(ByteValidator.validateRequestBytes("gemini://example.com/".getBytes()));
	}

	@Test
	void requestTabAllowed() {
		assertTrue(ByteValidator.validateRequestBytes("gemini://example.com/\t".getBytes()));
	}

	@Test
	void requestControlCharRejected() {
		assertFalse(ByteValidator.validateRequestBytes(new byte[]{'g', 0x01, 'i'}));
	}

	@Test
	void requestNullByteRejected() {
		assertFalse(ByteValidator.validateRequestBytes(new byte[]{'g', 0x00, 'i'}));
	}

	@Test
	void requestDelRejected() {
		assertFalse(ByteValidator.validateRequestBytes(new byte[]{'g', 0x7F, 'i'}));
	}

	@Test
	void requestC1ByteRejected() {
		assertFalse(ByteValidator.validateRequestBytes(new byte[]{'g', (byte) 0x80, 'i'}));
	}

	@Test
	void requestHighC1ByteRejected() {
		assertFalse(ByteValidator.validateRequestBytes(new byte[]{'g', (byte) 0x9F, 'i'}));
	}

	@Test
	void requestBackslashRejected() {
		assertFalse(ByteValidator.validateRequestBytes("ge\\mini".getBytes()));
	}

	// --- validateReplyHeaderBytes ---

	@Test
	void replyValidAscii() {
		assertTrue(ByteValidator.validateReplyHeaderBytes("20 text/gemini".getBytes()));
	}

	@Test
	void replyControlCharRejected() {
		assertFalse(ByteValidator.validateReplyHeaderBytes(new byte[]{'2', '0', ' ', 0x01, 't'}));
	}

	@Test
	void replyDelRejected() {
		assertFalse(ByteValidator.validateReplyHeaderBytes(new byte[]{'2', '0', ' ', 0x7F}));
	}

	@Test
	void replyValidUtf8TwoByte() {
		// é = U+00E9, valid 2-byte UTF-8, not a control character
		byte[] bytes = "20 héllo".getBytes(StandardCharsets.UTF_8);
		assertTrue(ByteValidator.validateReplyHeaderBytes(bytes));
	}

	@Test
	void replyValidUtf8ThreeByte() {
		// € = U+20AC = 0xE2 0x82 0xAC, valid 3-byte UTF-8, not a control character
		byte[] bytes = new byte[]{'2', '0', ' ', (byte) 0xE2, (byte) 0x82, (byte) 0xAC};
		assertTrue(ByteValidator.validateReplyHeaderBytes(bytes));
	}

	@Test
	void replyInvalidUtf8Rejected() {
		// 0xFF is never valid in UTF-8
		assertFalse(ByteValidator.validateReplyHeaderBytes(new byte[]{'2', '0', ' ', (byte) 0xFF}));
	}

	@Test
	void replyC1ViaUtf8Rejected() {
		// U+0080 encoded as 0xC2 0x80 — a C1 control character
		assertFalse(ByteValidator.validateReplyHeaderBytes(new byte[]{'2', '0', ' ', (byte) 0xC2, (byte) 0x80}));
	}

	@Test
	void replyHighC1ViaUtf8Rejected() {
		// U+009F encoded as 0xC2 0x9F — also a C1 control character
		assertFalse(ByteValidator.validateReplyHeaderBytes(new byte[]{'2', '0', ' ', (byte) 0xC2, (byte) 0x9F}));
	}
}
