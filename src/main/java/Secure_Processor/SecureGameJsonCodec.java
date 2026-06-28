package Secure_Processor;

import Restful_Processor.BoardState;
import Restful_Processor.GameJsonCodec;

public class SecureGameJsonCodec {
    private final GameJsonCodec gameJsonCodec;

    public SecureGameJsonCodec(GameJsonCodec gameJsonCodec) {
        this.gameJsonCodec = gameJsonCodec;
    }

    public String signedBoard(SignedBoardResponse result) {
        String json = gameJsonCodec.board(result.board());
        return json.substring(0, json.length() - 1)
                + ",\"token\":\"" + escape(result.token()) + "\"}";
    }

    public SignedBoardResponse readSignedBoard(String json) {
        BoardState board = gameJsonCodec.readBoard(json);
        return new SignedBoardResponse(board, stringField(json, "token"));
    }

    public String error(String message) {
        return gameJsonCodec.error(message);
    }

    public boolean isError(String json) {
        return gameJsonCodec.isError(json);
    }

    public String errorMessage(String json) {
        return gameJsonCodec.errorMessage(json);
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
