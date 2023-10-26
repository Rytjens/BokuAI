import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Stack;

public class Board {
    public static final int RADIUS = 5; //Set to 5 for normal board
    public static final int AREA = RADIUS*(3*RADIUS + 1); //Number of hexagons on the board
    private static final int MODULO = RADIUS + AREA;
    public static final int[] S = new int[]{1, 3*RADIUS+1, 3*RADIUS, MODULO-1, MODULO-3*RADIUS-1, MODULO-3*RADIUS};
    public static final int[] INDEX_TO_FILE = generateIndexToFile();
    public static final int[] INDEX_TO_RANK = generateIndexToRank();
    public static final int[] INDEX_TO_COL = generateIndexToCol();

    private final Stones WHITE, BLACK;
    private final Stack<Move> MOVE_HISTORY;
    private boolean whiteToMove;

    public Board(Stones white, Stones black, Stack<Move> moveHistory, boolean whiteToMove){
        this.WHITE = white;
        this.BLACK = black;

        this.MOVE_HISTORY = moveHistory;
        this.whiteToMove = whiteToMove;
    }

    public Board(boolean playerToMove){
        this(new Stones(), new Stones(), new Stack<>(), playerToMove);
    }

    public Board(){
        this(true);
    }

    public static class Stones {
        private BitSet bitSet;
        private char[] files, ranks, columns;

        private Stones(BitSet stones, char[] files, char[] ranks, char[] columns){
            this.bitSet = stones;
            this.files = files;
            this.ranks = ranks;
            this.columns = columns;
        }
        private Stones(){
            this(new BitSet(AREA),
                    new char[2*RADIUS],
                    new char[2*RADIUS],
                    new char[2*RADIUS+1]);
        }

        private void setStone(int index){
            bitSet.set(index);
            files[INDEX_TO_FILE[index]] |= (1<<INDEX_TO_RANK[index]);
            ranks[INDEX_TO_RANK[index]] |= (1<<INDEX_TO_FILE[index]);
            columns[INDEX_TO_COL[index]] |= (1<<INDEX_TO_RANK[index]);
        }

        private void clearStone(int index){
            bitSet.clear(index);
            files[INDEX_TO_FILE[index]] &= ~(1<<INDEX_TO_RANK[index]);
            ranks[INDEX_TO_RANK[index]] &= ~(1<<INDEX_TO_FILE[index]);
            columns[INDEX_TO_COL[index]] &= ~(1<<INDEX_TO_RANK[index]);
        }

        public BitSet getBitSet() {
            return bitSet;
        }

        public char[] getFiles() {
            return files;
        }

        public char[] getRanks() {
            return ranks;
        }

        public char[] getColumns() {
            return columns;
        }

        private Stones copy() {
            return new Stones((BitSet) bitSet.clone(), files.clone(), ranks.clone(), columns.clone());
        }
    }

    public boolean makeMove(Move move){
        if(!isValidMove(move)){
            return false;
        }

        Stones player = whiteToMove? WHITE:BLACK;
        Stones opponent = whiteToMove? BLACK:WHITE;

        player.setStone(move.getINDEX());

        if(move.isCapture()){
            opponent.clearStone(move.getCapture());
        }

        MOVE_HISTORY.push(move);
        whiteToMove = !whiteToMove;
        return true;
    }

    public boolean isValidMove(Move move){ //TODO: implement
        if(move.getINDEX() < 0
                || move.getINDEX() >= AREA
                || move.getCapture() >= AREA){
            return false;
        }

        Stones player = whiteToMove? WHITE:BLACK;
        Stones opponent = whiteToMove? BLACK:WHITE;

        return true;
    }

    public List<Move> getMoves(){
        List<Move> moves = new ArrayList<>();

        BitSet illegalSquares = (BitSet) WHITE.bitSet.clone();
        illegalSquares.or(BLACK.bitSet);
        if(!MOVE_HISTORY.isEmpty() && MOVE_HISTORY.peek().isCapture()){
            illegalSquares.set(MOVE_HISTORY.peek().getCapture());
        }

        for (int index = illegalSquares.nextClearBit(0); index >= 0 && index < AREA; index = illegalSquares.nextClearBit(index + 1)) {
            boolean[] captures = getCaptures(index);
            boolean capture = false;

            for (int j = 0; j < 6; j++) {
                if(captures[j]){
                    capture = true;
                    moves.add(new Move(index, (index + S[j]) % MODULO));
                    moves.add(new Move(index, (index + 2*S[j]) % MODULO));
                }
            }

            if(!capture){
                moves.add(new Move(index));
            }
        }

        return moves;
    }

    public boolean[] getCaptures(int index){
        boolean[] captures = new boolean[6];

        Stones player = whiteToMove? WHITE:BLACK;
        Stones opponent = whiteToMove? BLACK:WHITE;

        captures[0] = isCapture(player.ranks[INDEX_TO_RANK[index]], opponent.ranks[INDEX_TO_RANK[index]], INDEX_TO_FILE[index], true);
        captures[1] = isCapture(player.columns[INDEX_TO_COL[index]], opponent.columns[INDEX_TO_COL[index]], INDEX_TO_RANK[index], true);
        captures[2] = isCapture(player.files[INDEX_TO_FILE[index]], opponent.files[INDEX_TO_FILE[index]], INDEX_TO_RANK[index], true);
        captures[3] = isCapture(player.ranks[INDEX_TO_RANK[index]], opponent.ranks[INDEX_TO_RANK[index]], INDEX_TO_FILE[index], false);
        captures[4] = isCapture(player.columns[INDEX_TO_COL[index]], opponent.columns[INDEX_TO_COL[index]], INDEX_TO_RANK[index], false);
        captures[5] = isCapture(player.files[INDEX_TO_FILE[index]], opponent.files[INDEX_TO_FILE[index]], INDEX_TO_RANK[index], false);

        return captures;
    }

    private static boolean isCapture(char player, char opponent, int index, boolean posDir){
        if(posDir){
            return ((opponent >> 1) & (opponent >> 2) & (player >> 3) & (1 << index)) != 0;
        } else {
            return ((opponent << 1) & (opponent << 2) & (player << 3) & (1 << index)) != 0;
        }
    }

    public static int[] getCaptureIndices(int index, boolean[] captures){
        int[] captureIndices = new int[12];

        for (int j = 0; j < 6; j++) {
            if(captures[j]){
                captureIndices[j] = (index + S[j]) % MODULO;
                captureIndices[j+1] = (index + 2*S[j]) % MODULO;
            } else {
                captureIndices[j] = -1;
                captureIndices[j+1] = -1;
            }
        }

        return captureIndices;
    }

    public void unmakeMove(){
        whiteToMove = !whiteToMove;

        Stones player = whiteToMove? WHITE:BLACK;
        Stones opponent = whiteToMove? BLACK:WHITE;

        if(MOVE_HISTORY.peek().isCapture()){
            opponent.setStone(MOVE_HISTORY.peek().getCapture()); //Set the stone that was captured previous move.
        }

        player.clearStone(MOVE_HISTORY.peek().getINDEX()); //Clear the stone that was placed previous move.

        MOVE_HISTORY.pop();
    }

    public boolean isTerminal(){
        if(checkWin(WHITE)) {
            return true;
        }

        if(checkWin(BLACK)) {
            return true;
        }

        return false;
    }

    private static boolean checkWin(Stones stones){
        for (int i = 0; i < 2*RADIUS; i++) {
            if ((stones.ranks[i] & (stones.ranks[i] >> 1) & (stones.ranks[i] >> 2) & (stones.ranks[i] >> 3) & (stones.ranks[i] >> 4)) != 0
                    || (stones.files[i] & (stones.files[i] >> 1) & (stones.files[i] >> 2) & (stones.files[i] >> 3) & (stones.files[i] >> 4)) != 0
                    || (stones.columns[i] & (stones.columns[i] >> 1) & (stones.columns[i] >> 2) & (stones.columns[i] >> 3) & (stones.columns[i] >> 4)) != 0) {
                return true;
            }
        }
        return (stones.columns[2*RADIUS] & (stones.columns[2*RADIUS] >> 1) & (stones.columns[2*RADIUS] >> 2) & (stones.columns[2*RADIUS] >> 3) & (stones.columns[2*RADIUS] >> 4)) != 0;
    }

    public boolean checkWhiteWin(){
        return checkWin(WHITE);
    }
    public boolean checkBlackWin(){
        return checkWin(BLACK);
    }
    public boolean checkPlayerWin(){
        return checkWin(whiteToMove ? WHITE : BLACK);
    }
    public boolean checkOpponentWin(){
        return checkWin(whiteToMove ? BLACK : WHITE);
    }
    
    private static int[] generateIndexToFile(){
        int[] indexToFile = new int[AREA];

        for (int i = 0; i < indexToFile.length; i++) {
            indexToFile[i] = (i % (RADIUS*3 + 1) + i/(RADIUS*3 + 1)) % (RADIUS*2);
        }
        
        return indexToFile;
    }
    private static int[] generateIndexToRank(){
        int[] indexToRank = new int[AREA];

        for (int i = 0; i < indexToRank.length; i++) {
            int compressed = (i - INDEX_TO_FILE[i] + RADIUS);
            indexToRank[i] = (compressed - 1)/(3*RADIUS) + compressed % (3*RADIUS);
        }

        return indexToRank;
    }
    private static int[] generateIndexToCol(){
        int[] indexToCol = new int[AREA];

        for (int i = 0; i < indexToCol.length; i++) {
            indexToCol[i] = RADIUS - INDEX_TO_RANK[i] + INDEX_TO_FILE[i];
        }

        return indexToCol;
    }
    public static int fileRankToIndex(int file, int rank){
        if (file < 0
        || file >= 2 * RADIUS
        || rank < 0
        || rank >= 2 * RADIUS
        || (file - rank) > RADIUS
        || (rank - file) > RADIUS) {
            System.out.println("Invalid file/rank combination.");
            return -1;
        }

        return (2*RADIUS + file + 3*RADIUS*rank) % MODULO;
    }

    public Stones getPlayerStones() {
        return whiteToMove ? WHITE : BLACK;
    }
    public Stones getOpponentStones() {
        return whiteToMove ? BLACK : WHITE;
    }
    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public Board copy(){
        return new Board(WHITE.copy(), BLACK.copy(), (Stack<Move>) MOVE_HISTORY.clone(), whiteToMove);
    }
    public void print(){
        for (int r = RADIUS*2-1; r >= 0; r--) { //Print board
            System.out.print(r + " | ");
            for (int f = 0; f < RADIUS*2; f++) { //Print file
                if(-RADIUS <= (r-f) && (r-f) <= RADIUS){
                    if(WHITE.bitSet.get(fileRankToIndex(f, r))){
                        System.out.print("O ");
                    } else if(BLACK.bitSet.get(fileRankToIndex(f, r))){
                        System.out.print("X ");
                    } else {
                        System.out.print(". ");
                    }
                } else {
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
        System.out.println("R   -------------------");
        System.out.println("  F 0 1 2 3 4 5 6 7 8 9");
    }
}
