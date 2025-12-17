package util;

import java.net.URI;

public class UriParser {
	private String scheme;
	private String host;
	private int uriPort;
	private String path;
	private String queryString;
	private String fragment;
	private String userInfo;

	public UriParser(URI uri) {
		this.scheme = uri.getScheme();
		this.host = uri.getHost();
		this.uriPort = (uri.getPort() == -1 ? FinalVars.DEFAULT_PORT : uri.getPort());
		this.path = uri.getPath();
		this.queryString = uri.getQuery();
		this.fragment = uri.getFragment();
		this.userInfo = uri.getUserInfo();
	}

	public String getScheme() {
		return scheme;
	}

	public String getHost() {
		return host;
	}

	public int getPortNumber() {
		return uriPort;
	}

	public String getPath() {
		return path;
	}

	public String getQueryString() {
		return queryString;
	}

	public String getFragment() {
		return fragment;
	}

	public String getUserInfo() {
		return userInfo;
	}
}
