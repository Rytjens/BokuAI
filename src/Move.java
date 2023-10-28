public class Move {
    private final int INDEX;
    private final int CAPTURE;

    public Move(int index, int capture){
        INDEX = index;
        CAPTURE = capture;
    }

    public Move(int index){
        this(index, -1);
    }

    public int getINDEX(){
        return INDEX;
    }
    public boolean isCapture(){
        return CAPTURE >= 0;
    }
    public int getCapture(){
        return CAPTURE;
    }

    public boolean equals(Move move){
        return (this.INDEX == move.INDEX && this.CAPTURE == move.CAPTURE);
    }

    public void print(){
        System.out.print("File: " + Board.INDEX_TO_FILE[INDEX] + " Rank: " + Board.INDEX_TO_RANK[INDEX]);
//        System.out.print("Index: " + INDEX);
        if(isCapture()){
            System.out.print(" | Capture - File: " + Board.INDEX_TO_FILE[CAPTURE] + " Rank: " + Board.INDEX_TO_RANK[CAPTURE]);
//            System.out.println(" | Capture - " + CAPTURE);
        } else {
            System.out.print(" | No capture.");
        }
    }
}
