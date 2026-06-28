package HTTP_Processor;

import Restful_Processor.BoardState;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TTTHttpClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        String host = args.length >= 1 ? args[0] : DEFAULT_HOST;
        int port = args.length >= 2 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        String serverUrl = "http://" + host + ":" + port + "/move";

        HttpJsonCodec codec = new HttpJsonCodec();
        HttpClient httpClient = HttpClient.newHttpClient();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        BoardState currentBoard = BoardState.empty();
        System.out.println("Starting HTTP Tic-Tac-Toe Client against " + serverUrl);

        try {
            while (currentBoard.checkWinner() == 0 && !currentBoard.isFull()) {
                currentBoard.toBoard(System.out);

                System.out.print("Enter your move (0-8): ");
                String input = reader.readLine();
                
                if (input == null) break; // Allow gracefully exiting via EOF

                int cell;
                try {
                    cell = Integer.parseInt(input.trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    continue;
                }

                String compact = currentBoard.toCompact();
                if (cell < 0 || cell > 8 || compact.charAt(cell) != '0') {
                    System.out.println("Cell " + cell + " is invalid or already occupied. Try again.");
                    continue;
                }

                // Codec handles serialization
                String requestJson = codec.moveRequest(currentBoard, cell);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(serverUrl))
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                        .build();

                // Send request and parse response
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    currentBoard = codec.readBoard(response.body());
                } else if (codec.isError(response.body())) {
                    System.out.println("Server rejected move: " + codec.errorMessage(response.body()));
                } else {
                    System.out.println("Server error HTTP " + response.statusCode());
                }
            }

            // Game over phase
            currentBoard.toBoard(System.out);
            int winner = currentBoard.checkWinner();

            if (winner == BoardState.PLAYER) {
                System.out.println("Congratulations! You win!");
            } else if (winner == BoardState.COMPUTER) {
                System.out.println("Computer wins! Better luck next time.");
            } else {
                System.out.println("It's a draw.");
            }

        } catch (Exception e) {
            System.err.println("Communication error: " + e.getMessage());
        }
    }
}