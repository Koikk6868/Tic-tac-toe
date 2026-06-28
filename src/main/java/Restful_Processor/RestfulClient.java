package Restful_Processor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RestfulClient {
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

            GameApi api = new SocketGameApi(host, port, new GameJsonCodec());
            new TerminalGameClient(api, new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8))).play();
        } catch (NumberFormatException e) {
            printUsage();
        } catch (IOException e) {
            System.out.println("Socket REST client error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java RestfulClient [host] [port]");
    }

    private interface GameApi {
        BoardState playMove(BoardState board, int cell) throws IOException;
    }

    private static class TerminalGameClient {
        private final GameApi api;
        private final BufferedReader keyboard;
        private BoardState board;

        TerminalGameClient(GameApi api, BufferedReader keyboard) {
            this.api = api;
            this.keyboard = keyboard;
            this.board = BoardState.empty();
        }

        void play() throws IOException {
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
                board = api.playMove(board, cell);
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
            printBoard();
            printStatus();
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

        private void printBoard() {
            System.out.print(board.render());
        }
    }

    private static class SocketGameApi implements GameApi {
        private final String host;
        private final int port;
        private final GameJsonCodec jsonCodec;

        SocketGameApi(String host, int port, GameJsonCodec jsonCodec) {
            this.host = host;
            this.port = port;
            this.jsonCodec = jsonCodec;
        }

        @Override
        public BoardState playMove(BoardState board, int cell) throws IOException {
            return jsonCodec.readBoard(send("MOVE " + board.toCompact() + " " + cell));
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
