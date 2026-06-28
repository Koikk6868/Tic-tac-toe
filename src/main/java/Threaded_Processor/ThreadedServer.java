package Threaded_Processor;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import TTTGame.Game;

public class ThreadedServer {
    private static final int DEFAULT_PORT = 5000;
    private static final int WAITING_CONNECTION_BACKLOG = 50;
    private static final int USER_STARTS = 1;
    private static final int DEFAULT_POOL_SIZE = 4;

    private final int port;
    private final int poolSize;
    private final ExecutorService gameExecutor;
    private final GameSessionFactory sessionFactory;

    public ThreadedServer(int port, int poolSize) {
        this(port, poolSize, Executors.newFixedThreadPool(poolSize), NetworkGameSession::new);
    }

    private ThreadedServer(
            int port,
            int poolSize,
            ExecutorService gameExecutor,
            GameSessionFactory sessionFactory
    ) {
        this.port = port;
        this.poolSize = poolSize;
        this.gameExecutor = gameExecutor;
        this.sessionFactory = sessionFactory;
    }

    public static void main(String[] args) {
        try {
            int port = args.length >= 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
            int poolSize = args.length >= 2 ? Integer.parseInt(args[1]) : DEFAULT_POOL_SIZE;

            if (args.length > 2 || poolSize < 1) {
                printUsage();
                return;
            }

            new ThreadedServer(port, poolSize).start();
        } catch (NumberFormatException e) {
            printUsage();
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java ThreadedServer [port] [poolSize]");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port, WAITING_CONNECTION_BACKLOG)) {
            System.out.println("Threaded Tic-Tac-Toe server listening on port " + port);
            System.out.println("Game thread pool size: " + poolSize);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
                gameExecutor.execute(sessionFactory.create(clientSocket));
            }
        } finally {
            gameExecutor.shutdown();
        }
    }

    private interface GameSessionFactory {
        Runnable create(Socket socket);
    }

    private static class NetworkGameSession implements Runnable {
        private final Socket socket;

        NetworkGameSession(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                Socket clientSocket = socket;
                PrintStream clientOut = new PrintStream(
                        clientSocket.getOutputStream(), true, StandardCharsets.UTF_8)
            ) {
                sendGreeting(clientOut);
                new Game(USER_STARTS, clientSocket.getInputStream(), clientOut).play();
            } catch (NoSuchElementException e) {
                System.out.println("Client disconnected before the game finished.");
            } catch (IOException e) {
                System.out.println("Game session error: " + e.getMessage());
            }
        }

        private void sendGreeting(PrintStream clientOut) {
            clientOut.println("Connected to threaded Tic-Tac-Toe server.");
            clientOut.println("You are Player#1. The server computer is Player#2.");
            clientOut.println("Type 1-9 to move, or q to quit.");
        }
    }
}
