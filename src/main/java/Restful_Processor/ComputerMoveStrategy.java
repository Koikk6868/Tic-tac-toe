package Restful_Processor;
import java.util.OptionalInt;

public interface ComputerMoveStrategy {
    OptionalInt chooseMove(BoardState board);
}
