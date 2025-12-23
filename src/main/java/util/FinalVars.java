package util;

public class FinalVars {
	public static final int DEFAULT_PORT = 1965;
	public static final int DEFAULT_PROXY_PORT = 9999;
	public static final String PROXY_VARIABLE = "GEMINI_PROXY";
	public static final String SCHEME = "gemini";
	public static final int MAX_REDIRECT_ITERATIONS = 5;
	public static final int MAX_REQUEST_SIZE = 1024;
	// Status Code (2) + Space (1) + Meta (1024)
	public static final int MAX_REPLY_HEADER_SIZE = 1027;
}
