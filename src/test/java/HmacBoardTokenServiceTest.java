import Restful_Processor.BoardState;
import Secure_Processor.HmacBoardTokenService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HmacBoardTokenServiceTest {
    @Test
    void tokenValidatesOriginalBoard() {
        HmacBoardTokenService tokenService = new HmacBoardTokenService("test-secret");
        BoardState board = BoardState.fromCompact("200010000");

        String token = tokenService.sign(board);

        assertTrue(tokenService.isValid(board, token));
    }

    @Test
    void tokenRejectsTamperedBoard() {
        HmacBoardTokenService tokenService = new HmacBoardTokenService("test-secret");
        BoardState originalBoard = BoardState.fromCompact("200010000");
        BoardState tamperedBoard = BoardState.fromCompact("100010000");

        String token = tokenService.sign(originalBoard);

        assertFalse(tokenService.isValid(tamperedBoard, token));
    }
}
