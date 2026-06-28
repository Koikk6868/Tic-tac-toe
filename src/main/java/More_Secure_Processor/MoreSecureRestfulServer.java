package More_Secure_Processor;

import Restful_Processor.BoardState;
import Restful_Processor.FirstAvailableComputerMoveStrategy;
import Restful_Processor.GameJsonCodec;
import Restful_Processor.MoveProcessor;
import Restful_Processor.MoveRequest;
import Restful_Processor.TicTacToeMoveProcessor;
import Secure_Processor.BoardTokenService;
import Secure_Processor.HmacBoardTokenService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MoreSecureRestfulServer {
    private static final int DEFAULT_PORT = 5002;
    private static final int WAITING_CONNECTION_BACKLOG = 50;
    private static final long MOVE_TIME_LIMIT_MILLIS = 10_000L;
    private static final long USED_NONCE_RETENTION_MILLIS = 60_000L;
    private static final String DEFAULT_TOKEN_SECRET = "change-this-secret";
    private static final String TOKEN_SECRET_ENV = "TTT_TOKEN_SECRET";

    private final int port;
    private final RequestHandler requestHandler;

    public MoreSecureRestfulServer(int port) {
        this(port, defaultRequestHandler());
    }

    private MoreSecureRestfulServer(int port, RequestHandler requestHandler) {
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

            new MoreSecureRestfulServer(port).start();
        } catch (NumberFormatException e) {
            printUsage();
        } catch (IOException e) {
            System.out.println("More-secure socket REST server error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java More_Secure_Processor.MoreSecureRestfulServer [port]");
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port, WAITING_CONNECTION_BACKLOG)) {
            System.out.println("More-secure stateless socket Tic-Tac-Toe server listening on port " + port);
            System.out.println("Protocol: START | MOVE <board> <boardToken> <nonce> <nonceToken> <issuedAtMillis> <cell>");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    requestHandler.handle(clientSocket);
                }
            }
        }
    }

    private static RequestHandler defaultRequestHandler() {
        String secret = tokenSecret();
        MoreSecureGameJsonCodec jsonCodec = new MoreSecureGameJsonCodec(new GameJsonCodec());
        ChallengeService challengeService = new ChallengeService(
                new HmacBoardTokenService(secret),
                new HmacNonceTokenService(secret),
                new SecureRandomNonceGenerator()
        );
        MoveProcessor moveProcessor = new TicTacToeMoveProcessor(new FirstAvailableComputerMoveStrategy());
        return new SocketRequestHandler(new MoreSecureCommandProcessor(
                moveProcessor,
                challengeService,
                new InMemoryUsedNonceStore(),
                jsonCodec
        ));
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

    private static class MoreSecureCommandProcessor implements CommandProcessor {
        private final MoveProcessor moveProcessor;
        private final ChallengeService challengeService;
        private final UsedNonceStore usedNonceStore;
        private final MoreSecureGameJsonCodec jsonCodec;

        MoreSecureCommandProcessor(
                MoveProcessor moveProcessor,
                ChallengeService challengeService,
                UsedNonceStore usedNonceStore,
                MoreSecureGameJsonCodec jsonCodec
        ) {
            this.moveProcessor = moveProcessor;
            this.challengeService = challengeService;
            this.usedNonceStore = usedNonceStore;
            this.jsonCodec = jsonCodec;
        }

        @Override
        public String process(String request) {
            try {
                Command command = Command.parse(request);
                if (command.isStart()) {
                    return jsonCodec.challenge(challengeService.createChallenge(BoardState.empty(), now()));
                }

                long now = now();
                MoveCommand move = command.toMoveCommand();
                usedNonceStore.removeEntriesOlderThan(now - USED_NONCE_RETENTION_MILLIS);

                if (!challengeService.isValidBoard(move.board(), move.boardToken())) {
                    return jsonCodec.error("Invalid board token.");
                }

                if (!challengeService.isValidNonce(move.nonce(), move.issuedAtMillis(), move.nonceToken())) {
                    return jsonCodec.error("Invalid nonce token.");
                }

                if (!usedNonceStore.markUsed(move.nonce(), now)) {
                    return jsonCodec.error("Nonce already used.");
                }

                if (now - move.issuedAtMillis() > MOVE_TIME_LIMIT_MILLIS) {
                    return jsonCodec.error("Move expired.");
                }

                BoardState updatedBoard = moveProcessor.process(new MoveRequest(move.board(), move.cell()));
                return jsonCodec.challenge(challengeService.createChallenge(updatedBoard, now));
            } catch (IllegalArgumentException e) {
                return jsonCodec.error(e.getMessage());
            }
        }

        private long now() {
            return System.currentTimeMillis();
        }
    }

    private static class ChallengeService {
        private final BoardTokenService boardTokenService;
        private final NonceTokenService nonceTokenService;
        private final NonceGenerator nonceGenerator;

        ChallengeService(
                BoardTokenService boardTokenService,
                NonceTokenService nonceTokenService,
                NonceGenerator nonceGenerator
        ) {
            this.boardTokenService = boardTokenService;
            this.nonceTokenService = nonceTokenService;
            this.nonceGenerator = nonceGenerator;
        }

        SignedChallenge createChallenge(BoardState board, long issuedAtMillis) {
            String nonce = nonceGenerator.generate();
            return new SignedChallenge(
                    board,
                    boardTokenService.sign(board),
                    nonce,
                    nonceTokenService.sign(nonce, issuedAtMillis),
                    issuedAtMillis
            );
        }

        boolean isValidBoard(BoardState board, String boardToken) {
            return boardTokenService.isValid(board, boardToken);
        }

        boolean isValidNonce(String nonce, long issuedAtMillis, String nonceToken) {
            return nonceTokenService.isValid(nonce, issuedAtMillis, nonceToken);
        }
    }

    private record Command(String action, String[] parts) {
        static Command parse(String request) {
            if (request == null || request.isBlank()) {
                throw new IllegalArgumentException("Request cannot be empty.");
            }

            String[] parts = request.trim().split("\\s+");
            if ("START".equalsIgnoreCase(parts[0])) {
                if (parts.length != 1) {
                    throw new IllegalArgumentException("START does not accept arguments.");
                }
                return new Command("START", parts);
            }

            if (parts.length != 7 || !"MOVE".equalsIgnoreCase(parts[0])) {
                throw new IllegalArgumentException(
                        "Use protocol: START | MOVE <board> <boardToken> <nonce> <nonceToken> <issuedAtMillis> <cell>."
                );
            }

            return new Command("MOVE", parts);
        }

        boolean isStart() {
            return "START".equals(action);
        }

        MoveCommand toMoveCommand() {
            try {
                return new MoveCommand(
                        BoardState.fromCompact(parts[1]),
                        parts[2],
                        parts[3],
                        parts[4],
                        Long.parseLong(parts[5]),
                        Integer.parseInt(parts[6])
                );
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("MOVE issued time and cell must be numbers.");
            }
        }
    }

    private record MoveCommand(
            BoardState board,
            String boardToken,
            String nonce,
            String nonceToken,
            long issuedAtMillis,
            int cell
    ) {
    }
}
