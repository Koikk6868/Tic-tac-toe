package More_Secure_Processor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HmacNonceTokenService implements NonceTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public HmacNonceTokenService(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Nonce token secret cannot be blank.");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String sign(String nonce, long issuedAtMillis) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            String payload = nonce + "|" + issuedAtMillis;
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign nonce.", e);
        }
    }

    @Override
    public boolean isValid(String nonce, long issuedAtMillis, String token) {
        if (nonce == null || nonce.isBlank() || token == null || token.isBlank()) {
            return false;
        }

        byte[] expected = sign(nonce, issuedAtMillis).getBytes(StandardCharsets.UTF_8);
        byte[] actual = token.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
