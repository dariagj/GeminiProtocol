package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CRLFLine {
	// Checks if reply ends with CRLF terminator, is less than or equal to n bytes
	public static byte[] readCrlfLine(InputStream in, int limitBytes) throws IOException {
		int count = 0;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		while (true) {
			int b = in.read();
			if (b == -1)
				return null; // end of stream

			// CRLF Validation
			if (b == '\n')
				return null; // bare linefeed
			if (b == '\r') {
				if ((in.read()) != '\n')
					return null; // CR without LF
				return buffer.toByteArray();
			}

			// Length validation
			if (count >= limitBytes)
				return null; // too long

			count++;
			buffer.write(b);
		}
	}
}
