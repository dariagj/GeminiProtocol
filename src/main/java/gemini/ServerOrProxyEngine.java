package gemini;

import process.*;
import util.*;

import java.io.*;
import java.net.URI;
import java.net.Socket;
import javax.net.ssl.*;
import java.security.KeyStore;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;

public class ServerOrProxyEngine {
	int port;
	RequestHandler requestHandler;

	public ServerOrProxyEngine(int port, ProxyRequestHandler reqHand) {
		this.port = port;
		this.requestHandler = reqHand;
	}

	public ServerOrProxyEngine(int port, ServerRequestHandler reqHand) {
		this.port = port;
		this.requestHandler = reqHand;
	}

	public void run() {
		String rawPassword = System.getenv(FinalVars.GEMINI_TLS_PASS);
		if (rawPassword == null || rawPassword.isEmpty()) {
			System.err.println("Error: GEMINI_PASS environment variable is not set.");
			System.exit(1);
		}

		char[] password = rawPassword.toCharArray();

		SSLContext sslContext;
		try {
			KeyStore keyStore = KeyStore.getInstance("JKS");

			try (FileInputStream fis = new FileInputStream(FinalVars.GEMINI_TLS_FILE)) {
				keyStore.load(fis, password);
			} catch (IOException | CertificateException | NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, password);

			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), null, null);
		} catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
			throw new RuntimeException(e);
		}

		try {
			SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();

			try (SSLServerSocket server = (SSLServerSocket) ssf.createServerSocket(port)) {
				System.err.println("Gemini Server listening on TLS port " + port);
				server.setWantClientAuth(true);

				while (true) {
					final SSLSocket socket = (SSLSocket) server.accept();
					handleConnection(socket);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handleConnection(SSLSocket socket) {
		try (socket) {
			OutputStream o = socket.getOutputStream();
			InputStream i = socket.getInputStream();

			Reply reply;

			try {
				byte[] line = CRLFLine.readCrlfLine(i, FinalVars.MAX_REQUEST_SIZE);
				if (line == null || line.length < 1) {
					reply = new Reply(59, "Bad Request");
					reply.deliverReply(o);
					return;	// request doesn't abide by the crlf and/or length rules
				}

				if (!ByteValidator.validateRequestBytes(line)) {
					reply = new Reply(59, "Bad Request");
					reply.deliverReply(o);
					return;	// request has invalid bytes in it
				}

				String requestLine = new String(line, StandardCharsets.UTF_8);
				URI uri;
				try {
					uri = new URI(requestLine);
				} catch (Exception e) {
					reply = new Reply(59, "Bad Request");
					reply.deliverReply(o);
					return;	// checking uri correctness
				}

				UriValidator uriValidator = new UriValidator();
				if (!uriValidator.verifyUri(uri)) {
					reply = new Reply(59, "Bad Request");
					reply.deliverReply(o);
					return;	// checking uri validity
				}

				if (!uriValidator.verifyFragment(new UriParser(uri))) {
					reply = new Reply(59, "Fragment in URI");
					reply.deliverReply(o);
					return;	// checking fragment presence
				}

				Request req = new Request(uri);
				reply = requestHandler.handleRequest(req);
			} catch (Throwable e) {
				System.err.println("Error handling request: " + e.getMessage());
				e.printStackTrace();
				reply = new Reply(50, "Internal Server Error");
			}

			reply.deliverReply(o);
		} catch (IOException e) {
			System.err.println("Connection error: " + e.getMessage());
			try {
				new Reply(50, "Connection Error").deliverReply(socket.getOutputStream());
			} catch (IOException ignore) {
			}
		}
	}
}
