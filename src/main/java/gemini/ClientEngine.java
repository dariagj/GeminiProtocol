package gemini;

import process.*;
import util.ByteValidator;
import util.CRLFLine;
import util.FinalVars;
import util.UriValidator;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;

// Engine class: handles URL loop (+ redirect # detection) and exits, additionally delegates reply/request
public class ClientEngine {
	private ReplyManager replyManager = new ReplyManager();

	public void handleConnection(URI uri, String input) {
		int redirections = 0;
		String host = uri.getHost();
		int uriPort = uri.getPort() == -1 ? FinalVars.DEFAULT_PORT : uri.getPort();

		while (true) {
			if (redirections >= FinalVars.MAX_REDIRECT_ITERATIONS) {
				System.err.println("5 redirections already attempted.");
				System.exit(1);
			}

			UriValidator uriValidator = new UriValidator();
			if (!uriValidator.verifyUri(uri)) {
				System.err.println("Invalid URI");
				System.exit(1);
			}

			String proxyConnection = System.getenv(FinalVars.PROXY_VARIABLE);
			if (proxyConnection != null && !proxyConnection.isEmpty()) {
				String[] proxyVarParts = proxyConnection.split(":", 2);
				if (proxyVarParts.length != 2) {
					System.err.println("Requirement: GEMINI_LITE_PROXY format => hostname:port");
					System.exit(1);
				}
				host = proxyVarParts[0];
				try {
					uriPort = Integer.parseInt(proxyVarParts[1]);
				} catch (NumberFormatException e) {
					System.err.println("Invalid port in GEMINI_LITE_PROXY: " + proxyVarParts[1]);
					System.exit(1);
				}
			}

			try (Socket socket = new Socket(host, uriPort)) {
				InputStream socketInput = socket.getInputStream();
				OutputStream socketOutput = socket.getOutputStream();

				String request = uri.toASCIIString() + "\r\n";
				socketOutput.write(request.getBytes());
				socketOutput.flush();

				byte[] replyLineBytes = CRLFLine.readCrlfLine(socketInput, FinalVars.MAX_REPLY_HEADER_SIZE);
				if (replyLineBytes == null) {
					System.err.println("Invalid Header. Issues may be: surpassing the allowed length or the terminator is not in CRLF format.");
					System.exit(1);
				}

				// Checking if header has invalid bytes
				if (!ByteValidator.validateReplyHeaderBytes(replyLineBytes)) {
					System.err.println("Illegal bytes in reply");
					System.exit(1);
				}

				String header = new String(replyLineBytes, StandardCharsets.UTF_8);
				Integer statCodeInt = ReplyHeaderValidator.verifyStatCode(header);
				if (statCodeInt == null) {
					System.err.println("Invalid Status Code. Issues may be: not being a two digit number, not being in the valid (10-59) range, or no space after the first two characters of the header.");
					System.exit(1);
				}
				String statCode = String.valueOf(statCodeInt);

				String meta = ReplyHeaderValidator.verifyMetaPresence(header);
				boolean error = ReplyHeaderValidator.isErrorOrRedirect(statCode);
				if (meta == null && !error) {
					System.err.println("Invalid Meta. Prompt/Meta/Time is missing");
					System.exit(1);
				}

				if (statCode.startsWith("1")) {
					System.err.println(meta);
					input = replyManager.askInput(input);
					uri = new URI(uri.getScheme(), uri.getHost(), uri.getPath(), input, null);
				} else if (statCode.startsWith("2") && meta != null && !meta.isBlank()) {
					byte[] body = replyManager.processSuccess(socketInput);
					System.out.write(body);
					System.out.flush();
					System.exit(0);
				} else if (statCode.startsWith("3")) {
					redirections += 1;
					if (redirections > FinalVars.MAX_REDIRECT_ITERATIONS) {
						System.err.println("5 redirections already attempted.");
						System.exit(1);
					}
					uri = replyManager.getRedirect(meta, uri);
					if (uri.toString().equals("empty")) {
						System.err.println("Redirection with no path");
						System.exit(Integer.parseInt(statCode));
					}
				} else if (statCode.equals("44")) {
					replyManager.waitAndSleep(meta);
				} else if (statCode.startsWith("4") || statCode.startsWith("5")) {
					System.err.println(header);
					replyManager.getErrorCodes(statCode);
					System.exit(Integer.parseInt(statCode));
				} else
					System.exit(1);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}
}
