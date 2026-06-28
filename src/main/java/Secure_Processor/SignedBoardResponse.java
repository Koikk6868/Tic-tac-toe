package Secure_Processor;

import Restful_Processor.BoardState;

public record SignedBoardResponse(BoardState board, String token) {
}
