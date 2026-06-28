package TTTGame;
import java.io.InputStream;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Please, input a valid option [1-2]");
            return;
        }

        int start;
        try {
            start = Integer.parseInt(args[0]);
            if (start != 1 && start != 2) {
                System.out.println("Please, input a valid option [1-2]");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Please, input a valid option [1-2]");
            return;
        }

        InputStream in = System.in;
        PrintStream out = System.out;

        Game game = new Game(start, in, out);
        game.play();
    }
}