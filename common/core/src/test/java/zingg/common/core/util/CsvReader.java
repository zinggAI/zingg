package zingg.common.core.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import zingg.common.core.block.data.ICsvReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvReader implements ICsvReader {
    protected List<? extends IFromCsv> records;
    IFromCsv creator;

    public CsvReader(IFromCsv creator){
        records = new ArrayList<IFromCsv>();
        this.creator = creator;
    }

    //default constructor
    public CsvReader() {

    }

    public List<String[]> readDataFromSource(String source) throws IOException, CsvException {
        CSVReader csvReader = getCSVReader(source);
        List<String[]> allData = csvReader.readAll();
        return allData;
    }

    public List<? extends IFromCsv> getRecords(String file, boolean skipHeader) throws FileNotFoundException {
        int lineno = 0;
        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextLine()) {
                records.add(creator.fromCsv(scanner.nextLine()));
            }
        }
        return records;
    }

    private CSVReader getCSVReader(String source) throws IOException {
        FileReader filereader = new FileReader(source);
        CSVReader csvReader = new CSVReaderBuilder(filereader)
                .withSkipLines(1)
                .build();
        return csvReader;
    }

}
