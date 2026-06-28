package HTTP_Processor;

import Restful_Processor.BoardState;

public class HttpJsonCodec {

    /**
     * Serializes a BoardState to a simple JSON object response
     * Output: {"board":"000000000"}
     */
    public String board(BoardState board) {
        return "{"
                + "\"board\":\"" + escape(board.toCompact()) + "\""
                + "}";
    }

    /**
     * Extracts the board state from the request JSON
     */
    public BoardState readBoard(String json) {
        return BoardState.fromCompact(stringField(json, "board"));
    }

    /**
     * Extracts the integer cell value from the request JSON
     */
    public int readCell(String json) {
        String cellStr = numberField(json, "cell");
        try {
            return cellStr.isEmpty() ? -1 : Integer.parseInt(cellStr);
        } catch (NumberFormatException e) {
            return -1; // -1 will be naturally rejected by Tic-Tac-Toe rules as invalid
        }
    }

    /**
     * Serializes an error message into a JSON response
     * Output: {"error":"reason"}
     */
    public String error(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    // Extracts a string value (must be inside quotes, e.g. "board":"102000000")
    private String stringField(String json, String fieldName) {
        int start = valueStart(json, fieldName);
        if (start < 0 || start >= json.length() || json.charAt(start) != '"') {
            return "";
        }

        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = start + 1; i < json.length(); i++) {
            char current = json.charAt(i);
            if (escaped) {
                value.append(unescape(current));
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            } else if (current == '"') {
                return value.toString();
            } else {
                value.append(current);
            }
        }
        return "";
    }

    // Extracts a numeric value (no quotes, e.g. "cell":5)
    private String numberField(String json, String fieldName) {
        int start = valueStart(json, fieldName);
        if (start < 0 || start >= json.length()) {
            return "";
        }

        StringBuilder value = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char current = json.charAt(i);
            // Harvest numeric characters or a minus sign for negative indices
            if (Character.isDigit(current) || current == '-') {
                value.append(current);
            } else if (current == ',' || current == '}' || Character.isWhitespace(current)) {
                break; // stop when we hit formatting around the number
            }
        }
        return value.toString();
    }

    // Navigates purely to the start of the field's value regardless of whitespace
    private int valueStart(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int keyStart = json.indexOf(key);
        if (keyStart < 0) {
            return -1;
        }

        int colon = json.indexOf(':', keyStart + key.length());
        if (colon < 0) {
            return -1;
        }

        int valueStart = colon + 1;
        // Skip over any whitespace between the colon and the value
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        return valueStart;
    }

    /**
     * Serializes a move request for the HTTP client.
     * Output: {"board":"000000000", "cell":5}
     */
    public String moveRequest(BoardState board, int cell) {
        return "{"
                + "\"board\":\"" + escape(board.toCompact()) + "\", "
                + "\"cell\":" + cell
                + "}";
    }

    /**
     * Checks if the JSON response contains an "error" field
     */
    public boolean isError(String json) {
        return !stringField(json, "error").isBlank();
    }

    /**
     * Extracts the error message from the JSON response
     */
    public String errorMessage(String json) {
        String error = stringField(json, "error");
        return error.isBlank() ? json : error; 
    }

    private char unescape(char value) {
        return switch (value) {
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case '\"' -> '\"';
            case '\\' -> '\\';
            default -> value;
        };
    }
}