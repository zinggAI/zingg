package zingg.common.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvReader {
    protected List<? extends IFromCsv> records;
    IFromCsv creator;

    public CsvReader(IFromCsv creator){
        records = new ArrayList<IFromCsv>();
        this.creator = creator;
    }

    public List<? extends IFromCsv> getRecords(String file, boolean skipHeader) throws FileNotFoundException{
        int lineno = 0;
        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextLine()) {
                records.add(creator.fromCsv(scanner.nextLine()));
            }
        }
        return records;
    }

}
