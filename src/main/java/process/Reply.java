package process;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

// Deals with replies
public class Reply {
	private final int statCode;
	private final String meta;
	private final byte[] body;

	public Reply(int statCode, String meta) {
		this.statCode = statCode;
		this.meta = meta;
		this.body = null;
	}

	public Reply(int statCode, String meta, byte[] body) {
		this.statCode = statCode;
		this.meta = meta;
		this.body = body;
	}

	private byte[] buildReplyHeader() {
		return (statCode + " " + meta + "\r\n").getBytes(StandardCharsets.UTF_8);
	}

	private byte[] buildReplyWithBody() {
		if (body != null) {
			byte[] header = buildReplyHeader();
			byte[] result = new byte[header.length + body.length];

			System.arraycopy(header, 0, result, 0, header.length);
			System.arraycopy(body, 0, result, header.length, body.length);

			return result;
		}
		return buildReplyHeader();
	}

	public void deliverReply(OutputStream output) {
		try {
			if (this.body == null) {
				output.write(buildReplyHeader());
			} else {
				output.write(buildReplyWithBody());
			}
			output.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
