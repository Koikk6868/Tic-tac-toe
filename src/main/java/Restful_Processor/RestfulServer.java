package Restful_Processor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RestfulServer {
    private static final int DEFAULT_PORT = 5000;
    private static final int WAITING_CONNECTION_BACKLOG = 50;

    private final int port;
    private final RequestHandler requestHandler;

    public RestfulServer(int port) {
        this(port, new SocketRequestHandler(
                new StatelessMoveCommandProcessor(
                        new TicTacToeMoveProcessor(new FirstAvailableComputerMoveStrategy()),
                        new GameJsonCodec()
                )
        ));
    }

    private RestfulServer(int port, RequestHandler requestHandler) {
        this.port = port;
        this.requestHandler = requestHandler;
    }

    public static void main(String[] args) {
        try {
            int port = args.length >= 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

            if (args.length > 1) {
                printUsage();
                return;
            }

            new RestfulServer(port).start();
        } catch (NumberFormatException e) {
            printUsage();
        } catch (IOException e) {
            System.out.println("Socket REST server error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java RestfulServer [port]");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port, WAITING_CONNECTION_BACKLOG)) {
            System.out.println("Stateless socket Tic-Tac-Toe server listening on port " + port);
            System.out.println("Protocol: MOVE <compactBoard> <cell>");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    requestHandler.handle(clientSocket);
                }
            }
        }
    }

    private interface RequestHandler {
        void handle(Socket socket) throws IOException;
    }

    private static class SocketRequestHandler implements RequestHandler {
        private final CommandProcessor commandProcessor;

        SocketRequestHandler(CommandProcessor commandProcessor) {
            this.commandProcessor = commandProcessor;
        }

        @Override
        public void handle(Socket socket) throws IOException {
            try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)
            ) {
                out.println(commandProcessor.process(in.readLine()));
            }
        }
    }

    private interface CommandProcessor {
        String process(String request);
    }

    private static class StatelessMoveCommandProcessor implements CommandProcessor {
        private final MoveProcessor moveProcessor;
        private final GameJsonCodec jsonCodec;

        StatelessMoveCommandProcessor(MoveProcessor moveProcessor, GameJsonCodec jsonCodec) {
            this.moveProcessor = moveProcessor;
            this.jsonCodec = jsonCodec;
        }

        @Override
        public String process(String request) {
            try {
                BoardState board = moveProcessor.process(Command.parse(request).toMoveRequest());
                return jsonCodec.board(board);
            } catch (IllegalArgumentException e) {
                return jsonCodec.error(e.getMessage());
            }
        }
    }

    private record Command(String board, int cell) {
        static Command parse(String request) {
            if (request == null || request.isBlank()) {
                throw new IllegalArgumentException("Request cannot be empty.");
            }

            String[] parts = request.trim().split("\\s+");
            if (parts.length != 3 || !"MOVE".equalsIgnoreCase(parts[0])) {
                throw new IllegalArgumentException("Use protocol: MOVE <compactBoard> <cell>.");
            }

            try {
                return new Command(parts[1], Integer.parseInt(parts[2]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("MOVE cell must be a number.");
            }
        }

        MoveRequest toMoveRequest() {
            return new MoveRequest(BoardState.fromCompact(board), cell);
        }
    }
}
