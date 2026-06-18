package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;

public class TofuManager {
	public static String getFingerprint(X509Certificate cert) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] der = cert.getEncoded();
		byte[] hash = md.digest(der);
		StringBuilder sb = new StringBuilder();
		for (byte b : hash) sb.append(String.format("%02x", b));
		return sb.toString();
	}

	public static void checkOrSaveTofu(String host, String fingerprint) throws IOException {
		Path knownHosts = Path.of(System.getProperty("user.home"), ".gemini", "known_hosts");
		Files.createDirectories(knownHosts.getParent());

		if (Files.exists(knownHosts)) {
			for (String line : Files.readAllLines(knownHosts)) {
				String[] parts = line.split(" ", 2);
				if (parts.length == 2 && parts[0].equals(host)) {
					if (!parts[1].equals(fingerprint)) {
						System.err.println("Certificate mismatch for " + host + " — possible MITM.");
						System.exit(1);
					}
					return; // known and matches
				}
			}
		}

		// first time seeing this host — save it
		Files.writeString(knownHosts, host + " " + fingerprint + "\n",
			StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}
}
