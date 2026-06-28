import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import TTTGame.Board;
import TTTGame.ComputerPlayer;
import TTTGame.HumanPlayer;
import TTTGame.Main;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BoardTestV4 {
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

class ComputerTestV4 {
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

class HumanTestV4 {
    private Board board;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        board = new Board(3, new PrintStream(outContent));
    }

    // Intent: Verify that a valid console input (like "5") correctly updates the board.
    @Test
    void testHumanMove_ValidInput() {
        ByteArrayInputStream inContent = new ByteArrayInputStream("5\n".getBytes());

        HumanPlayer human = new HumanPlayer(board, inContent, new PrintStream(outContent));
        assertTrue(human.move());

        assertFalse(board.isCellEmpty(5));
    }

    // Intent: Verify that the human player loop handles invalid inputs (like letters) gracefully, asks again, and ultimately accepts a valid input.
    @Test
    void testHumanMove_InvalidInputThenValid() {
        ByteArrayInputStream inContent = new ByteArrayInputStream("abc\n3\n".getBytes());

        HumanPlayer human = new HumanPlayer(board, inContent, new PrintStream(outContent));
        assertTrue(human.move());

        assertFalse(board.isCellEmpty(3));
    }

    // Intent: Verify TS-009 - Human q quit case (returns false to stop game)
    @Test
    void testHumanMove_QuitGame() {
        ByteArrayInputStream inContent = new ByteArrayInputStream("q\n".getBytes());
        HumanPlayer human = new HumanPlayer(board, inContent, new PrintStream(outContent));

        boolean continuePlaying = human.move();

        assertFalse(continuePlaying);
        assertTrue(outContent.toString().contains("End of the game"));
    }

    // Intent: Verify TS-010 - Human non-integer branch strictness (Q is not q)
    @Test
    void testHumanMove_QuitCaseSensitivity() {
        ByteArrayInputStream inContent = new ByteArrayInputStream("Q\n1\n".getBytes());
        HumanPlayer human = new HumanPlayer(board, inContent, new PrintStream(outContent));

        boolean continuePlaying = human.move();

        assertTrue(continuePlaying);
        assertTrue(outContent.toString().contains("Please, input a valid number [1-9]"));
    }
}

public class TestScenarioV4 {
    /*
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(out));
    }
    */

    // Stimulate Socket:
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    public void setUpStream() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent)); }

    @AfterEach
    public void restoreStream() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    void testMain_StartupWithoutArguments() throws IOException {
        Main.main(new String[]{});
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertEquals("Please, input a valid option [1-2]" + System.lineSeparator(), output);
    }

    @Test
    void testMain_StartupWithInvalidArguments() throws IOException {
        Main.main(new String[]{"1 "});
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertEquals("Please, input a valid option [1-2]" + System.lineSeparator(), output);
    }

    @Test
    void testMain_StartupWithExtraArguments() throws IOException {
        Main.main(new String[]{"1", "extra"});
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertEquals("Please, input a valid option [1-2]" + System.lineSeparator(), output);
    }

    @Test
    void testMain_player1OptionTest() throws IOException {
        String simulatedUserInput = "1\n4\n7\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes(StandardCharsets.UTF_8)));
        Main.main(new String[]{"1"});
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Game started!"));
        assertTrue(output.contains("| 0 | 0 | 0 |"));
        assertTrue(output.contains("Player#1's turn"));
        assertTrue(output.contains("Player#1 won!"));
    }

    @Test
    void testMain_player2OptionTest() throws IOException {
        String simulatedUserInput = "4\n5\n";
        System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes(StandardCharsets.UTF_8)));
        Main.main(new String[]{"2"});
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Game started!"));
        assertTrue(output.contains("| 0 | 0 | 0 |"));
        assertTrue(output.contains("Player#1's turn"));
        assertTrue(output.contains("Player#2 won!"));
    }
}
