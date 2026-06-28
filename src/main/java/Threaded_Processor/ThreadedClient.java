package Threaded_Processor;
import java.io.IOException;

import Single_Processor.SingleClient;

public class ThreadedClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) {
        try {
            String host = args.length >= 1 ? args[0] : DEFAULT_HOST;
            int port = args.length >= 2 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

            if (args.length > 2) {
                printUsage();
                return;
            }

            new ThreadedClient().start(host, port);
        } catch (NumberFormatException e) {
            printUsage();
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java ThreadedClient [host] [port]");
    }

    public void start(String host, int port) throws IOException {
        new SingleClient().start(host, port);
    }
}
