package process;

import util.FinalVars;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class ReplyManager {
	// starts with 1 => input expected
	public String askInput(String input) {
		if (input == null) {
			var console = System.console();
			if (console != null) {
				input = console.readLine();
			}
		}
		return input;
	}

	// starts with 2 => success => proceed with full reply
	public byte[] processSuccess(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int i;
		while ((i = in.read()) != -1) {
			buffer.write(i);
		}
		return buffer.toByteArray();
	}

	// starts with 3 => redirection
	public URI getRedirect(String meta, URI uri) throws URISyntaxException {
		if (meta.startsWith(FinalVars.SCHEME))
			return new URI(meta);
		else if (meta.isEmpty())
			return new URI("empty");
		else
			return uri.resolve(meta);
	}

	// 44 => sleep
	public void waitAndSleep(String meta) {
		try {
			int millis = Integer.parseInt(meta) * 1000;
			Thread.sleep(millis);
		} catch (NumberFormatException e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException _) {}
		} catch (InterruptedException _) {}
	}

	// starts with 4 => temp failure | starts with 5 => permanent failures
	public void getErrorCodes(String statCode) {
		// Descriptive error messages
		if (statCode.startsWith("4"))
			System.err.println("Temporary Failure");
		else
			System.err.println("Permanent Failure");
	}
}
