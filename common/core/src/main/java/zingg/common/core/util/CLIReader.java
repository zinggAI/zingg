package zingg.common.core.util;

import java.util.Scanner;

/*
* Generic Class CLIReader for reading user inputs.
* Independent of labelling logic
* */

public class CLIReader {
    private Scanner scanner;

    public CLIReader() {
        this.scanner = new Scanner(System.in);
    }

    public int readCliInput(String userInputRegex) {
        while (!scanner.hasNext(userInputRegex)) {
            scanner.next();
            System.out.println("Nope, please enter one of the allowed options!");
        }
        String word = scanner.next();

        return Integer.parseInt(word);
    }
}
