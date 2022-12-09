package org.victorschools.club.math;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVUtil {
    private CSVUtil() {}

    public static List<List<String>> read(InputStream stream) {
        try {
            return CSVParser.parse(stream, StandardCharsets.UTF_8,
                    CSVFormat.DEFAULT)
                            .stream()
                            .map(CSVRecord::toList)
                            .toList();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
