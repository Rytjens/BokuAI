import java.util.Arrays;

public class Boku {
    private GameState gameState;

    public Boku() {
        gameState = new GameState();
        for(GameState g: gameState.getPossibleStates()){
            for (GameState h: g.getPossibleStates()) {
                for (GameState i: h.getPossibleStates()) {
                    for (GameState j: i.getPossibleStates()) {
                        System.out.println(j.getPossibleStates().length);
                    }
                }
            }
        }
    }

    public int checkWin() {
        return 0;
    }


}
