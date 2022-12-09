package org.victorschools.club.math;

import java.util.Random;

public class Student implements Cloneable {
    private static final Random RANDOM = new Random();

    protected final String lastName;
    protected final String firstName;
    protected final String formattedName;
    protected final int    course;
    protected final int    grade;
    protected final int[]  rankings;
    protected final int[]  biases;
    protected long         randomID;

    public Student(String lastName, String firstName, int grade, int course,
            int[] rankings, int[] biases) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.formattedName = lastName + ", " + firstName;
        this.grade = grade;
        this.course = course;
        this.rankings = rankings.clone();
        this.biases = biases.clone();
        this.randomID = RANDOM.nextLong();
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public long getRandomID() {
        return this.randomID;
    }

    public int getGrade() {
        return this.grade;
    }

    public int getCourse() {
        return this.course;
    }

    @Override
    public String toString() {
        return this.firstName + " " + this.lastName;
    }

    public String getFormattedName() {
        return this.formattedName;
    }

    public int[] getRankings() {
        return this.rankings;
    }

    public int getRanking(int category) {
        return this.rankings[category];
    }

    public int[] getBiases() {
        return this.biases;
    }

    public int getBiasCount() {
        int count = 0;
        for (int i = 0; i < this.biases.length; i++) {
            if (biases[i] != 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    protected Student clone() {
        try {
            return (Student) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
