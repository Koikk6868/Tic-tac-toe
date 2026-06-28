package Single_Processor;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import TTTGame.Game;

public class SingleServer {
    private static final int DEFAULT_PORT = 5000;
    private static final int WAITING_CONNECTION_BACKLOG = 50;
    private static final int USER_STARTS = 1;

    private final int port;
    private final GameSessionFactory sessionFactory;

    public SingleServer(int port) {
        this(port, NetworkGameSession::new);
    }

    private SingleServer(int port, GameSessionFactory sessionFactory) {
        this.port = port;
        this.sessionFactory = sessionFactory;
    }

    public static void main(String[] args) {
        try {
            int port = args.length >= 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

            if (args.length > 1) {
                printUsage();
                return;
            }

            new SingleServer(port).start();
        } catch (NumberFormatException e) {
            printUsage();
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java GameServer [port]");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port, WAITING_CONNECTION_BACKLOG)) {
            System.out.println("Tic-Tac-Toe server listening on port " + port);
            System.out.println("The server runs continuously. Waiting clients are handled one game at a time.");

            while (true) {
                waitForNextClient(serverSocket).play();
            }
        }
    }

    private GameSession waitForNextClient(ServerSocket serverSocket) throws IOException {
        System.out.println("Waiting for the next client...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
        return sessionFactory.create(clientSocket);
    }

    private interface GameSession {
        void play() throws IOException;
    }

    private interface GameSessionFactory {
        GameSession create(Socket socket) throws IOException;
    }

    private static class NetworkGameSession implements GameSession {
        private final Socket socket;

        NetworkGameSession(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void play() throws IOException {
            try (
                Socket clientSocket = socket;
                PrintStream clientOut = new PrintStream(
                        clientSocket.getOutputStream(), true, StandardCharsets.UTF_8)
            ) {
                sendGreeting(clientOut);
                new Game(USER_STARTS, clientSocket.getInputStream(), clientOut).play();
            } catch (NoSuchElementException e) {
                System.out.println("Client disconnected before the game finished.");
            }
        }

        private void sendGreeting(PrintStream clientOut) {
            clientOut.println("Connected to Tic-Tac-Toe server.");
            clientOut.println("You are Player#1. The server computer is Player#2.");
            clientOut.println("Type 1-9 to move, or q to quit.");
        }
    }
}
