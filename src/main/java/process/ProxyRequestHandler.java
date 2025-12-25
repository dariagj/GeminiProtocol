package process;

import util.ByteValidator;
import util.CRLFLine;
import util.FinalVars;

import javax.net.ssl.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

public class ProxyRequestHandler implements RequestHandler {
	private final ReplyManager replyManager = new ReplyManager();

	@Override
	public Reply handleRequest(Request request) {
		try {
			URI uri = request.getURI();
			int redirections = 0;

			while (true) {
				if (redirections > FinalVars.MAX_REDIRECT_ITERATIONS)
					return new Reply(43, "Proxy Error: Too many redirects.");

				String host = uri.getHost();
				int uriPort = uri.getPort() < 0 ? FinalVars.DEFAULT_PORT : uri.getPort();

				String proxyConnection = System.getenv(FinalVars.PROXY_VARIABLE);
				if (proxyConnection != null && !proxyConnection.isEmpty()) {
					String[] proxyVarParts = proxyConnection.split(":", 2);
					if (proxyVarParts.length != 2)
						return new Reply(43, "Proxy Error: Invalid GEMINI_PROXY format");

					host = proxyVarParts[0];
					try {
						uriPort = Integer.parseInt(proxyVarParts[1]);
					} catch (NumberFormatException e) {
						return new Reply(43, "Proxy Error: Invalid port in GEMINI_PROXY");
					}
				}

				TrustManager[] trustAllCerts = new TrustManager[] {
					new X509TrustManager() {
						public void checkClientTrusted(X509Certificate[] chain, String authType) {}
						public void checkServerTrusted(X509Certificate[] chain, String authType) {}
						public X509Certificate[] getAcceptedIssuers() { return null; }
					}
				};
				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());

				SSLSocketFactory ssf = sc.getSocketFactory();
				try (SSLSocket socket = (SSLSocket) ssf.createSocket(host, uriPort)) {
					socket.startHandshake();

					InputStream socketInput = socket.getInputStream();
					OutputStream socketOutput = socket.getOutputStream();

					socketOutput.write((uri + "\r\n").getBytes());
					socketOutput.flush();

					byte[] replyLineBytes = CRLFLine.readCrlfLine(socketInput, FinalVars.MAX_REPLY_HEADER_SIZE);
					if (replyLineBytes == null)
						return new Reply(43, "Proxy Error: Invalid reply header, either not correct crlf or longer length.");

					// Checking if header has invalid bytes
					if (!ByteValidator.validateReplyHeaderBytes(replyLineBytes))
						return new Reply(43, "Proxy Error: Invalid header, illegal bytes.");

					String header = new String(replyLineBytes, StandardCharsets.UTF_8);
					Integer statCodeInt = ReplyHeaderValidator.verifyStatCode(header);
					if (statCodeInt == null)
						return new Reply(43, "Proxy Error: Invalid status code, either not in range 10-69 or no space between it and meta");
					String statCode = String.valueOf(statCodeInt);

					String meta = ReplyHeaderValidator.verifyMetaPresence(header);
					boolean error = ReplyHeaderValidator.isErrorOrRedirect(statCode);
					if (meta == null) {
						if (!error)
							return new Reply(43, "Proxy Error: Missing Meta.");
						else
							meta = "";
					}

					if (statCode.startsWith("1")) {
						return new Reply(statCodeInt, meta);
					} else if (statCode.startsWith("2")) {
						if (ReplyHeaderValidator.verifyMimeType(meta)) {
							byte[] body = replyManager.processSuccess(socketInput);
							return new Reply(statCodeInt, meta, body);
						} else
							return new Reply(43, "Proxy Error: Invalid mime type");
					} else if (statCode.startsWith("3")) {
						redirections += 1;
						uri = replyManager.getRedirect(meta, uri);
					} else if (statCode.equals("44")) {
						replyManager.waitAndSleep(meta);
					} else if (statCode.startsWith("4") || statCode.startsWith("5")) {    // handles 43 as well
						return new Reply(statCodeInt, meta);
					} else if (statCode.startsWith("6")) {
						return new Reply(statCodeInt, meta);
					} else {
						return new Reply(43, "Proxy Error: Unexpected status code " + statCode);
					}
				} catch (Throwable e) {
					return new Reply(43, "Proxy Error: Connection to target server failed.");
				}
			}
		} catch (Exception e) {
			return new Reply(43, "Proxy Error: URI Exception.");
		}
	}
}
