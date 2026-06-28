import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import TTTGame.Board;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    private Board board;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    public void initialize() {
        outContent = new ByteArrayOutputStream();
        board = new Board(3, new PrintStream(outContent));
    }

    @Test
    void printBoardTestFunction() {
        String expected =
                "| 0 | 0 | 0 |" + System.lineSeparator() +
                        "| 0 | 0 | 0 |" + System.lineSeparator() +
                        "| 0 | 0 | 0 |" + System.lineSeparator();

        board.printBoard();
        assertEquals(expected, outContent.toString());
    }

    // Intent: Verify that a valid, unoccupied cell within the 1-9 range is accepted.
    @Test
    void isValidCellTestFunction_Valid() {
        assertTrue(board.isValidCell(5));
    }

    // Intent: Verify that the board rejects cell inputs that are greater than 9.
    @Test
    void isValidCellTestFunction_CellLarger9() {
        assertFalse(board.isValidCell(10));
    }

    // Intent: Verify that the board rejects cell inputs that are less than 1.
    @Test
    void isValidCellTestFunction_CellSmaller1() {
        assertFalse(board.isValidCell(-1));
    }

    // Intent: Verify that the board accepts a move on a cell that is free.
    @Test
    void isCellEmptyTestFunction_CellFree() {
        assertTrue(board.isCellEmpty(1));
    }

    // Intent: Verify that the board rejects a move on a cell that is already occupied.
    @Test
    void isCellEmptyTestFunction_CellOccupied() {
        board.fillCell(1, 1);
        board.fillCell(2, 2);
        assertFalse(board.isCellEmpty(2));
    }

    // Intent: Verify that a horizontal win on the top row correctly returns the winning player's number.
    @Test
    void checkWinnerTestFunction_WinnerExistHorizontal1() {
        board.fillCell(1, 1);
        board.fillCell(1, 2);
        board.fillCell(1, 3);
        assertEquals(1, board.checkWinner());
    }

    // Intent: Verify that a horizontal win on the middle row correctly returns the winning player's number.
    @Test
    void checkWinnerTestFunction_WinnerExistHorizontal2() {
        board.fillCell(2, 4);
        board.fillCell(2, 5);
        board.fillCell(2, 6);
        assertEquals(2, board.checkWinner());
    }

    // Intent: Verify that a horizontal win on the bottom row correctly returns the winning player's number.
    @Test
    void checkWinnerTestFunction_WinnerExistHorizontal3() {
        board.fillCell(3, 7);
        board.fillCell(3, 8);
        board.fillCell(3, 9);
        assertEquals(3, board.checkWinner());
    }

    // Intent: Verify that a vertical win on the left column correctly returns the winning player's number.
    @Test
    void checkWinnerTestFunction_WinnerExistVertical1() {
        board.fillCell(1, 1);
        board.fillCell(1, 4);
        board.fillCell(1, 7);
        assertEquals(1, board.checkWinner());
    }

    // Intent: Verify that a vertical win on the middle column correctly returns the winning player's number.
    @Test
    void checkWinnerTestFunction_WinnerExistVertical2() {
        board.fillCell(2, 2);
        board.fillCell(2, 5);
        board.fillCell(2, 8);
        assertEquals(2, board.checkWinner());
    }

    // Intent: Verify that a vertical win on the right column correctly returns the winning player's number.
    @Test
    void checkWinnerTestFunction_WinnerExistVertical3() {
        board.fillCell(3, 3);
        board.fillCell(3, 6);
        board.fillCell(3, 9);
        assertEquals(3, board.checkWinner());
    }

    // Intent: Verify that a diagonal win (top-left to bottom-right) correctly returns the winning player.
    @Test
    void checkWinnerTestFunction_WinnerExistDiagonal1() {
        board.fillCell(1, 1);
        board.fillCell(1, 5);
        board.fillCell(1, 9);
        assertEquals(1, board.checkWinner());
    }

    // Intent: Verify that a diagonal win (top-right to bottom-left) correctly returns the winning player.
    @Test
    void checkWinnerTestFunction_WinnerExistDiagonal2() {
        board.fillCell(2, 3);
        board.fillCell(2, 5);
        board.fillCell(2, 7);
        assertEquals(2, board.checkWinner());
    }

    // Intent: Verify that an empty board returns 0, indicating no winner yet.
    @Test
    void checkWinnerTestFunction_NoWinner1() {
        assertEquals(0, board.checkWinner());
    }

    // Intent: Verify that a full board with no three-in-a-row returns 0 (a draw condition).
    @Test
    void checkWinnerTestFunction_NoWinner2() {
        board.fillCell(1, 1);
        board.fillCell(2, 2);
        board.fillCell(1, 5);
        board.fillCell(2, 3);
        board.fillCell(1, 6);
        board.fillCell(2, 4);
        board.fillCell(1, 7);
        board.fillCell(2, 9);
        board.fillCell(1, 8);
        assertEquals(0, board.checkWinner());
    }

    // Intent: Verify that the isFull method returns true when all 9 cells are occupied.
    @Test
    void checkIsFullTestFunction_Full() {
        board.fillCell(1, 1);
        board.fillCell(2, 2);
        board.fillCell(1, 5);
        board.fillCell(2, 3);
        board.fillCell(1, 6);
        board.fillCell(2, 4);
        board.fillCell(1, 7);
        board.fillCell(2, 9);
        board.fillCell(1, 8);
        assertTrue(board.isFull());
    }

    // Intent: Verify that the isFull method returns false on an empty board.
    @Test
    void checkIsFullTestFunction_NotFull1() {
        assertFalse(board.isFull());
    }

    // Intent: Verify that the isFull method returns false on a partially filled board.
    @Test
    void checkIsFullTestFunction_NotFull2() {
        board.fillCell(1, 1);
        board.fillCell(2, 2);
        assertFalse(board.isFull());
    }
}