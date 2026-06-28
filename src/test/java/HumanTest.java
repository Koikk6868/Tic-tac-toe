import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import TTTGame.Board;
import TTTGame.HumanPlayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

public class HumanTest {
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
