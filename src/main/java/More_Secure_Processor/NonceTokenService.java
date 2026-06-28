package More_Secure_Processor;

public interface NonceTokenService {
    String sign(String nonce, long issuedAtMillis);

    boolean isValid(String nonce, long issuedAtMillis, String token);
}
