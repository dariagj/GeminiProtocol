package gemini;

import process.ProxyRequestHandler;
import util.FinalVars;

public class Proxy {
	public static void main(String[] args) {
		int port = args.length > 0 ? Integer.parseInt(args[0]) : FinalVars.DEFAULT_PROXY_PORT;

		try {
			ProxyRequestHandler proxyRequestHandler = new ProxyRequestHandler();
			new ServerOrProxyEngine(port, proxyRequestHandler).run();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
}
