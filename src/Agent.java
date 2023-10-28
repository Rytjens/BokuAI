import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Agent extends Player{
    public static final int[] SINGLES = generateSingles(); // Precalculated mapping from bitstring to number of set bits
    public static final int[] DOUBLES = generateDoubles(); // Precalculated mapping from bitstring to number of adjacent bits
    private static final int[] TRIPLES = generateTriples(); // Precalculated mapping from bitstring to number of triple adjacent bits
    private static final int[] QUADRUPLES = generateQuadruples(); // Precalculated mapping from bitstring to number of quadruple adjacent bits
    private static final int[] QUINTUPLES = generateQuintuples(); // Precalculated mapping from bitstring to number of quadruple adjacent bits

    private static int[] generateSingles(){
        int[] singles = new int[1 << 2*Board.RADIUS];

        Arrays.setAll(singles, Integer::bitCount);

        return singles;
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
    private static int[] generateQuintuples(){
        int[] doubles = new int[1 << 2*Board.RADIUS];

        Arrays.setAll(doubles, i -> Integer.bitCount(i & (i << 1) & (i << 2) & (i << 3) & (i << 4)));

        return doubles;
    }


    private final double MAX_TIME;
    private final int MAX_DEPTH = 16; // Rather arbitrary
    private final HashMap<Long, TTEntry> TT;
    private static final int HASH_KEY_LENGTH = 48;

    public Agent(double seconds){
        this.MAX_TIME = seconds * 1_000_000_000.0;
        this.TT = new HashMap<>();
    }

    private static class TTEntry {
        private final long HASH;
        private final Move MOVE; //Move
        private final int VALUE; // Value of move
        private final int FLAG; //
        private final int DEPTH;

        public static final TTEntry NO_ENTRY = new TTEntry(0, null, -Integer.MAX_VALUE, 0, -1);

        public TTEntry(long hash, Move move, int value, int flag, int depth){
            this.HASH = hash;
            this.MOVE = move;
            this.VALUE = value;
            this.FLAG = flag;
            this.DEPTH = depth;
        }
    }

    private int nodeCount, pruneCount, hashWrites, hashOverwrites, hashReads, hashHits;
    private Move iterationBestRootMove;
    private int iterationBestValue;
    private Move[] killerMoves;
    private double startTime;
    private final int WINNING_VALUE = 1_000_000_000;
    private boolean stopSearch;
    public Move getMove(Board board){
        nodeCount = pruneCount = 0;
        hashWrites = hashOverwrites = hashReads = hashHits = 0;
        startTime = System.nanoTime();
        stopSearch = false;

        Board boardCopy = board.copy();

        Move bestMove = null;
        killerMoves = new Move[MAX_DEPTH];

        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            System.out.println("Starting iteration: " + depth);

            iterationBestRootMove = null;
            iterationBestValue = -Integer.MAX_VALUE;
            alphaBeta(boardCopy, 0, depth, -Integer.MAX_VALUE,Integer.MAX_VALUE); //Initial AlphaBeta

            if(iterationBestRootMove != null){
                bestMove = iterationBestRootMove;
                System.out.println("Iteration result: " + iterationBestValue + " ");
                bestMove.print();
                System.out.println();
            }

            if(System.nanoTime()-startTime > MAX_TIME
                    || iterationBestValue == WINNING_VALUE
                    || iterationBestValue == -WINNING_VALUE) break;
        }

        System.out.println();
        System.out.println("Number of nodes visited: " + nodeCount);
        System.out.println("Number of nodes pruned: " + pruneCount);
        System.out.println("Hash W:" + hashWrites +" O:" + hashOverwrites + " R:" + hashReads + " H:" + hashHits);

        double end = System.nanoTime();
        System.out.println("Time: " + (end - startTime)/1_000_000.0 + "ms");

        return bestMove;
    }

    private int alphaBeta(Board board, int plyFromRoot, int depth, int alpha, int beta){
        if(System.nanoTime() - startTime > MAX_TIME) {
            stopSearch = true;
            return 0;
        }
        nodeCount++;
        int oldAlpha = alpha;
        TTEntry ttEntry = getTTEntry(board.getZobristHash());

        if(ttEntry.DEPTH >= depth){ // if board was evaluated earlier to a greater depth
            if(ttEntry.FLAG == 0) { // Check if value is exact
                if(plyFromRoot == 0){
                    iterationBestRootMove = ttEntry.MOVE;
                    iterationBestValue = ttEntry.VALUE;
                }
                return ttEntry.VALUE;
            } else if(ttEntry.FLAG < 0){ // Check if value is lowerbound
                if(ttEntry.VALUE > alpha) alpha = ttEntry.VALUE;
            } else { // Check if value is upperbound
                if(ttEntry.VALUE < beta) beta = ttEntry.VALUE;
            }
            if(alpha >= beta) return ttEntry.VALUE;
        }

        if(depth == 0){ // check if leaf
            return evaluate(board);
        }

        int bestValue = -Integer.MAX_VALUE;
        Move bestMove = null;

        if (ttEntry.DEPTH >= 0){ // explore best found move from previous iteration first
            board.makeMove(ttEntry.MOVE);
            bestValue = -alphaBeta(board, plyFromRoot + 1, depth - 1, -beta, -alpha);
            if(stopSearch) return 0;
            board.unmakeMove();

            bestMove = ttEntry.MOVE;
            if(plyFromRoot == 0){
                iterationBestRootMove = bestMove;
                iterationBestValue = bestValue;
            }

            if(bestValue >= beta){
                storeInTT(board.getZobristHash(), bestMove, bestValue, bestValue <= oldAlpha ? 1 : -1, depth);
                return bestValue;
            }
        }

        List<Move> moveList = board.getMoves();

        if(moveList.isEmpty()){ // Sort to check killermove first
            return evaluate(board);
        }

        sortMoves(moveList, plyFromRoot);

//        System.out.println("[  Depth: " + depth + " " + (board.isWhiteToMove()?"White":"Black") + " to move.  ]");
        for (Move move : moveList) {
            board.makeMove(move);
            int value = -alphaBeta(board, plyFromRoot + 1, depth - 1, -beta, -alpha);
            if(stopSearch) return 0;
            board.unmakeMove();

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
                if(plyFromRoot == 0){
                    iterationBestRootMove = bestMove;
                    iterationBestValue = bestValue;
                }
            }
            if (bestValue > alpha) alpha = bestValue;
            if (alpha >= beta) {
                killerMoves[plyFromRoot] = move; // Update killer move at current level
                pruneCount++;
                break;
            }
        }

        storeInTT(board.getZobristHash(), bestMove, bestValue, ((bestValue <= oldAlpha) ? 1 : ((bestValue >= beta) ? -1 : 0)), depth);
//        System.out.println("Best value: " + bestValue);
        return bestValue;
    }

    private int evaluate(Board board){ // Give the board evaluation for the player to move, positive is better.
        if(board.checkOpponentWin()){ //Check if opponent has won.
            return -WINNING_VALUE;
        }

        if(board.countOpponentInChecks() > 0) {
            return 1_000_000 * board.countOpponentInChecks();
        }

        int evaluation = 25;

        evaluation -= 1_000 * board.countPlayerInChecks();

        evaluation += 20 * board.countOpponentInPreChecks();
        evaluation -= 20 * board.countPlayerInPreChecks();

        evaluation += 2*countMapping(board.getPlayerStones(), SINGLES); //Material Advantage
        evaluation -= 2*countMapping(board.getOpponentStones(), SINGLES);

        evaluation += 2*countMapping(board.getPlayerStones(), DOUBLES);
        evaluation -= 2*countMapping(board.getOpponentStones(), DOUBLES);

        evaluation += 3*countMapping(board.getPlayerStones(), TRIPLES);
        evaluation -= 3*countMapping(board.getOpponentStones(), TRIPLES);

        evaluation += 5*countMapping(board.getPlayerStones(), QUADRUPLES);
        evaluation -= 5*countMapping(board.getOpponentStones(), QUADRUPLES);

        evaluation += countEmpty5(board.getOpponentStones()); // Mobility, spaces left where a win could fit the board
        evaluation -= countEmpty5(board.getPlayerStones());

        return evaluation;
    }

    private void sortMoves(List<Move> moveList, int plyFromRoot){
        if(killerMoves[plyFromRoot] == null || moveList.size() < 2){
            return;
        }

        for (int i = 1; i < moveList.size(); i++) {
            Move move = moveList.get(i);
            if (killerMoves[plyFromRoot].equals(move)) {
                moveList.set(i, moveList.get(0));
                moveList.set(0, move);
                return;
            }
        }
    }

    public static long getZobristKey(long zobristHash){
        return zobristHash & (~((~0L) << HASH_KEY_LENGTH));
    }

    private void storeInTT(long zobristHash, Move move, int bestValue, int flag, int depth){
        hashWrites++;

        long key = getZobristKey(zobristHash);

        if(TT.containsKey(key) && TT.get(key).HASH != zobristHash){
            hashOverwrites++;
        }

        TT.put(key, new TTEntry(zobristHash, move, bestValue, flag, depth));
    }
    private TTEntry getTTEntry(long zobristHash){
        hashReads++;
        long key = getZobristKey(zobristHash);

        if (!TT.containsKey(key)){
            return TTEntry.NO_ENTRY;
        }

        TTEntry entry = TT.get(key);

        if(zobristHash == entry.HASH){
            hashHits++;
            return TT.get(key);
        }

        return TTEntry.NO_ENTRY;
    }
    private static int countMapping(Board.Stones stones, int[] mapping){
        int count = 0;

        for (int i = 0; i < 2*Board.RADIUS; i++) {
            count += mapping[stones.getFiles()[i]];
            count += mapping[stones.getRanks()[i]];
            count += mapping[stones.getColumns()[i]];
        }
        count += mapping[stones.getColumns()[2*Board.RADIUS]];

        return count;
    }
    public static int countEmpty5(Board.Stones stones){
        int count = 0;

        for (int i = 0; i < 2*Board.RADIUS; i++) {
            count += QUINTUPLES[Board.getRange(~stones.getFiles()[i], Board.START_INDICES[i], Board.END_INDICES[i])];
            count += QUINTUPLES[Board.getRange(~stones.getRanks()[i], Board.START_INDICES[i], Board.END_INDICES[i])];
            count += QUINTUPLES[Board.getRange(~stones.getColumns()[i], Board.COL_START_INDICES[i], Board.COL_END_INDICES[i])];
        }
        count += QUINTUPLES[Board.getRange(stones.getColumns()[2*Board.RADIUS], Board.COL_START_INDICES[2*Board.RADIUS], Board.COL_END_INDICES[2*Board.RADIUS])];

        return count;
    }
}
