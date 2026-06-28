package TTTGame;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class HumanPlayer implements Player {
    private final Board board;
    private final Scanner scanner;
    private final PrintStream out;

    public HumanPlayer(Board board, InputStream in, PrintStream out) {
        this.board = board;
        this.scanner = new Scanner(in);
        this.out = out;
    }

//    private int getMove() {
//        if (scanner.hasNextInt()) {
//            return scanner.nextInt();
//        } else {
//            scanner.next();
//            return -1;
//        }
//    }

    private boolean isQuit(String input) {
        return input.equals("q");
    }

    @Override
    public boolean move() {
        while (true) {
            out.print("Player#1's turn: ");
            out.flush();
            String input = scanner.nextLine();

            if (isQuit(input)) {
                out.println("End of the game!");
                return false;
            }

            try {
                int cell = Integer.parseInt(input);

                if (this.board.isValidCell(cell)) {
                    if (this.board.isCellEmpty(cell)) {
                        this.board.fillCell(1, cell);
                        return true;
                    } else {
                        out.println("The cell is occupied!");
                        out.println("Player#1's turn");
                    }
                } else {
                    out.println("Please, input a valid number [1-9]");
                }

            } catch (NumberFormatException e) {
                out.println("Please, input a valid number [1-9]");
            }
        }
    }
}
