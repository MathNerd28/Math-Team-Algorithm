package org.victorschools.club.math;

import static java.lang.Integer.parseInt;
import static org.victorschools.club.math.Defaults.BIAS_COLUMN;
import static org.victorschools.club.math.Defaults.CATEGORY_COUNT;
import static org.victorschools.club.math.Defaults.CATS_PER_STUDENT;
import static org.victorschools.club.math.Defaults.CLASS_COLUMN;
import static org.victorschools.club.math.Defaults.FIRST_NAME_COLUMN;
import static org.victorschools.club.math.Defaults.GRADE_COLUMN;
import static org.victorschools.club.math.Defaults.INPUT_FILE;
import static org.victorschools.club.math.Defaults.LAST_NAME_COLUMN;
import static org.victorschools.club.math.Defaults.MAX_CAT_COUNT_DIFF;
import static org.victorschools.club.math.Defaults.MAX_STUDENTS_PER_CATEGORY;
import static org.victorschools.club.math.Defaults.RANKING_START_COLUMN;
import static org.victorschools.club.math.Defaults.SCORE_EXP_BASE;
import static org.victorschools.club.math.Defaults.STUDENTS_PER_TEAM;

import java.util.regex.Pattern;

public class Arguments implements Cloneable {
    private static final Pattern DELIM = Pattern.compile("=");

    private String inputURI = INPUT_FILE;

    private int firstNameColumn    = FIRST_NAME_COLUMN;
    private int lastNameColumn     = LAST_NAME_COLUMN;
    private int gradeColumn        = GRADE_COLUMN;
    private int classColumn        = CLASS_COLUMN;
    private int rankingStartColumn = RANKING_START_COLUMN;
    private int biasColumn         = BIAS_COLUMN;

    private int categoryCount          = CATEGORY_COUNT;
    private int studentsPerTeam        = STUDENTS_PER_TEAM;
    private int categoriesPerStudent   = CATS_PER_STUDENT;
    private int maxStudentsPerCategory = MAX_STUDENTS_PER_CATEGORY;
    private int minStudentsPerCategory = 2;
    private int maxCatCountDifference  = MAX_CAT_COUNT_DIFF;

    private int scoreExpBase = SCORE_EXP_BASE;

    public Arguments withInputURI(String uri) {
        this.inputURI = uri;
        return this;
    }

    public Arguments withFirstNameColumn(int column) {
        ensureNonnegative(column);
        this.firstNameColumn = column;
        return this;
    }

    public Arguments withLastNameColumn(int column) {
        ensureNonnegative(column);
        this.lastNameColumn = column;
        return this;
    }

    public Arguments withGradeColumn(int column) {
        ensureNonnegative(column);
        this.gradeColumn = column;
        return this;
    }

    public Arguments withClassColumn(int column) {
        ensureNonnegative(column);
        this.classColumn = column;
        return this;
    }

    public Arguments withRankingStartColumn(int column) {
        ensureNonnegative(column);
        this.rankingStartColumn = column;
        return this;
    }

    public Arguments withBiasColumn(int column) {
        ensureNonnegative(column);
        this.biasColumn = column;
        return this;
    }

    public Arguments withCategoryCount(int count) {
        ensurePositive(count);
        this.categoryCount = count;
        return this;
    }

    public Arguments withStudentsPerTeam(int count) {
        ensurePositive(count);
        this.studentsPerTeam = count;
        return this;
    }

    public Arguments withCategoriesPerStudent(int count) {
        ensurePositive(count);
        this.categoriesPerStudent = count;
        return this;
    }

    public Arguments withMaxStudentsPerCategory(int count) {
        ensurePositive(count);
        this.maxStudentsPerCategory = count;
        return this;
    }

    public Arguments withScoreExpBase(int exponent) {
        ensurePositive(exponent);
        this.scoreExpBase = exponent;
        return this;
    }

    public Arguments withMaxCatCountDiff(int difference) {
        ensurePositive(difference);
        this.maxCatCountDifference = difference;
        return this;
    }

    public String getInputURI() {
        return this.inputURI;
    }

    public int getFirstNameColumn() {
        return this.firstNameColumn;
    }

    public int getLastNameColumn() {
        return this.lastNameColumn;
    }

    public int getGradeColumn() {
        return this.gradeColumn;
    }

    public int getCourseColumn() {
        return this.classColumn;
    }

    public int getRankingStartColumn() {
        return this.rankingStartColumn;
    }

    public int getBiasColumn() {
        return this.biasColumn;
    }

    public int getCategoryCount() {
        return this.categoryCount;
    }

    public int getStudentsPerTeam() {
        return this.studentsPerTeam;
    }

    public int getCategoriesPerStudent() {
        return this.categoriesPerStudent;
    }

    public int getMaxStudentsPerCategory() {
        return this.maxStudentsPerCategory;
    }

    public int getMinStudentsPerCategory() {
        return this.minStudentsPerCategory;
    }

    public long getScoreExpBase() {
        return this.scoreExpBase;
    }

    public int getMaxCatCountDiff() {
        return this.maxCatCountDifference;
    }

    @Override
    public Arguments clone() {
        try {
            return (Arguments) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return this;
        }
    }

    public static Arguments parse(String... args) {
        Arguments arguments = new Arguments();
        for (String arg : args) {
            String[] split = DELIM.split(arg);
            switch (split[0].toLowerCase()) {
                case "file":
                    arguments.withInputURI(split[1]);
                    break;
                case "firstnamecolumn":
                    arguments.withFirstNameColumn(parseInt(split[1]));
                    break;
                case "lastnamecolumn":
                    arguments.withLastNameColumn(parseInt(split[1]));
                    break;
                case "gradecolumn":
                    arguments.withGradeColumn(parseInt(split[1]));
                    break;
                case "classcolumn":
                    arguments.withClassColumn(parseInt(split[1]));
                    break;
                case "rankingcolumn":
                    arguments.withRankingStartColumn(parseInt(split[1]));
                    break;
                case "biascolumn":
                    arguments.withBiasColumn(parseInt(split[1]));
                    break;
                case "catcount":
                    arguments.withCategoryCount(parseInt(split[1]));
                    break;
                case "studentsperteam":
                    arguments.withStudentsPerTeam(parseInt(split[1]));
                    break;
                case "catsperstudent":
                    arguments.withCategoriesPerStudent(parseInt(split[1]));
                    break;
                case "studentspercat":
                    arguments.withMaxStudentsPerCategory(parseInt(split[1]));
                    break;
                case "scoreexp":
                    arguments.withScoreExpBase(parseInt(split[1]));
                    break;
                case "maxcountdiff":
                    arguments.withMaxCatCountDiff(parseInt(split[1]));
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unrecognized argument: " + arg);
            }
        }
        return arguments;
    }

    private static void ensureNonnegative(int num) {
        if (num < 0) {
            throw new IllegalArgumentException("Argument cannot be negative");
        }
    }

    private static void ensurePositive(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("Argument must be positive");
        }
    }
}
