import More_Secure_Processor.MoreSecureGameJsonCodec;
import More_Secure_Processor.SignedChallenge;
import Restful_Processor.BoardState;
import Restful_Processor.GameJsonCodec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoreSecureGameJsonCodecTest {
    @Test
    void readsAndWritesChallenge() {
        MoreSecureGameJsonCodec codec = new MoreSecureGameJsonCodec(new GameJsonCodec());
        SignedChallenge challenge = new SignedChallenge(
                BoardState.fromCompact("200010000"),
                "board-token",
                "nonce",
                "nonce-token",
                1234L
        );

        SignedChallenge parsed = codec.readChallenge(codec.challenge(challenge));

        assertEquals("200010000", parsed.board().toCompact());
        assertEquals("board-token", parsed.boardToken());
        assertEquals("nonce", parsed.nonce());
        assertEquals("nonce-token", parsed.nonceToken());
        assertEquals(1234L, parsed.issuedAtMillis());
    }
}
