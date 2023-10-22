import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
//        Boku boku = new Boku();

        Scanner scanner = new Scanner(System.in);

        Board board = new Board();
        board.print();
        while (true){
            System.out.print("File:");
            int file = Integer.parseInt(scanner.nextLine());
            System.out.print("Rank:");
            int rank = Integer.parseInt(scanner.nextLine());
            board.makeMove(new Move(Board.fileRankToIndex(file, rank),-1,-1));
            board.print();
        }
    }

}