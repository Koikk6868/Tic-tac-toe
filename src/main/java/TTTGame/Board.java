package TTTGame;
import java.io.PrintStream;

public class Board {
    private final int[][] board;
    private final int size;
    private final PrintStream out;

    public Board(int size, PrintStream out) {
        this.size = size;
        this.board = new int[size][size];
        this.out = out;
    }

    private int[] getCoordinates(int cell) {
        int index = cell - 1;
        return new int[]{index / size, index % size};
    }

    public boolean isValidCell(int cell) {
        return cell >= 1 && cell <= size * size;
    }

    public boolean isCellEmpty(int cell) {
        int[] pos = getCoordinates(cell);
        return this.board[pos[0]][pos[1]] == 0;
    }

    public void fillCell(int playerIdentifier, int cell) {
        int[] pos = getCoordinates(cell);
        this.board[pos[0]][pos[1]] = playerIdentifier;
    }

    public void printBoard() {
        for (int i = 0; i < size; i++) {
            out.print("|");
            for (int j = 0; j < size; j++) {
                out.print(" " + this.board[i][j] + " |");
            }
            out.print(System.lineSeparator());
        }
    }

    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (this.board[i][j] == 0) return false;
            }
        }
        return true;
    }

    public int checkWinner() {
        for (int i = 0; i < size; i++) {
            if (board[i][0] != 0 && board[i][0] == board[i][1] && board[i][1] == board[i][2]) return board[i][0];
            if (board[0][i] != 0 && board[0][i] == board[1][i] && board[1][i] == board[2][i]) return board[0][i];
        }
        if (board[0][0] != 0 && board[0][0] == board[1][1] && board[1][1] == board[2][2]) return board[0][0];
        if (board[0][2] != 0 && board[0][2] == board[1][1] && board[1][1] == board[2][0]) return board[0][2];

        return 0;
    }
}
