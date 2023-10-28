import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class AlphaBetaAgent extends Player{
    public static final int[] DOUBLES = generateDoubles();
    private static final int[] TRIPLES = generateTriples();
    private static final int[] QUADRUPLES = generateQuadruples();

    private final int DEPTH;

    public AlphaBetaAgent(int depth){
        this.DEPTH = depth;
    }

    private int nodeCount, pruneCount;
    private Move rootBestMove;
    public Move getMove(Board board){
        nodeCount = pruneCount = 0;
        double start = System.nanoTime();

        Board boardCopy = board.copy();

        rootBestMove = null;
        int bestValue = alphaBeta(boardCopy, 0, DEPTH, -Integer.MAX_VALUE,Integer.MAX_VALUE); //Initial AlphaBeta

        double end = System.nanoTime();

        System.out.println("Number of nodes visited: " + nodeCount);
        System.out.println("Number of nodes pruned: " + pruneCount);
//        System.out.println("Total number of nodes visited: " + (totalNodes+=nodes));
        System.out.println("Time: " + ((end - start)/(1_000_000.0)) + "ms");
        System.out.println(" Best score: " + bestValue);

        return rootBestMove;
    }

    private int alphaBeta(Board board, int plyFromRoot, int depth, int alpha, int beta){
        nodeCount++;

        if(depth == 0){
            return evaluate(board);
        }

        int bestValue = -Integer.MAX_VALUE;

        List<Move> moveList = board.getMoves();

        if(moveList.isEmpty()){
            return evaluate(board);
        }

//        System.out.println("[  Depth: " + depth + " " + (board.isWhiteToMove()?"White":"Black") + " to move.  ]");
        for (Move move : moveList) {

            board.makeMove(move);
            int value = -alphaBeta(board, plyFromRoot + 1, depth - 1, -beta, -alpha);
            board.unmakeMove();

            if (value > bestValue) {
                bestValue = value;
                if(plyFromRoot == 0){
                    rootBestMove = move;
                }
            }
            if (bestValue > alpha) alpha = bestValue;
            if (alpha >= beta) {
                pruneCount++;
                break;
            }
        }

//        System.out.println("Best value: " + bestValue);
        return bestValue;
    }

    private int evaluate(Board board){ // Give the board evaluation for the player to move, positive is better.
        int evaluation = 5;

        if(board.checkOpponentWin()){ //Check if opponent has won.
            evaluation -= 1_000_000_000;
        }

        evaluation += countStones(board.getPlayerStones());
        evaluation -= countStones(board.getOpponentStones());

        evaluation += countDoubles(board.getPlayerStones());
        evaluation -= countDoubles(board.getOpponentStones());

        evaluation += countTriples(board.getPlayerStones());
        evaluation -= countTriples(board.getOpponentStones());

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
