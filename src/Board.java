import java.util.*;

public class Board {
    public static final int RADIUS = 5; //Set to 5 for normal board
    public static final int AREA = RADIUS*(3*RADIUS + 1); //Number of hexagons on the board
    private static final int MODULO = RADIUS + AREA;
    public static final int[] S = new int[]{1, 3*RADIUS+1, 3*RADIUS, MODULO-1, MODULO-3*RADIUS-1, MODULO-3*RADIUS};
    public static final int[] INDEX_TO_FILE = generateIndexToFile();
    private static int[] generateIndexToFile(){
        int[] indexToFile = new int[AREA];

        for (int i = 0; i < indexToFile.length; i++) {
            indexToFile[i] = (i % (RADIUS*3 + 1) + i/(RADIUS*3 + 1)) % (RADIUS*2);
        }

        return indexToFile;
    }
    public static final int[] INDEX_TO_RANK = generateIndexToRank();
    private static int[] generateIndexToRank(){
        int[] indexToRank = new int[AREA];

        for (int i = 0; i < indexToRank.length; i++) {
            int compressed = (i - INDEX_TO_FILE[i] + RADIUS);
            indexToRank[i] = (compressed - 1)/(3*RADIUS) + compressed % (3*RADIUS);
        }

        return indexToRank;
    }
    public static final int[] INDEX_TO_COL = generateIndexToCol();
    private static int[] generateIndexToCol(){
        int[] indexToCol = new int[AREA];

        for (int i = 0; i < indexToCol.length; i++) {
            indexToCol[i] = RADIUS - INDEX_TO_RANK[i] + INDEX_TO_FILE[i];
        }

        return indexToCol;
    }
    public static final int[] START_INDICES = generateStartIndices();
    private static int[] generateStartIndices(){
        int[] startIndices = new int[RADIUS*2];

        for (int i = 0; i < RADIUS; i++) {
            startIndices[i] = 0;
            startIndices[RADIUS + i] = i;
        }

        return startIndices;
    }
    public static final int[] END_INDICES = generateEndIndices();
    private static int[] generateEndIndices(){
        int[] endIndices = new int[RADIUS*2];

        for (int i = 0; i < RADIUS; i++) {
            endIndices[i] = RADIUS + i;
            endIndices[RADIUS + i] = 2*RADIUS - 1;
        }

        return endIndices;
    }
    public static final int[] COL_START_INDICES = generateColStartIndices();
    private static int[] generateColStartIndices(){
        int[] startIndices = new int[RADIUS*2+1];

        for (int i = 0; i < RADIUS; i++) {
            startIndices[i] = RADIUS - i;
            startIndices[RADIUS + i] = 0;
        }
        startIndices[RADIUS*2] = 0;

        return startIndices;
    }
    public static final int[] COL_END_INDICES = generateColEndIndices();
    private static int[] generateColEndIndices(){
        int[] endIndices = new int[RADIUS*2+1];

        for (int i = 0; i < RADIUS; i++) {
            endIndices[i] = 2*RADIUS - 1;
            endIndices[RADIUS + i] = 2*RADIUS - 1 - i;
        }
        endIndices[2*RADIUS] = RADIUS - 1;

        return endIndices;
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

    private static final Random R = new Random();
    private static final long[] Z_WHITE = new long[Board.AREA];
    private static final long[] Z_BLACK = new long[Board.AREA];
    private static final long[] Z_CAPTURE = new long[Board.AREA];
    private static final long Z_WTM = generateZValues();
    private static long generateZValues(){
        R.setSeed(57);
        Arrays.setAll(Z_WHITE, i -> R.nextLong());
        Arrays.setAll(Z_BLACK, i -> R.nextLong());
        Arrays.setAll(Z_CAPTURE, i -> R.nextLong());
        return R.nextLong();
    }

    private final Stones WHITE, BLACK;
    private final Stack<Move> MOVE_HISTORY;
    private boolean whiteToMove;
    private long zobristHash;

    public Board(Stones white, Stones black, Stack<Move> moveHistory, boolean whiteToMove, long zobristHash){
        this.WHITE = white;
        this.BLACK = black;

        this.MOVE_HISTORY = moveHistory;
        this.whiteToMove = whiteToMove;

        this.zobristHash = zobristHash;
    }

    public Board(boolean whiteToMove){
        this(new Stones(), new Stones(), new Stack<>(), whiteToMove, whiteToMove? Z_WTM:0);
//        for (long l : Z_WHITE) System.out.println(Long.toBinaryString(HashAgent.getZobristKey(l)));
//        for (long l : Z_BLACK) System.out.println(Long.toBinaryString(HashAgent.getZobristKey(l)));
//        for (long l : Z_CAPTURE) System.out.println(Long.toBinaryString(HashAgent.getZobristKey(l)));
//        System.out.println(Long.toBinaryString(HashAgent.getZobristKey(Z_WTM)));
    }

    public Board(){
        this(true);
    }

    public static class Stones {
        private final BitSet bitSet;
        private final char[] files, ranks, columns;

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

    public void makeMove(Move move){
        Stones player = whiteToMove? WHITE:BLACK;
        Stones opponent = whiteToMove? BLACK:WHITE;

        player.setStone(move.getINDEX()); // Set player stone
        zobristHash ^= whiteToMove ? Z_WHITE[move.getINDEX()] : Z_BLACK[move.getINDEX()]; // Set stone in hash

        if(!MOVE_HISTORY.isEmpty() && MOVE_HISTORY.peek().isCapture()){ // If previous move was capture
            zobristHash ^= Z_CAPTURE[MOVE_HISTORY.peek().getCapture()]; // Remove old capture from hash
        }

        if(move.isCapture()){ // If move is capture
            opponent.clearStone(move.getCapture()); // Remove opponent stone
            zobristHash ^= (whiteToMove ? Z_BLACK[move.getCapture()] : Z_WHITE[move.getCapture()]) ^ Z_CAPTURE[move.getCapture()]; // Remove opponent stone from; and add new capture to hash
        }

        MOVE_HISTORY.push(move); // Add move to history

        whiteToMove = !whiteToMove; // Flip white to move
        zobristHash ^= Z_WTM; // Flip white to move in hash
    }

    public void unmakeMove(){
        whiteToMove = !whiteToMove; // Flip white to move
        zobristHash ^= Z_WTM; // Flip white to move in hash

        Stones player = whiteToMove? WHITE:BLACK;
        Stones opponent = whiteToMove? BLACK:WHITE;

        Move previousMove = MOVE_HISTORY.pop(); // Get previous move and pop from history

        if(previousMove.isCapture()){ // If previous move is capture
            opponent.setStone(previousMove.getCapture()); // Reset opponent stone
            zobristHash ^= (whiteToMove ? Z_BLACK[previousMove.getCapture()] : Z_WHITE[previousMove.getCapture()]) ^ Z_CAPTURE[previousMove.getCapture()]; // Reset opponent stone and remove previous capture from hash
        }

        if(!MOVE_HISTORY.isEmpty() && MOVE_HISTORY.peek().isCapture()){ // If previous-previous move was capture
            zobristHash ^= Z_CAPTURE[MOVE_HISTORY.peek().getCapture()]; // Add old capture to hash
        }

        player.clearStone(previousMove.getINDEX()); //Remove player stone that was set previous move
        zobristHash ^= whiteToMove ? Z_WHITE[previousMove.getINDEX()] : Z_BLACK[previousMove.getINDEX()]; //Remove player stone that was set previous move from hash
    }

    public List<Move> getMoves(){
        List<Move> moves = new ArrayList<>();

        if((whiteToMove && checkWin(BLACK)) || (!whiteToMove && checkWin(WHITE))) return moves;

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

    public boolean isTerminal(){
        return getMoves().isEmpty();
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
    private static boolean isInCheck(Stones player, Stones opponent){

        int flippedPlayerBits;
        int opponentBits;

        for (int i = 0; i < 2*RADIUS; i++) {
            flippedPlayerBits = getRange(~player.getFiles()[i], Board.START_INDICES[i], Board.END_INDICES[i]);
            opponentBits = getRange(opponent.files[i], Board.START_INDICES[i], Board.END_INDICES[i]);
            if(isInCheck(flippedPlayerBits, opponentBits)) return true;

            flippedPlayerBits = getRange(~player.getRanks()[i], Board.START_INDICES[i], Board.END_INDICES[i]);
            opponentBits = getRange(opponent.ranks[i], Board.START_INDICES[i], Board.END_INDICES[i]);
            if(isInCheck(flippedPlayerBits, opponentBits)) return true;

            flippedPlayerBits = getRange(~player.getColumns()[i], Board.COL_START_INDICES[i], Board.COL_END_INDICES[i]);
            opponentBits = getRange(opponent.columns[i], Board.COL_START_INDICES[i], Board.COL_END_INDICES[i]);
            if(isInCheck(flippedPlayerBits, opponentBits)) return true;
        }

        flippedPlayerBits = getRange(~player.getColumns()[2*RADIUS], Board.COL_START_INDICES[2*RADIUS], Board.COL_END_INDICES[2*RADIUS]);
        opponentBits = getRange(opponent.columns[2*RADIUS], Board.COL_START_INDICES[2*RADIUS], Board.COL_END_INDICES[2*RADIUS]);
        return isInCheck(flippedPlayerBits, opponentBits);
    }
    private static boolean isInCheck(int flippedPlayerBits, int opponentBits){
        return ((flippedPlayerBits) & (opponentBits << 1) & (opponentBits << 2) & (opponentBits << 3) & (opponentBits << 4)) != 0
                || ((opponentBits) & (flippedPlayerBits << 1) & (opponentBits << 2) & (opponentBits << 3) & (opponentBits << 4)) != 0
                || ((opponentBits) & (opponentBits << 1) & (flippedPlayerBits << 2) & (opponentBits << 3) & (opponentBits << 4)) != 0
                || ((opponentBits) & (opponentBits << 1) & (opponentBits << 2) & (flippedPlayerBits << 3) & (opponentBits << 4)) != 0
                || ((opponentBits) & (opponentBits << 1) & (opponentBits << 2) & (opponentBits << 3) & (flippedPlayerBits << 4)) != 0;
    }
    public boolean checkWhiteWin(){
        return checkWin(WHITE);
    }
    public boolean checkBlackWin(){
        return checkWin(BLACK);
    }
    public boolean checkOpponentWin(){
        return checkWin(whiteToMove ? BLACK : WHITE);
    }
    public boolean isPlayerInCheck(){
        return isInCheck(whiteToMove? WHITE:BLACK, whiteToMove? BLACK:WHITE);
    }
    public boolean isOpponentInCheck(){
        return isInCheck(whiteToMove? BLACK:WHITE, whiteToMove? WHITE:BLACK);
    }

    public long calculateZobristHash(){
        long zobristHash = 0;

        for (int i = 0; i < Board.AREA; i++) {
            if(WHITE.bitSet.get(i)) zobristHash ^= Z_WHITE[i];
            else if(BLACK.bitSet.get(i)) zobristHash ^= Z_BLACK[i];
        }

        if(!MOVE_HISTORY.isEmpty() && MOVE_HISTORY.peek().isCapture()){
            zobristHash ^= Z_CAPTURE[MOVE_HISTORY.peek().getCapture()];
        }

        if(whiteToMove) zobristHash ^= Z_WTM;

        return zobristHash;
    }
    public long calculateZobristHash(Move move){
        long zobristHash = this.zobristHash;

        zobristHash ^= whiteToMove ? Z_WHITE[move.getINDEX()] : Z_BLACK[move.getINDEX()]; // Set stone in hash

        if(!MOVE_HISTORY.isEmpty() && MOVE_HISTORY.peek().isCapture()){ // If previous move was capture
            zobristHash ^= Z_CAPTURE[MOVE_HISTORY.peek().getCapture()]; // Remove old capture from hash
        }

        if(move.isCapture()){ // If move is capture
            zobristHash ^= (whiteToMove ? Z_BLACK[move.getCapture()] : Z_WHITE[move.getCapture()]) ^ Z_CAPTURE[move.getCapture()]; // Remove opponent stone from; and add new capture to hash
        }

        zobristHash ^= Z_WTM; // Flip white to move in hash

        return zobristHash;
    }

    public Stones getPlayerStones() {
        return whiteToMove ? WHITE : BLACK;
    }
    public Stones getOpponentStones() {
        return whiteToMove ? BLACK : WHITE;
    }
    public Stones getWhiteStones() {
        return WHITE;
    }
    public Stones getBlackStones() {
        return BLACK;
    }
    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public long getZobristHash(){
        return zobristHash;
    }

    public static int getRange(int bits, int start, int end){ // Returns range of bits from start to including end
        int shifted = bits>>>start;
        int mask = (1 << (end - start + 1)) - 1;
        return shifted & mask;
    }

    public Board copy(){
        return new Board(WHITE.copy(), BLACK.copy(), (Stack<Move>) MOVE_HISTORY.clone(), whiteToMove, zobristHash);
    }
    public void print(){
        System.out.println();
        System.out.println("[ " + (whiteToMove?"White":"Black")+ " to move. ]");
        for (int r = RADIUS*2-1; r >= 0; r--) { //Print board
            System.out.print(r + " | ");
            for (int f = 0; f < RADIUS*2; f++) { //Print file
                if(-RADIUS <= (r-f) && (r-f) <= RADIUS){
                    if(WHITE.bitSet.get(fileRankToIndex(f, r))){
                        System.out.print("O ");
                    } else if(BLACK.bitSet.get(fileRankToIndex(f, r))){
                        System.out.print("X ");
                    } else if(!MOVE_HISTORY.isEmpty() && MOVE_HISTORY.peek().getCapture() == (fileRankToIndex(f, r))){
                        System.out.print("_ ");
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
    public void printArray(){
        int[] array = new int[AREA+1];

        for (int i = 0; i < AREA; i++) {
            if(WHITE.getBitSet().get(i)) array[i] = 1;
            else if(BLACK.getBitSet().get(i)) array[i] = 2;
            else if(i == MOVE_HISTORY.peek().getCapture()) array[i] = -1;
        }

        array[AREA] = whiteToMove? 1:0;

        System.out.print(Arrays.toString(array));
    }
}
