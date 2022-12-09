package org.victorschools.club.math;

import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ParseUtil {
    public static List<Student> openAndParse(Arguments args) {
        List<List<String>> data = null;
        try (InputStream iStream = Files.newInputStream(
                Paths.get(args.getInputURI()))) {
            data = CSVUtil.read(iStream);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        List<Student> students = new ArrayList<>();
        for (int r = 1; r < data.size(); r++) {
            List<String> line = data.get(r);
            int[] rankings = new int[args.getCategoryCount()];
            for (int c = 0; c < rankings.length; c++) {
                rankings[c] = parseInt(
                        line.get(args.getRankingStartColumn() + c));
            }
            String bias = line.get(args.getBiasColumn());
            int[] biases = new int[rankings.length];
            if (bias.length() > 0) {
                String[] biasSet = bias.split(",");
                for (String b : biasSet) {
                    char type = b.charAt(0);
                    int catIndex = parseInt(b.substring(1)) - 1;
                    if (type == '!') {
                        biases[catIndex] = 1000;
                    } else if (type == '+') {
                        biases[catIndex] = -1000;
                    } else {
                        throw new IllegalArgumentException(
                                "Unknown bias type " + type);
                    }
                }
            }
            students.add(new Student(line.get(args.getLastNameColumn()),
                    line.get(args.getFirstNameColumn()),
                    parseInt(line.get(args.getGradeColumn())),
                    parseInt(line.get(args.getCourseColumn())), rankings,
                    biases));
        }
        return students;
    }
}
