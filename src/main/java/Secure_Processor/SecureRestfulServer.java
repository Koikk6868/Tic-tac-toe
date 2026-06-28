package Secure_Processor;

import Restful_Processor.BoardState;
import Restful_Processor.FirstAvailableComputerMoveStrategy;
import Restful_Processor.GameJsonCodec;
import Restful_Processor.MoveProcessor;
import Restful_Processor.MoveRequest;
import Restful_Processor.TicTacToeMoveProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SecureRestfulServer {
    private static final int DEFAULT_PORT = 5001;
    private static final int WAITING_CONNECTION_BACKLOG = 50;
    private static final String DEFAULT_TOKEN_SECRET = "change-this-secret";
    private static final String TOKEN_SECRET_ENV = "TTT_TOKEN_SECRET";

    private final int port;
    private final RequestHandler requestHandler;

    public SecureRestfulServer(int port) {
        this(port, defaultRequestHandler());
    }

    private SecureRestfulServer(int port, RequestHandler requestHandler) {
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

            new SecureRestfulServer(port).start();
        } catch (NumberFormatException e) {
            printUsage();
        } catch (IOException e) {
            System.out.println("Secure socket REST server error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java Secure_Processor.SecureRestfulServer [port]");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port, WAITING_CONNECTION_BACKLOG)) {
            System.out.println("Secure stateless socket Tic-Tac-Toe server listening on port " + port);
            System.out.println("Protocol: START | MOVE <compactBoard> <cell> <token>");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    requestHandler.handle(clientSocket);
                }
            }
        }
    }

    private static RequestHandler defaultRequestHandler() {
        SecureGameJsonCodec jsonCodec = new SecureGameJsonCodec(new GameJsonCodec());
        BoardTokenService tokenService = new HmacBoardTokenService(tokenSecret());
        MoveProcessor moveProcessor = new TicTacToeMoveProcessor(new FirstAvailableComputerMoveStrategy());
        return new SocketRequestHandler(new SignedMoveCommandProcessor(moveProcessor, tokenService, jsonCodec));
    }

    private static String tokenSecret() {
        String secret = System.getenv(TOKEN_SECRET_ENV);
        return secret == null || secret.isBlank() ? DEFAULT_TOKEN_SECRET : secret;
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

    private static class SignedMoveCommandProcessor implements CommandProcessor {
        private final MoveProcessor moveProcessor;
        private final BoardTokenService tokenService;
        private final SecureGameJsonCodec jsonCodec;

        SignedMoveCommandProcessor(
                MoveProcessor moveProcessor,
                BoardTokenService tokenService,
                SecureGameJsonCodec jsonCodec
        ) {
            this.moveProcessor = moveProcessor;
            this.tokenService = tokenService;
            this.jsonCodec = jsonCodec;
        }

        @Override
        public String process(String request) {
            try {
                Command command = Command.parse(request);
                if (command.isStart()) {
                    return jsonCodec.signedBoard(sign(BoardState.empty()));
                }

                BoardState board = BoardState.fromCompact(command.board());
                if (!tokenService.isValid(board, command.token())) {
                    return jsonCodec.error("Invalid board token.");
                }

                BoardState updatedBoard = moveProcessor.process(new MoveRequest(board, command.cell()));
                return jsonCodec.signedBoard(sign(updatedBoard));
            } catch (IllegalArgumentException e) {
                return jsonCodec.error(e.getMessage());
            }
        }

        private SignedBoardResponse sign(BoardState board) {
            return new SignedBoardResponse(board, tokenService.sign(board));
        }
    }

    private record Command(String action, String board, int cell, String token) {
        static Command parse(String request) {
            if (request == null || request.isBlank()) {
                throw new IllegalArgumentException("Request cannot be empty.");
            }

            String[] parts = request.trim().split("\\s+");
            if ("START".equalsIgnoreCase(parts[0])) {
                if (parts.length != 1) {
                    throw new IllegalArgumentException("START does not accept arguments.");
                }
                return new Command("START", "", 0, "");
            }

            if (parts.length != 4 || !"MOVE".equalsIgnoreCase(parts[0])) {
                throw new IllegalArgumentException("Use protocol: START | MOVE <compactBoard> <cell> <token>.");
            }

            try {
                return new Command("MOVE", parts[1], Integer.parseInt(parts[2]), parts[3]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("MOVE cell must be a number.");
            }
        }

        boolean isStart() {
            return "START".equals(action);
        }
    }
}
