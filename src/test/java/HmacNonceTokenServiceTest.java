import More_Secure_Processor.HmacNonceTokenService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HmacNonceTokenServiceTest {
    @Test
    void tokenValidatesOriginalNonceAndTime() {
        HmacNonceTokenService tokenService = new HmacNonceTokenService("test-secret");
        String nonce = "nonce-value";
        long issuedAtMillis = 1000L;

        String token = tokenService.sign(nonce, issuedAtMillis);

        assertTrue(tokenService.isValid(nonce, issuedAtMillis, token));
    }

    @Test
    void tokenRejectsChangedNonceOrTime() {
        HmacNonceTokenService tokenService = new HmacNonceTokenService("test-secret");
        String token = tokenService.sign("nonce-value", 1000L);

        assertFalse(tokenService.isValid("different-nonce", 1000L, token));
        assertFalse(tokenService.isValid("nonce-value", 2000L, token));
    }
}
