import org.junit.jupiter.api.Test;

import Restful_Processor.BoardState;
import Restful_Processor.FirstAvailableComputerMoveStrategy;
import Restful_Processor.MoveProcessor;
import Restful_Processor.MoveRequest;
import Restful_Processor.TicTacToeMoveProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TicTacToeMoveProcessorTest {
    private final MoveProcessor processor = new TicTacToeMoveProcessor(new FirstAvailableComputerMoveStrategy());

    @Test
    void processMoveReturnsUpdatedBoardWithoutServerSessionState() {
        BoardState result = processor.process(new MoveRequest(BoardState.empty(), 5));

        assertEquals("200010000", result.toCompact());
    }

    @Test
    void processMoveRejectsOccupiedCellWithoutChangingBoard() {
        BoardState board = BoardState.fromCompact("200010000");

        BoardState result = processor.process(new MoveRequest(board, 5));

        assertEquals(board.toCompact(), result.toCompact());
    }

    @Test
    void processMoveDetectsPlayerWinFromClientOwnedBoard() {
        BoardState board = BoardState.fromCompact("122010000");

        BoardState result = processor.process(new MoveRequest(board, 9));

        assertEquals("122010000", board.toCompact());
        assertEquals("122010001", result.toCompact());
        assertEquals(BoardState.PLAYER, result.checkWinner());
    }
}
