package More_Secure_Processor;

import Restful_Processor.BoardState;
import Restful_Processor.GameJsonCodec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MoreSecureRestfulClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5002;

    public static void main(String[] args) {
        try {
            String host = args.length >= 1 ? args[0] : DEFAULT_HOST;
            int port = args.length >= 2 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

            if (args.length > 2) {
                printUsage();
                return;
            }

            MoreSecureGameApi api = new SocketMoreSecureGameApi(
                    host,
                    port,
                    new MoreSecureGameJsonCodec(new GameJsonCodec())
            );
            new TerminalGameClient(api, new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8))).play();
        } catch (NumberFormatException e) {
            printUsage();
        } catch (IOException e) {
            System.out.println("More-secure socket REST client error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java More_Secure_Processor.MoreSecureRestfulClient [host] [port]");
    }

    private interface MoreSecureGameApi {
        SignedChallenge startGame() throws IOException;

        SignedChallenge playMove(SignedChallenge challenge, int cell) throws IOException;
    }

    private static class TerminalGameClient {
        private final MoreSecureGameApi api;
        private final BufferedReader keyboard;
        private SignedChallenge challenge;

        TerminalGameClient(MoreSecureGameApi api, BufferedReader keyboard) {
            this.api = api;
            this.keyboard = keyboard;
        }

        void play() throws IOException {
            challenge = api.startGame();
            System.out.println("Game started!");
            printBoard();

            while (true) {
                System.out.print("Choose cell [1-9] or q to quit: ");
                String input = keyboard.readLine();

                if (input == null || "q".equalsIgnoreCase(input.trim())) {
                    System.out.println("End of the game!");
                    return;
                }

                Integer cell = parseCell(input);
                if (cell == null) {
                    System.out.println("Please, input a valid number [1-9]");
                    continue;
                }

                BoardState previousBoard = challenge.board();
                challenge = api.playMove(challenge, cell);
                printResult(previousBoard);

                if (isFinished(challenge.board())) {
                    return;
                }
            }
        }

        private Integer parseCell(String input) {
            try {
                int cell = Integer.parseInt(input.trim());
                return cell >= 1 && cell <= 9 ? cell : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private void printResult(BoardState previousBoard) {
            if (previousBoard.toCompact().equals(challenge.board().toCompact())) {
                System.out.println("Move was not applied.");
            }
            printBoard();
            printStatus();
        }

        private void printBoard() {
            System.out.print(challenge.board().render());
        }

        private void printStatus() {
            int winner = challenge.board().checkWinner();
            if (winner == BoardState.PLAYER) {
                System.out.println("Player#1 won!");
            } else if (winner == BoardState.COMPUTER) {
                System.out.println("Player#2 won!");
            } else if (challenge.board().isFull()) {
                System.out.println("It's a draw!");
            }
        }

        private boolean isFinished(BoardState board) {
            return board.checkWinner() != BoardState.EMPTY || board.isFull();
        }
    }

    private static class SocketMoreSecureGameApi implements MoreSecureGameApi {
        private final String host;
        private final int port;
        private final MoreSecureGameJsonCodec jsonCodec;

        SocketMoreSecureGameApi(String host, int port, MoreSecureGameJsonCodec jsonCodec) {
            this.host = host;
            this.port = port;
            this.jsonCodec = jsonCodec;
        }

        @Override
        public SignedChallenge startGame() throws IOException {
            return jsonCodec.readChallenge(send("START"));
        }

        @Override
        public SignedChallenge playMove(SignedChallenge challenge, int cell) throws IOException {
            return jsonCodec.readChallenge(send("MOVE "
                    + challenge.board().toCompact() + " "
                    + challenge.boardToken() + " "
                    + challenge.nonce() + " "
                    + challenge.nonceToken() + " "
                    + challenge.issuedAtMillis() + " "
                    + cell));
        }

        private String send(String command) throws IOException {
            try (
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)
            ) {
                out.println(command);
                String response = in.readLine();
                if (response == null) {
                    throw new IOException("Server closed the connection without a response.");
                }
                if (jsonCodec.isError(response)) {
                    throw new IOException(jsonCodec.errorMessage(response));
                }
                return response;
            }
        }
    }
}
