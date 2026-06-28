package More_Secure_Processor;

import Restful_Processor.BoardState;
import Restful_Processor.GameJsonCodec;

public class MoreSecureGameJsonCodec {
    private final GameJsonCodec gameJsonCodec;

    public MoreSecureGameJsonCodec(GameJsonCodec gameJsonCodec) {
        this.gameJsonCodec = gameJsonCodec;
    }

    public String challenge(SignedChallenge challenge) {
        return "{"
                + "\"board\":\"" + escape(challenge.board().toCompact()) + "\","
                + "\"boardToken\":\"" + escape(challenge.boardToken()) + "\","
                + "\"nonce\":\"" + escape(challenge.nonce()) + "\","
                + "\"nonceToken\":\"" + escape(challenge.nonceToken()) + "\","
                + "\"issuedAtMillis\":" + challenge.issuedAtMillis()
                + "}";
    }

    public SignedChallenge readChallenge(String json) {
        return new SignedChallenge(
                BoardState.fromCompact(stringField(json, "board")),
                stringField(json, "boardToken"),
                stringField(json, "nonce"),
                stringField(json, "nonceToken"),
                longField(json, "issuedAtMillis")
        );
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

    private long longField(String json, String fieldName) {
        int start = valueStart(json, fieldName);
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        return start < end ? Long.parseLong(json.substring(start, end)) : 0;
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
