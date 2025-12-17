package gemini;

import process.*;
import util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;

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
		try (final var server = new ServerSocket(port)) {
            System.err.println("Listening on port " + port);
			while (true) {
				final var socket = server.accept();
				handleConnection(socket);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handleConnection(Socket socket) {
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
