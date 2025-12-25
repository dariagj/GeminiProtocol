package process;

import util.FinalVars;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;

public class ReplyManager {
	// starts with 1 => input expected
	public String askInput(String input) {
		if (input == null) {
			var console = System.console();
			if (console != null) {
				input = console.readLine();
			}
		}
		return input;
	}

	// starts with 2 => success => proceed with full reply
	public byte[] processSuccess(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int i;
		while ((i = in.read()) != -1) {
			buffer.write(i);
		}
		return buffer.toByteArray();
	}

	// starts with 3 => redirection
	public URI getRedirect(String meta, URI uri) throws URISyntaxException {
		if (meta.startsWith(FinalVars.SCHEME))
			return new URI(meta);
		else if (meta.isEmpty())
			return new URI("empty");
		else
			return uri.resolve(meta);
	}

	// 44 => sleep
	public void waitAndSleep(String meta) {
		try {
			int millis = Integer.parseInt(meta) * 1000;
			Thread.sleep(millis);
		} catch (NumberFormatException e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException _) {}
		} catch (InterruptedException _) {}
	}

	// starts with 4 => temp failure | starts with 5 => permanent failures
	public void getErrorCodes(String statCode) {
		// Descriptive error messages
		if (statCode.startsWith("4"))
			System.err.println("Temporary Failure");
		else
			System.err.println("Permanent Failure");
	}

	// starts with 6 => certificate issues
	public SSLContext getCertificateAsk(String meta, TrustManager[] trustManagers) {
		System.err.println("Certificate Requested: " + meta);

		String rawPassword = System.getenv(FinalVars.GEMINI_TLS_PASS);
		if (rawPassword == null || rawPassword.isEmpty()) {
			System.err.println("Error: GEMINI_PASS environment variable is not set.");
			System.exit(1);
		}

		char[] password = rawPassword.toCharArray();
		try {
			KeyStore keyStore = KeyStore.getInstance("JKS");

			try (FileInputStream fis = new FileInputStream(FinalVars.GEMINI_TLS_FILE)) {
				keyStore.load(fis, password);
			} catch (IOException | CertificateException | NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, password);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), trustManagers, new java.security.SecureRandom());
			return sslContext;
		} catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}
}
