package util;

public class ByteValidator {
	public static boolean validateRequestBytes(byte[] in) {
		int b;
		for (int i = 0; i < in.length; i++) {
			b = in[i] & 0xFF;
			if ((b <= 0x1F && b != 0x09)|| b == 0x7F || (b >= 0x80 && b <= 0x9F) || b == 0x5C)
				return false;
		}
		return true;
	}

	public static boolean validateReplyHeaderBytes(byte[] in) {
		if (containsNonAscii(in) && !isValidUtf8Sequence(in)) {
			return false;
		}

		int b;
		for (int i = 0; i < in.length; i++) {
			b = in[i] & 0xFF;
			// C1 rule
			if (b == 0xC2 && (i + 1) < in.length) {
				int next = in[i + 1] & 0xFF;
				if (next >= 0x80 && next <= 0x9F)
					return false;
			}

			if (b <= 0x7F) {	// Single byte ASCII
				if (b == 0x0D || b == 0x0A)
					continue;
				if ((b <= 0x1F && b != 0x20) || b == 0x7F)  // Reject C0 controls and DEL
					return false;
			} else
				if (isStartSequence(b))
					i += getNeeded(b); // Multi-byte UTF-8
				else
					return false;
		}
		return true;
	}

	private static boolean containsNonAscii(byte[] in) {
		for (byte b : in) {
			if ((b & 0x80) != 0)
				return true;
		}
		return false;
	}

	// References:
	// https://datatracker.ietf.org/doc/html/rfc2279
	// https://encoding.spec.whatwg.org/#utf-8-decoder
	// https://www.utf8-chartable.de/ => to check the Unicode code points
	public static boolean isValidUtf8Sequence(byte[] in) {
		int b;
		for (int i = 0; i < in.length; i++) {
			b = in[i] & 0xFF;
			if (b <= 0x7F)
				continue;	// single byte ASCII

			int needed = 0;
			if (isStartSequence(b))	// multi byte utf-8
				needed = getNeeded(b);
			else
				return false; // invalid

			if (needed == -1)
				return false;	// start byte not in the correct range


			if (i + needed >= in.length)
				return false;	// length surpassed

			// Reading continuation bytes
			for (int j = 1; j <= needed; j++) {
				int next = in[i+j] & 0xFF;
				if ((next & 0xC0) != 0x80)
					return false; // end of stream or invalid continuation bytes
			}
			i += needed;
		}
		return true;
	}

	private static boolean isStartSequence(int b) {
		return (b & 0xE0) == 0xC0 || (b & 0xF0) == 0xE0 || (b & 0xF8) == 0xF0;
	}

	private static int getNeeded(int b) {
		if ((b & 0xE0) == 0xC0)  		// start byte is 0xC2-0xDF, 2-byte utf-8
			return (b < 0xC2)? -1: 1;
		else if ((b & 0xF0) == 0xE0)  	// start byte is 0xE0–0xEF, 3-byte utf-8
			return 2;
		else if ((b & 0xF8) == 0xF0)  	// start byte is 0xF0–0xF4, 4-byte utf-8
			return (b > 0xF4)? -1 : 3;
		return -1;
	}
}
