package Single_Processor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SingleClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;
    private static final String INPUT_PROMPT = "Player#1's turn: ";

    public static void main(String[] args) {
        try {
            String host = args.length >= 1 ? args[0] : DEFAULT_HOST;
            int port = args.length >= 2 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

            if (args.length > 2) {
                printUsage();
                return;
            }

            new SingleClient().start(host, port);
        } catch (NumberFormatException e) {
            printUsage();
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java GameClient [host] [port]");
    }

    public void start(String host, int port) throws IOException {
        try (
            Socket socket = new Socket(host, port);
            BufferedReader keyboard = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8));
            PrintWriter serverOut = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            readServerOutput(socket.getInputStream(), keyboard, serverOut);
        }
    }

    private void readServerOutput(
            InputStream serverIn,
            BufferedReader keyboard,
            PrintWriter serverOut
    ) throws IOException {
        StringBuilder latestOutput = new StringBuilder(INPUT_PROMPT.length());

        int value;
        while ((value = serverIn.read()) != -1) {
            System.out.write(value);
            System.out.flush();
            rememberLatestOutput(latestOutput, value);

            if (isWaitingForUserMove(latestOutput)) {
                sendKeyboardInput(keyboard, serverOut);
                latestOutput.setLength(0);
            }
        }
    }

    private void rememberLatestOutput(StringBuilder latestOutput, int value) {
        latestOutput.append((char) value);
        if (latestOutput.length() > INPUT_PROMPT.length()) {
            latestOutput.deleteCharAt(0);
        }
    }

    private boolean isWaitingForUserMove(StringBuilder latestOutput) {
        return latestOutput.toString().equals(INPUT_PROMPT);
    }

    private void sendKeyboardInput(BufferedReader keyboard, PrintWriter serverOut) throws IOException {
        String input = keyboard.readLine();
        serverOut.println(input == null ? "q" : input);
        if (serverOut.checkError()) {
            throw new IOException("Unable to send input to server.");
        }
    }
}
