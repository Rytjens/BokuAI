import java.util.Arrays;
import java.util.BitSet;

public class OldGameState {
    private static final byte RADIUS = 2; //Must be > 1 and < 7
    private static final byte AREA = 3*RADIUS*(RADIUS+1)+1;
    private static final byte[] S = new byte[]{1, 3*RADIUS+2, 3*RADIUS+1, AREA-1, AREA-3*RADIUS-2, AREA-3*RADIUS-1};
    private static final byte[] ROW_SIZES = getRowSizes();
    private static final byte[] START_INDICES = getStartIndices();
    private static final BitSet[] CAPTURE_MASKS = getCaptureMasks();
    private static final byte[][][] CAPTURE_TRIPLETS = getCaptureTriplets();

    private final BitSet PLAYER_BITSET, OPPONENT_BITSET;
    private final byte ILLEGAL_POSITION;

    public OldGameState(){
        this(new BitSet(AREA),
             new BitSet(AREA),
                (byte) -1);
    }

    public OldGameState(BitSet playerBitSet, BitSet opponentBitSet, byte illegalPosition) {
        PLAYER_BITSET = playerBitSet;
        OPPONENT_BITSET = opponentBitSet;
        ILLEGAL_POSITION = illegalPosition;
    }

    public OldGameState[] getPossibleStates() {
        BitSet possibleMoves = getPossibleMoves();
        OldGameState[] possibleGameStates = new OldGameState[13*possibleMoves.cardinality()];
        int index = 0;
        for (int i = possibleMoves.nextSetBit(0); i >= 0; i = possibleMoves.nextSetBit(i+1)) {
            BitSet newPlayerBitSet = (BitSet)PLAYER_BITSET.clone();
            newPlayerBitSet.set(i);
            possibleGameStates[index++] = new OldGameState(OPPONENT_BITSET, newPlayerBitSet, (byte) -1);
            possibleGameStates[index-1].print();

            for (int j = 0; j < CAPTURE_TRIPLETS[i].length; j++) {
                if(OPPONENT_BITSET.get(CAPTURE_TRIPLETS[i][j][0]) && OPPONENT_BITSET.get(CAPTURE_TRIPLETS[i][j][1]) && PLAYER_BITSET.get(CAPTURE_TRIPLETS[i][j][2])){
                    BitSet firstNewOpponentBitSet = (BitSet)OPPONENT_BITSET.clone();
                    firstNewOpponentBitSet.clear(CAPTURE_TRIPLETS[i][j][0]);
                    possibleGameStates[index++] = new OldGameState(firstNewOpponentBitSet, newPlayerBitSet, CAPTURE_TRIPLETS[i][j][0]);

                    BitSet secondNewOpponentBitSet = (BitSet)OPPONENT_BITSET.clone();
                    secondNewOpponentBitSet.clear(CAPTURE_TRIPLETS[i][j][1]);
                    possibleGameStates[index++] = new OldGameState(secondNewOpponentBitSet, newPlayerBitSet, CAPTURE_TRIPLETS[i][j][1]);
                    System.out.println("removed balls!");
                }
            }
        }

        return Arrays.copyOf(possibleGameStates, index);
    }

    private BitSet getPossibleMoves() {
        BitSet possibleMoves = (BitSet)PLAYER_BITSET.clone();
        possibleMoves.or(OPPONENT_BITSET);
        possibleMoves.flip(0, AREA);

        if(ILLEGAL_POSITION >= 0) {
            possibleMoves.clear(ILLEGAL_POSITION);
        }

        return possibleMoves;
    }

    private static BitSet[] getCaptureMasks() {
        BitSet[] captureMasks = new BitSet[6];
        for (int i = 0; i < 6; i++) {
            captureMasks[i] = new BitSet(AREA);
            for (int j = (RADIUS-2)*S[i]; j < (RADIUS+1)*S[i]; j+=S[i]) {
                captureMasks[i].set(j%AREA);
                for (int k = 1; k < RADIUS + 1; k++) {
                    captureMasks[i].set((j + k*S[(i+2)%6])%AREA);
                    captureMasks[i].set((j + k*S[(i+4)%6])%AREA);
                }
            }
            captureMasks[i].flip(0, AREA);
        }

        return captureMasks;
    }

    private static byte[][][] getCaptureTriplets() {
        byte [][][] captureSpokes = new byte[AREA][][];

        for (int i = 0; i < captureSpokes.length; i++) {
            byte[][] tempSpokes = new byte[6][];
            int count = 0;
            for (int j = 0; j < 6; j++) {
                if(CAPTURE_MASKS[j].get(i)){
                    tempSpokes[count++] = new byte[]{(byte) ((i+S[j])%AREA), (byte) ((i+2*S[j])%AREA), (byte) ((i+3*S[j])%AREA)};
                }
            }
            captureSpokes[i] = new byte[count][];
            System.arraycopy(tempSpokes, 0, captureSpokes[i], 0, count);
        }

        return captureSpokes;
    }

    public void print() {
        System.out.print("Printing GameState: ");
        for (int i = 0; i < 2*RADIUS+1; i++) {
            for (int j = 0; j < ROW_SIZES[i]; j++) {
                if(PLAYER_BITSET.get((START_INDICES[i]+S[0]*j)%AREA)) {
                    System.out.print("X ");
                } else if (OPPONENT_BITSET.get((START_INDICES[i]+S[0]*j)%AREA)) {
                    System.out.print("O ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
    }

    // UTILS

    private static byte[] getRowSizes() {
        byte[] rowSizes = new byte[2*RADIUS+1];

        for (int i = 0; i < RADIUS + 1; i++) {
            rowSizes[i] = rowSizes[rowSizes.length-i-1] = (byte) (RADIUS+i+1);
        }

        return rowSizes;
    }

    private static byte[] getStartIndices() {
        byte[] startIndices = new byte[ROW_SIZES.length];

        startIndices[0] = (byte) ((S[2]*RADIUS)%AREA);
        for (int i = 1; i < RADIUS+1; i++) {
            startIndices[i] = (byte) ((startIndices[i-1] + S[4])%AREA);
        }

        for (int i = RADIUS+1; i < startIndices.length; i++) {
            startIndices[i] = (byte) ((startIndices[i-1] + S[5])%AREA);
        }

        return startIndices;
    }
}
