public class State {
    private byte[][] board;

    public State(int boardSize){
        if (boardSize < 1) {
            System.out.println("State size too small.");
            board = new byte[1][1];
        } else {
            int[][] board = new int[boardSize*2-1][boardSize*2-1];

            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if(Math.abs(i-j) > boardSize - 1) {
                        board[i][j] = -2;
                    }
                }
            }
        }
    }

    public void print() {
        for (int i = 0; i < board.length; i++) {
            if(i < board.length/2) {
                for (int j = 0; j < board.length/2-i; j++) {
                    System.out.print(" ");
                }
            }

            for (int j = 0; j < board[i].length; j++) {
                if(board[i][j] == 0) {
                    System.out.print(".");
                } else if(board[i][j] == 1) {
                    System.out.print("X");
                } else if(board[i][j] == 2) {
                    System.out.print("O");
                }

                System.out.print(" ");
            }

            System.out.println();
        }
    }
}
