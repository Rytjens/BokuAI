import java.util.Collections;
import java.util.List;

public class RandomAgent extends Player{
    @Override
    Move getMove(Board board) {
        List<Move> moves = board.getMoves();
        Collections.shuffle(moves);
        return moves.get(0);
    }
}
