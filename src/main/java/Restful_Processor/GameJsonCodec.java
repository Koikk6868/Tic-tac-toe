package Restful_Processor;
public class GameJsonCodec {
    public String board(BoardState board) {
        return "{"
                + "\"board\":\"" + escape(board.toCompact()) + "\""
                + "}";
    }

    public BoardState readBoard(String json) {
        return BoardState.fromCompact(stringField(json, "board"));
    }

    public String error(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    public boolean isError(String json) {
        return !stringField(json, "error").isBlank();
    }

    public String errorMessage(String json) {
        String error = stringField(json, "error");
        return error.isBlank() ? json : error;
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

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
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        return valueStart;
    }

    private char unescape(char value) {
        return switch (value) {
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case '"' -> '"';
            case '\\' -> '\\';
            default -> value;
        };
    }
}
