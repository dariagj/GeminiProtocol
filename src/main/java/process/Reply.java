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
			int headerLength = buildReplyHeader().length;
			int bodyLength = body.length;
			byte[] resultReply = new byte[headerLength + bodyLength];

			System.arraycopy(buildReplyHeader(), 0, resultReply, 0, headerLength);
			System.arraycopy(body, 0, resultReply, headerLength, bodyLength);

			return resultReply;
		}
		return buildReplyHeader();
	}

//	public byte[] getBody() {
//		return body;
//	}

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
