import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import TTTGame.Board;
import TTTGame.ComputerPlayer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class ComputerTest {
    private Board board;
    private ComputerPlayer computer;

    @BeforeEach
    public void setUp() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        board = new Board(3, new PrintStream(out));
        computer = new ComputerPlayer(board, new PrintStream(out));
    }

    // Intent: Verify that the computer chooses the first available cell (cell 1) on a completely empty board.
    @Test
    void testComputerMove_EmptyBoard() {
        computer.move();
        assertFalse(board.isCellEmpty(1));
    }

    // Intent: Verify that the computer skips occupied cells and selects the next available one.
    @Test
    void testComputerMove_PartiallyFilledBoard() {
        board.fillCell(1, 1);
        board.fillCell(1, 2);
        computer.move();
        assertFalse(board.isCellEmpty(3));
    }
}