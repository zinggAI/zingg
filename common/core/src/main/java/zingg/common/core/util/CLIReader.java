package zingg.common.core.util;

import java.util.Scanner;

public class CLIReader {
    private static final String userInputRegex = "[0129]";

    public static int readCliInput() {
        Scanner sc = new Scanner(System.in);

        while (!sc.hasNext(userInputRegex)) {
            sc.next();
            System.out.println("Nope, please enter one of the allowed options!");
        }
        String word = sc.next();
        int selection = Integer.parseInt(word);
        // sc.close();

        return selection;
    }
}
