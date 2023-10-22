public class Move {
    private final int INDEX;
    private final int CAPTURE;
    private final int PREV_ILLEGAL;

    public Move(int index, int capture, int prevIllegal){
        INDEX = index;
        CAPTURE = capture;
        PREV_ILLEGAL = prevIllegal;
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

    public boolean isPrevIllegal(){
        return PREV_ILLEGAL >= 0;
    }

    public int getPrevIllegal(){
        return PREV_ILLEGAL;
    }
}
