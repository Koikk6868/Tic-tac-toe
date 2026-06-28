import Restful_Processor.BoardState;
import Restful_Processor.GameJsonCodec;
import Secure_Processor.SecureGameJsonCodec;
import Secure_Processor.SignedBoardResponse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SecureGameJsonCodecTest {
    @Test
    void readsAndWritesSignedBoardResponse() {
        SecureGameJsonCodec codec = new SecureGameJsonCodec(new GameJsonCodec());
        SignedBoardResponse signedResult = new SignedBoardResponse(
                BoardState.fromCompact("200010000"),
                "token-value"
        );

        SignedBoardResponse parsed = codec.readSignedBoard(codec.signedBoard(signedResult));

        assertEquals("200010000", parsed.board().toCompact());
        assertEquals("token-value", parsed.token());
    }
}
