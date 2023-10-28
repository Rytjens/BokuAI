import java.util.Scanner;

public class Human extends Player{
    @Override
    Move getMove(Board board) {
        int index = promptIndex();

        boolean[] captures = board.getCaptures(index);

        if(allFalse(captures)) {
            return new Move(index, -1);
        }

        System.out.println("You must capture.");

        int[] captureIndices = Board.getCaptureIndices(index, captures);

        int captureIndex = promptIndex();

        while (!contains(captureIndices, captureIndex)) {
            System.out.println("Not a valid capture, try again.");
            captureIndex = promptIndex();
        }

        return new Move(index, captureIndex);
    }

    private int promptIndex(){
        Scanner scanner = new Scanner(System.in);

        int file, rank;

        do {
            System.out.print("File: ");
            while (!scanner.hasNextInt()){
                System.out.println("Not a valid number. Try again.");
                scanner.nextLine();
                System.out.print("File: ");
            }
            file = scanner.nextInt();

            System.out.print("Rank: ");
            while (!scanner.hasNextInt()){
                System.out.println("Not a valid number. Try again.");
                scanner.nextLine();
                System.out.print("Rank: ");
            }
            rank = scanner.nextInt();

            System.out.println();
        } while (Board.fileRankToIndex(file, rank) < 0);

        return Board.fileRankToIndex(file, rank);
    }

    private static boolean allFalse(boolean[] array){
        for(boolean b: array){
            if(b) return false;
        }
        return true;
    }

    private static boolean contains(int[] array, int element){
        for (int i: array){
            if(i == element) return true;
        }
        return false;
    }
}
