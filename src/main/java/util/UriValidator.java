package util;

import java.net.URI;

public class UriValidator {
	// verify that the request is valid
	public boolean verifyUri(URI uri) {
		UriParser uriParser = new UriParser(uri);
		return isAbsolute(uriParser) && verifyPort(uriParser) && verifyPath(uriParser);
	}

	private boolean verifyScheme(UriParser uriParser) {
		if (uriParser.getScheme() != null)
			return uriParser.getScheme().equalsIgnoreCase(FinalVars.SCHEME);
		return false;
	}

	private boolean verifyHost(UriParser uriParser) {
		if (uriParser.getHost() != null)
			return !uriParser.getHost().isBlank();
		return false;
	}

	public boolean verifyFragment(UriParser uriParser) {
		return uriParser.getFragment() == null;
	}

	private boolean verifyUserInfo(UriParser uriParser) {
		return uriParser.getUserInfo() == null;
	}

	private boolean isAbsolute(UriParser uriParser) {
		return verifyScheme(uriParser)
			&& verifyHost(uriParser)
			&& verifyFragment(uriParser)
			&& verifyUserInfo(uriParser);
	}

	private boolean verifyPort(UriParser uriParser) {
		int port = uriParser.getPortNumber();
		return port <= 65535 && port != 0;
	}

	private boolean verifyPath(UriParser uriParser) {
		return !uriParser.getPath().contains("//"); // path has more than one /
	}
}
