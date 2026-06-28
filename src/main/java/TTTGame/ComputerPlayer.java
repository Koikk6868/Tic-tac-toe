package TTTGame;
import java.io.PrintStream;

public class ComputerPlayer implements Player {
    private final Board board;
    private final PrintStream out;

    public ComputerPlayer(Board board, PrintStream out) {
        this.board = board;
        this.out = out;
    }

    public int chooseCell() {
        for (int i = 1; i <= 9; i++) {
            if (board.isValidCell(i) && board.isCellEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean move() {
        int cell = chooseCell();
        if (cell != -1) {
            board.fillCell(2, cell); // 2 represents Computer
            out.println("Computer chooses cell " + cell);
        }
        return true;
    }
}
