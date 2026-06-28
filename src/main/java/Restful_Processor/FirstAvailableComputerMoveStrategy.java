package Restful_Processor;
import TTTGame.ComputerPlayer;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.OptionalInt;

public class FirstAvailableComputerMoveStrategy implements ComputerMoveStrategy {
    @Override
    public OptionalInt chooseMove(BoardState board) {
        ComputerPlayer computerPlayer = new ComputerPlayer(
                board.toBoard(new PrintStream(OutputStream.nullOutputStream())),
                new PrintStream(OutputStream.nullOutputStream())
        );
        int cell = computerPlayer.chooseCell();
        return cell == -1 ? OptionalInt.empty() : OptionalInt.of(cell);
    }
}
