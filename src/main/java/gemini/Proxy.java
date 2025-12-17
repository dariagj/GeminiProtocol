package gemini;

import process.ProxyRequestHandler;
import util.FinalVars;

public class Proxy {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Requirement: java -cp target/gemini-2025.jar gemini.Proxy <port>");
			System.exit(1);
		}

		int port = args[0] != null? Integer.parseInt(args[0]) : FinalVars.DEFAULT_PROXY_PORT;

		try {
			ProxyRequestHandler proxyRequestHandler = new ProxyRequestHandler();
			new ServerOrProxyEngine(port, proxyRequestHandler).run();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
}
