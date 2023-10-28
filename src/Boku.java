public class Boku {
    private final Board BOARD;
    private final Player WHITE, BLACK;
    private final int RANDOM_START = 2;

    public Boku() {
        this.BOARD = new Board();
        this.WHITE = new AlphaBetaAgent(5);
        this.BLACK = new HashAgent(10);
    }

    public void startGame(){
        boolean whiteToMove = true;
        int round = 0;
        System.out.println("[ Round 0 | Start of the game ]");
        BOARD.print();

        Player random = new RandomAgent();

        for (int i = 0; i < RANDOM_START; i++) {
            round++;
            Move move = random.getMove(BOARD);
            BOARD.makeMove(move);

            whiteToMove = !whiteToMove;
        }

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
//            BOARD.printArray();
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
    }
}
