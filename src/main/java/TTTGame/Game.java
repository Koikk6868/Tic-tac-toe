package TTTGame;
import java.io.InputStream;
import java.io.PrintStream;

public class Game {
    private final Board board;
    private final Player humanPlayer;
    private final Player computerPlayer;
    private final PrintStream out;
    private int currentTurn;

    public Game(int start, InputStream in, PrintStream out) {
        this.out = out;
        this.board = new Board(3, out);
        this.humanPlayer = new HumanPlayer(this.board, in, out);
        this.computerPlayer = new ComputerPlayer(this.board, out);
        this.currentTurn = start;
    }

    public void play() {
        out.println("Game started!");
        board.printBoard();

        while (true) {
            boolean continuePlaying;

            if (currentTurn == 1) {
                continuePlaying = humanPlayer.move();
            } else {
                continuePlaying = computerPlayer.move();
            }

            if (!continuePlaying) {
                break;
            }

            board.printBoard();

            int winner = board.checkWinner();
            if (winner != 0) {
                if (winner == 1) {
                    out.println("Player#1 won!");
                } else {
                    out.println("Player#2 won!");
                }
                break;
            }

            if (board.isFull()) {
                out.println("It's a draw!");
                break;
            }

            currentTurn = (currentTurn == 1) ? 2 : 1;
        }
    }
}