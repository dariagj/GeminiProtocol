package gemini;

import util.UriValidator;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

// Entry point class
public class Client {
    ClientEngine clientEngine = new ClientEngine();

    public static void main(String[] args) throws Throwable {
        if (args.length == 0) {
            System.err.println("Requirement: java -cp target/gemini-2025.jar gemini.Client <URL> [input]");
            System.exit(1);
        }

        URI uri = new URI(args[0]);
        String input = args.length > 1 ? args[1] : null;

        UriValidator uriValidator = new UriValidator();
        if (!uriValidator.verifyUri(uri)) {
            System.err.println("Invalid URI");
            System.exit(1);
        }

        Client client = new Client();
        client.run(uri, input);
    }

    private void run(URI uri, String input) {
        clientEngine.handleConnection(uri, input);
    }
}
