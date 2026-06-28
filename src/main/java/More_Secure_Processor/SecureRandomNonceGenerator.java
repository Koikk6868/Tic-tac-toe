package More_Secure_Processor;

import java.security.SecureRandom;
import java.util.Base64;

public class SecureRandomNonceGenerator implements NonceGenerator {
    private static final int NONCE_BYTES = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        byte[] nonce = new byte[NONCE_BYTES];
        secureRandom.nextBytes(nonce);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);
    }
}
