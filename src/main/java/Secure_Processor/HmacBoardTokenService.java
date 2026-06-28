package Secure_Processor;

import Restful_Processor.BoardState;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HmacBoardTokenService implements BoardTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public HmacBoardTokenService(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Token secret cannot be blank.");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String sign(BoardState board) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(board.toCompact().getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign board state.", e);
        }
    }

    @Override
    public boolean isValid(BoardState board, String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        byte[] expected = sign(board).getBytes(StandardCharsets.UTF_8);
        byte[] actual = token.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
