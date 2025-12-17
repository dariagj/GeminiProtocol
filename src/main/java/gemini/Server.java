package gemini;

import process.ServerRequestHandler;
import util.FinalVars;

public class Server {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Requirement: java -cp target/gemini-2025.jar gemini.Server <directory> [port]");
            System.exit(1);
        }

        String directory = args[0];
        int port = args.length > 1? Integer.parseInt(args[1]) : FinalVars.DEFAULT_PORT;

        try {
            ServerRequestHandler fileSystemRequestHandler = new ServerRequestHandler(directory);
            new ServerOrProxyEngine(port, fileSystemRequestHandler).run();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
