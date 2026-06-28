import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import TTTGame.Main;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest {
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
