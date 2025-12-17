package process;

import java.net.URI;
import java.net.URISyntaxException;

import util.UriParser;

// Deals with requests
public class Request {
	private final String path;
	private URI uri;

	public Request(URI uri) {
		UriParser uriParser = new UriParser(uri);
		this.path = uriParser.getPath();
		this.uri = uri;
	}

	public String getPath() {
		return path;
	}

	public URI getURI() throws URISyntaxException {
//		if (uri.getFragment() != null)
//			return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), null, null);
		return uri;
	}
}
