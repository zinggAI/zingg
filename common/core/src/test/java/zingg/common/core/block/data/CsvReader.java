package zingg.common.core.block.data;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CsvReader implements DataReader{

    @Override
    public List<String[]> readDataFromSource(String source) throws IOException, CsvException {
        CSVReader csvReader = getCSVReader(source);
        List<String[]> allData = csvReader.readAll();
        return allData;
    }


    private CSVReader getCSVReader(String source) throws IOException {
        FileReader filereader = new FileReader(source);
        com.opencsv.CSVReader csvReader = new CSVReaderBuilder(filereader)
                .withSkipLines(1)
                .build();
        filereader.close();
        return csvReader;
    }

}
