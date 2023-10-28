import java.util.Objects;
import java.util.Scanner;

public class Boku {
    private final Board BOARD;
    private final Player WHITE, BLACK;

    public Boku() {
        this.BOARD = new Board();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Player moves first? (y?): ");
        if(Objects.equals(scanner.nextLine(), "y")){
            this.WHITE = new Human();
            this.BLACK = new Agent(20);
        } else {
            this.WHITE = new Agent(20);
//            this.BLACK = new Agent(5);
            this.BLACK = new Human();
        }
    }

    public void startGame(){
        boolean whiteToMove = true;
        int round = 0;

        System.out.println("[ Round 0 | Start of the game ]");
        BOARD.print();

        while (!BOARD.isTerminal()) {
            round++;
            Player playerToMove = whiteToMove? WHITE:BLACK;

            Move move = playerToMove.getMove(BOARD);
            BOARD.makeMove(move);

            move.print();
            System.out.println();
            System.out.println("[ Round " + round + " ]");
            BOARD.print();
            System.out.println();

            whiteToMove = !whiteToMove;
        }

        if (BOARD.checkWhiteWin()){
            System.out.println("White won");
        } else if(BOARD.checkBlackWin()){
            System.out.println("Black won");
        } else {
            System.out.println("Draw");
        }

        System.out.println();
        System.out.println("[  Moves:  ]");

        for (Move move: BOARD.getMoveHistory()){
            move.print();
            System.out.println();
        }
    }
}
