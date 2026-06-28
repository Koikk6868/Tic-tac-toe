package HTTP_Processor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import Restful_Processor.BoardState;
import Restful_Processor.FirstAvailableComputerMoveStrategy;
import Restful_Processor.MoveRequest;
import Restful_Processor.TicTacToeMoveProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class TTTHttpServer {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = args.length >= 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        try {
            // 1. Initialize the built-in HTTP server
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // 2. Map the context path "/move" to our MoveHandler instance
            server.createContext("/move", new MoveHandler());
            
            server.setExecutor(null); // Creates a default executor/thread model
            server.start();
            System.out.println("HTTP Server started on port " + port);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    static class MoveHandler implements HttpHandler {
        private final HttpJsonCodec codec = new HttpJsonCodec();
        private final TicTacToeMoveProcessor processor = 
                new TicTacToeMoveProcessor(new FirstAvailableComputerMoveStrategy());

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 1. ADD THIS: Handle CORS Preflight requests for Web Browsers
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // We strictly enforce POST method for /move
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, codec.error("Method not allowed. Use POST."));
                return;
            }

            try (InputStream is = exchange.getRequestBody()) {
                String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                
                BoardState board = codec.readBoard(requestBody);
                int cell = codec.readCell(requestBody);

                MoveRequest request = new MoveRequest(board, cell);
                BoardState updatedBoard = processor.process(request);

                sendResponse(exchange, 200, codec.board(updatedBoard));
            } catch (Exception e) {
                sendResponse(exchange, 400, codec.error("Bad request: " + e.getMessage()));
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            // 2. ADD THIS: Allow the web client to read the response
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}