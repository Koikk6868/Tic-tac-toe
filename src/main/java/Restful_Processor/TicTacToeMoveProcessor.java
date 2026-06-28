package Restful_Processor;
import TTTGame.Board;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.OptionalInt;

public class TicTacToeMoveProcessor implements MoveProcessor {
    private final ComputerMoveStrategy computerMoveStrategy;

    public TicTacToeMoveProcessor(ComputerMoveStrategy computerMoveStrategy) {
        this.computerMoveStrategy = computerMoveStrategy;
    }

    @Override
    public BoardState process(MoveRequest request) {
        BoardState boardState = request.board();
        Board board = toBoard(boardState);
        int cell = request.cell();

        if (!isValidBoard(boardState)) {
            return boardState;
        }

        if (board.checkWinner() != 0 || board.isFull()) {
            return boardState;
        }

        if (!board.isValidCell(cell)) {
            return boardState;
        }

        if (!board.isCellEmpty(cell)) {
            return boardState;
        }

        board.fillCell(BoardState.PLAYER, cell);
        BoardState updatedBoard = boardState.withMove(BoardState.PLAYER, cell);
        if (board.checkWinner() != 0 || board.isFull()) {
            return updatedBoard;
        }

        OptionalInt computerCell = computerMoveStrategy.chooseMove(updatedBoard);
        if (computerCell.isEmpty()) {
            return updatedBoard;
        }

        board.fillCell(BoardState.COMPUTER, computerCell.getAsInt());
        return updatedBoard.withMove(BoardState.COMPUTER, computerCell.getAsInt());
    }

    private boolean isValidBoard(BoardState board) {
        int playerMoves = board.countMoves(BoardState.PLAYER);
        int computerMoves = board.countMoves(BoardState.COMPUTER);
        return computerMoves <= playerMoves && playerMoves - computerMoves <= 1;
    }

    private Board toBoard(BoardState boardState) {
        return boardState.toBoard(new PrintStream(OutputStream.nullOutputStream()));
    }
}
