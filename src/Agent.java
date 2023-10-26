import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

public class Agent extends Player{
    public static final int[] DOUBLES = generateDoubles();
    private static final int[] TRIPLES = generateTriples();
    private static final int[] QUADRUPLES = generateQuadruples();

    private final Random R = new Random();
    private final long[] Z_WHITE = new long[Board.AREA];
    private final long[] Z_BLACK = new long[Board.AREA];
    private final long Z_WTM;

    private final int DEPTH;
    private int nodes;
    public Agent(int depth){
        this.DEPTH = depth;

        R.setSeed(0);
        for (int i = 0; i < Z_WHITE.length; i++) {
            Z_WHITE[i] = R.nextLong();
            Z_BLACK[i] = R.nextLong();
        }
        Z_WTM = R.nextLong();
    }

    public Move getMove(Board board){
        nodes = 0;
        double start = System.nanoTime();

        Board boardCopy = board.copy();

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move: boardCopy.getMoves()){
            boardCopy.makeMove(move);
            int score = -alphaBeta(boardCopy, DEPTH, -1_000_000_000,1_000_000_000); //Initial AlphaBeta
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            boardCopy.unmakeMove();
        }

        double end = System.nanoTime();

        System.out.println("Number of nodes visited: " + nodes);
        System.out.println("Time per node: " + ((end - start)/(1_000_000*nodes)) + "ms");
        System.out.println("Best score: " + bestScore);

        return bestMove;
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta){
        if(board.isTerminal() || depth == 0){
            nodes++;
            return evaluate(board);
        }

        int score = -1_000_000_000;

        for (Move move: board.getMoves()) {
            board.makeMove(move);
            int value = -alphaBeta(board, depth - 1, -beta, -alpha);
            board.unmakeMove();

            if(value > score) score = value;
            if(score > alpha) alpha = score;
            if(alpha >= beta) break;
        }

        return score;
    }

    private int evaluate(Board board){
        int evaluation = 0;

        if(board.checkPlayerWin()){ //Check if player to move has won.
            evaluation += 1_000_000_000;
        }

        if(board.checkOpponentWin()){ //Check if opponent has won.
            evaluation -= 1_000_000_000;
        }


        evaluation += countStones(board.getPlayerStones());
        evaluation -= countStones(board.getOpponentStones());

        evaluation += 2*countDoubles(board.getPlayerStones());
        evaluation -= 2*countDoubles(board.getOpponentStones());

        evaluation += 5*countTriples(board.getPlayerStones());
        evaluation -= 5*countTriples(board.getOpponentStones());

        evaluation += 20*countQuadruples(board.getPlayerStones());
        evaluation -= 20*countQuadruples(board.getOpponentStones());

        return evaluation;
    }

    private static int countStones(Board.Stones stones){
        return stones.getBitSet().cardinality();
    }
    private static int countDoubles(Board.Stones stones){

        int count = 0;

        for (int i = 0; i < 2*Board.RADIUS; i++) {
            count += DOUBLES[stones.getFiles()[i]];
            count += DOUBLES[stones.getRanks()[i]];
            count += DOUBLES[stones.getColumns()[i]];
        }
        count += DOUBLES[stones.getColumns()[2*Board.RADIUS]];

        return count;
    }
    private static int countTriples(Board.Stones stones){

        int count = 0;

        for (int i = 0; i < 2*Board.RADIUS; i++) {
            count += TRIPLES[stones.getFiles()[i]];
            count += TRIPLES[stones.getRanks()[i]];
            count += TRIPLES[stones.getColumns()[i]];
        }
        count += TRIPLES[stones.getColumns()[2*Board.RADIUS]];

        return count;
    }
    private static int countQuadruples(Board.Stones stones){

        int count = 0;

        for (int i = 0; i < 2*Board.RADIUS; i++) {
            count += QUADRUPLES[stones.getFiles()[i]];
            count += QUADRUPLES[stones.getRanks()[i]];
            count += QUADRUPLES[stones.getColumns()[i]];
        }
        count += QUADRUPLES[stones.getColumns()[2*Board.RADIUS]];

        return count;
    }

    private long getZobristHash(BitSet whiteStones, BitSet blackStones, boolean whiteToMove){
        long zobristKey = 0;

        for (int i = 0; i < Board.AREA; i++) {
            if(whiteStones.get(i)) zobristKey ^= Z_WHITE[i];
            else if(blackStones.get(i)) zobristKey ^= Z_BLACK[i];
        }
        if(whiteToMove) zobristKey ^= Z_WTM;

        return zobristKey;
    }


    private static int[] generateDoubles(){
        int[] doubles = new int[1 << 2*Board.RADIUS];

        Arrays.setAll(doubles, i -> Integer.bitCount(i & (i << 1)));

        return doubles;
    }
    private static int[] generateTriples(){
        int[] doubles = new int[1 << 2*Board.RADIUS];

        Arrays.setAll(doubles, i -> Integer.bitCount(i & (i << 1) & (i << 2)));

        return doubles;
    }
    private static int[] generateQuadruples(){
        int[] doubles = new int[1 << 2*Board.RADIUS];

        Arrays.setAll(doubles, i -> Integer.bitCount(i & (i << 1) & (i << 2) & (i << 3)));

        return doubles;
    }
}
