import java.util.Scanner;

public class Boku {
    private final Board BOARD;
    private final Player WHITE, BLACK;

    public Boku() {
        this.BOARD = new Board();
        this.WHITE = new Agent(2);
        this.BLACK = new Agent(4);
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

            System.out.println();
            System.out.println("[ Round " + round + " ]");
            move.print();
            BOARD.print();

            whiteToMove = !whiteToMove;
        }

        if (BOARD.checkWhiteWin()){
            System.out.println("White won");
        } else if(BOARD.checkBlackWin()){
            System.out.println("Black won");
        }
    }
}
