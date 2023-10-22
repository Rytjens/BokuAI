import java.util.BitSet;

public class Board {
    public static final int RADIUS = 5; //Set to 5 for normal board
    public static final int AREA = RADIUS*(3*RADIUS + 1); //Number of hexagons on the board
    private static final int MODULO = RADIUS + AREA;

    public static final int[] INDEX_TO_FILE = generateIndexToFile();
    public static final int[] INDEX_TO_RANK = generateIndexToRank();
    public static final int[] INDEX_TO_COL = generateIndexToCol();

    private final Player HUMAN, COMPUTER;
    private int illegalIndex;
    private boolean humanToMove;

    public Board(boolean playerToMove){
        this.HUMAN = new Player();
        this.COMPUTER = new Player();

        this.illegalIndex = -1;
        this.humanToMove = playerToMove;
    }

    public Board(){
        this(true);
    }

    private class Player {
        protected BitSet stones;
        private char[] files, ranks, columns;

        protected Player(){
            this.stones = new BitSet(AREA);
            this.files = new char[2*RADIUS];
            this.ranks = new char[2*RADIUS];
            this.columns = new char[2*RADIUS+1];
        }

        protected void flipStone(int index){
            stones.set(index);
            files[INDEX_TO_FILE[index]] ^= (1<<INDEX_TO_RANK[index]);
            ranks[INDEX_TO_RANK[index]] ^= (1<<INDEX_TO_FILE[index]);
            columns[INDEX_TO_COL[index]] ^= (1<<INDEX_TO_RANK[index]);
        }
    }

    public void makeMove(Move move){
        if(humanToMove){
            HUMAN.flipStone(move.getINDEX());
        } else {
            COMPUTER.flipStone(move.getINDEX());
        }

        if(move.isCapture()){
            if(humanToMove){
                COMPUTER.flipStone(move.getCapture());
            } else {
                HUMAN.flipStone(move.getCapture());
            }
            illegalIndex = move.getINDEX();
        } else {
            illegalIndex = -1;
        }

        humanToMove = !humanToMove;
    }

    public void unmakeMove(Move move){
        humanToMove = !humanToMove;

        if(move.isCapture()){
            if(humanToMove){
                COMPUTER.flipStone(move.getCapture());
            } else {
                HUMAN.flipStone(move.getCapture());
            }
        }

        illegalIndex = move.getPrevIllegal();

        if(humanToMove){
            HUMAN.flipStone(move.getINDEX());
        } else {
            COMPUTER.flipStone(move.getINDEX());
        }
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
        return (2*RADIUS + file + 3*RADIUS*rank) % MODULO;
    }

    public void print(){
        for (int r = RADIUS*2-1; r >= 0; r--) {
            System.out.print(r + " | ");
            for (int f = 0; f < RADIUS*2; f++) {
                if(-RADIUS <= (r-f) && (r-f) <= RADIUS){
                    if(HUMAN.stones.get(fileRankToIndex(f, r))){
                        System.out.print("X ");
                    } else if(COMPUTER.stones.get(fileRankToIndex(f, r))){
                        System.out.print("C ");
                    } else {
                        System.out.print(". ");
                    }
                } else {
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
        System.out.println("    -------------------");
        System.out.println("    A B C D E F G H I J");
    }
}
