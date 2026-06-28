package Secure_Processor;

import Restful_Processor.BoardState;

public interface BoardTokenService {
    String sign(BoardState board);

    boolean isValid(BoardState board, String token);
}
