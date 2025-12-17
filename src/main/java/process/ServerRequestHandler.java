package process;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ServerRequestHandler implements RequestHandler {
	String rootDirectory;

	public ServerRequestHandler(String dir) {
		this.rootDirectory = dir;
	}

	@Override
	public Reply handleRequest(Request request) {
		// from https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URLDecoder.html
		// had to make sure path is in UTF_8 form so later I can check for "invalid" requests
		String path = URLDecoder.decode(request.getPath(), StandardCharsets.UTF_8);
		if (path == null || path.isEmpty())
			path = "/";
		path = path.toLowerCase();
		if (path.contains("%20"))
			path = path.replace("%20", " ");
		return dealWithPath(rootDirectory, path);
	}

	private static Reply dealWithPath(String rootDirectory, String path) {
		try {
			File file = new File(rootDirectory, path);

			// Checking if file is in the directory and is not an attack
			File canonicalFile = file.getCanonicalFile();
			File canonicalDir = new File(rootDirectory).getCanonicalFile();
			String dir = canonicalDir.getPath().toLowerCase();
			if (!canonicalFile.getPath().toLowerCase().startsWith(dir))
				return new Reply(51, "Not Found");

			// TODO: mention in report
			if (file.isDirectory()) {
				File geminiIndex = new File(file, "index.gmi");
				File plainIndex = new File(file, "index.txt");
				if (!geminiIndex.exists() && !plainIndex.exists())
					return new Reply(51, "Not Found");
				if (geminiIndex.exists())
					file = geminiIndex;
				else
					file = plainIndex;
			}

			File parent = file.getParentFile();
			File caseInsensitiveFile = findFileWithNoRegardToCase(parent, file);
			if (caseInsensitiveFile == null)
				return new Reply(51, "Not Found");

			file = caseInsensitiveFile;
			String mime = getMimeType(file);
			byte[] body = Files.readAllBytes(file.toPath());
			return new Reply(20, mime, body);
		} catch (Exception e) {
			e.printStackTrace();
			return new Reply(50, "Internal Server Error");
		}
	}

	private static File findFileWithNoRegardToCase(File directory, File file) {
		String currentFile = file.getName();
		File[] currentDirectory = directory.listFiles();
		if (currentDirectory == null)
			return null;

		for (File f : currentDirectory) {
			if (f.getName().equalsIgnoreCase(currentFile)) {
				return f;
			}
		}

		return null;
	}

	// Use mimeType classes
	private static String getMimeType(File file) {
		String name = file.toString().toLowerCase();
		if (name.endsWith(".gmi"))
			return "text/gemini";
		else if (name.endsWith(".txt"))
			return "text/plain";
		else
			return "application/octet-stream";
	}
}
