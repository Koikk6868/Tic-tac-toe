package Restful_Processor;
import java.util.Arrays;

import TTTGame.Board;

public final class BoardState {
    public static final int EMPTY = 0;
    public static final int PLAYER = 1;
    public static final int COMPUTER = 2;
    public static final int SIZE = 3;
    public static final int CELL_COUNT = SIZE * SIZE;

    private final int[] cells;

    private BoardState(int[] cells) {
        this.cells = Arrays.copyOf(cells, cells.length);
    }

    public static BoardState empty() {
        return new BoardState(new int[CELL_COUNT]);
    }

    public static BoardState fromCompact(String compactBoard) {
        if (compactBoard == null || compactBoard.length() != CELL_COUNT) {
            throw new IllegalArgumentException("Board must contain exactly 9 cells.");
        }

        int[] cells = new int[CELL_COUNT];
        for (int i = 0; i < compactBoard.length(); i++) {
            char value = compactBoard.charAt(i);
            if (value < '0' || value > '2') {
                throw new IllegalArgumentException("Board cells must use only 0, 1, or 2.");
            }
            cells[i] = value - '0';
        }
        return new BoardState(cells);
    }

    public String toCompact() {
        StringBuilder compact = new StringBuilder(CELL_COUNT);
        for (int cell : cells) {
            compact.append(cell);
        }
        return compact.toString();
    }

    public boolean isValidCell(int cell) {
        return cell >= 1 && cell <= CELL_COUNT;
    }

    public boolean isCellEmpty(int cell) {
        return cells[cell - 1] == EMPTY;
    }

    public BoardState withMove(int player, int cell) {
        if (!isValidPlayer(player)) {
            throw new IllegalArgumentException("Player must be 1 or 2.");
        }
        if (!isValidCell(cell)) {
            throw new IllegalArgumentException("Cell must be in range [1-9].");
        }
        if (!isCellEmpty(cell)) {
            throw new IllegalArgumentException("The cell is occupied.");
        }

        int[] updated = Arrays.copyOf(cells, cells.length);
        updated[cell - 1] = player;
        return new BoardState(updated);
    }

    public boolean isFull() {
        for (int cell : cells) {
            if (cell == EMPTY) {
                return false;
            }
        }
        return true;
    }

    public int checkWinner() {
        int[][] lines = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };

        for (int[] line : lines) {
            int first = cells[line[0]];
            if (first != EMPTY && first == cells[line[1]] && first == cells[line[2]]) {
                return first;
            }
        }
        return EMPTY;
    }

    public int countMoves(int player) {
        if (!isValidPlayer(player)) {
            throw new IllegalArgumentException("Player must be 1 or 2.");
        }

        int count = 0;
        for (int cell : cells) {
            if (cell == player) {
                count++;
            }
        }
        return count;
    }

    public String render() {
        StringBuilder rendered = new StringBuilder();
        for (int row = 0; row < SIZE; row++) {
            rendered.append("|");
            for (int column = 0; column < SIZE; column++) {
                rendered.append(" ").append(cells[row * SIZE + column]).append(" |");
            }
            rendered.append(System.lineSeparator());
        }
        return rendered.toString();
    }

    public Board toBoard(java.io.PrintStream out) {
        Board board = new Board(SIZE, out);
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] != EMPTY) {
                board.fillCell(cells[i], i + 1);
            }
        }
        return board;
    }

    private boolean isValidPlayer(int player) {
        return player == PLAYER || player == COMPUTER;
    }
}
