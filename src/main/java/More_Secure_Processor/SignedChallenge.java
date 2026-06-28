package More_Secure_Processor;

import Restful_Processor.BoardState;

public record SignedChallenge(
        BoardState board,
        String boardToken,
        String nonce,
        String nonceToken,
        long issuedAtMillis
) {
}
