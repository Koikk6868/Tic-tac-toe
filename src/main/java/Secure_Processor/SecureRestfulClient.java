package Secure_Processor;

import Restful_Processor.BoardState;
import Restful_Processor.GameJsonCodec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SecureRestfulClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5001;

    public static void main(String[] args) {
        try {
            String host = args.length >= 1 ? args[0] : DEFAULT_HOST;
            int port = args.length >= 2 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

            if (args.length > 2) {
                printUsage();
                return;
            }

            SecureGameApi api = new SocketSecureGameApi(host, port, new SecureGameJsonCodec(new GameJsonCodec()));
            new TerminalGameClient(api, new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8))).play();
        } catch (NumberFormatException e) {
            printUsage();
        } catch (IOException e) {
            System.out.println("Secure socket REST client error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java Secure_Processor.SecureRestfulClient [host] [port]");
    }

    private interface SecureGameApi {
        SignedBoardResponse startGame() throws IOException;

        SignedBoardResponse playMove(BoardState board, int cell, String token) throws IOException;
    }

    private static class TerminalGameClient {
        private final SecureGameApi api;
        private final BufferedReader keyboard;
        private BoardState board;
        private String token;

        TerminalGameClient(SecureGameApi api, BufferedReader keyboard) {
            this.api = api;
            this.keyboard = keyboard;
        }

        void play() throws IOException {
            SignedBoardResponse start = api.startGame();
            board = start.board();
            token = start.token();
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

                BoardState previousBoard = board;
                SignedBoardResponse signedResult = api.playMove(board, cell, token);
                board = signedResult.board();
                token = signedResult.token();
                printResult(previousBoard);

                if (isFinished(board)) {
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
            if (previousBoard.toCompact().equals(board.toCompact())) {
                System.out.println("Move was not applied.");
            }
            System.out.print(board.render());
            printStatus();
        }

        private void printBoard() {
            System.out.print(board.render());
        }

        private void printStatus() {
            int winner = board.checkWinner();
            if (winner == BoardState.PLAYER) {
                System.out.println("Player#1 won!");
            } else if (winner == BoardState.COMPUTER) {
                System.out.println("Player#2 won!");
            } else if (board.isFull()) {
                System.out.println("It's a draw!");
            }
        }

        private boolean isFinished(BoardState board) {
            return board.checkWinner() != BoardState.EMPTY || board.isFull();
        }
    }

    private static class SocketSecureGameApi implements SecureGameApi {
        private final String host;
        private final int port;
        private final SecureGameJsonCodec jsonCodec;

        SocketSecureGameApi(String host, int port, SecureGameJsonCodec jsonCodec) {
            this.host = host;
            this.port = port;
            this.jsonCodec = jsonCodec;
        }

        @Override
        public SignedBoardResponse startGame() throws IOException {
            return jsonCodec.readSignedBoard(send("START"));
        }

        @Override
        public SignedBoardResponse playMove(BoardState board, int cell, String token) throws IOException {
            return jsonCodec.readSignedBoard(send("MOVE " + board.toCompact() + " " + cell + " " + token));
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
