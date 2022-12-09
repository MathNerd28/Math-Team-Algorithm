package org.victorschools.club.math;

import java.util.Arrays;

public class AssignedStudent extends Student {
    private boolean[]  isAssigned;
    private int        assignmentCount;
    private long       currentScore;
    private long       currentBias;
    private ScoreTable scoreTable;

    public AssignedStudent(String lastName, String firstName, int grade,
            int course, int[] rankings, int[] biases) {
        super(lastName, firstName, grade, course, rankings, biases);
        this.isAssigned = new boolean[this.rankings.length];
        this.assignmentCount = 0;
        this.currentScore = 0;
    }

    public AssignedStudent(Student other) {
        this(other.lastName, other.firstName, other.grade, other.course,
                other.rankings, other.biases);
    }

    public AssignedStudent withScoringTable(ScoreTable table) {
        this.scoreTable = table;
        return this;
    }

    public long getCurrentScore() {
        return this.currentScore + currentBias;
    }

    public long getCurrentScoreNoBias() {
        return this.currentScore;
    }

    public void assignAll() {
        for (int i = 0; i < this.isAssigned.length; i++) {
            assign(i);
        }
    }

    private void assignSafe(int category) {
        isAssigned[category] = true;
        this.assignmentCount++;
        this.currentScore += this.scoreTable.getScore(this.rankings[category]);
        this.currentBias += this.biases[category];
    }

    public boolean assign(int category) {
        boolean change = !isAssigned[category];
        if (change) {
            assignSafe(category);
        }
        return change;
    }

    private void unassignSafe(int category) {
        isAssigned[category] = false;
        this.assignmentCount--;
        this.currentScore -= this.scoreTable.getScore(this.rankings[category]);
        this.currentBias -= this.biases[category];
    }

    public boolean unassign(int category) {
        boolean change = isAssigned[category];
        if (change) {
            unassignSafe(category);
        }
        return change;
    }

    public boolean swap(AssignedStudent other, int cat1, int cat2) {
        boolean change = this.isAssigned[cat1] != other.isAssigned[cat1]
                && this.isAssigned[cat2] != other.isAssigned[cat2]
                && this.isAssigned[cat1] != this.isAssigned[cat2];
        if (change) {
            if (this.isAssigned[cat1]) {
                unassignSafe(cat1);
                assignSafe(cat2);
                other.unassignSafe(cat2);
                other.assignSafe(cat1);
            } else {
                unassignSafe(cat2);
                assignSafe(cat1);
                other.unassignSafe(cat1);
                other.assignSafe(cat2);
            }
        }
        return change;
    }

    public void clearAssignments() {
        Arrays.fill(this.isAssigned, false);
        this.assignmentCount = 0;
        this.currentScore = 0;
    }

    public int getAssignmentCount() {
        return this.assignmentCount;
    }

    public boolean isAssigned(int cat) {
        return this.isAssigned[cat];
    }

    @Override
    protected AssignedStudent clone() {
        AssignedStudent copy = (AssignedStudent) super.clone();
        copy.isAssigned = Arrays.copyOf(this.isAssigned,
                this.isAssigned.length);
        return copy;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int nameLength) {
        String formattedName = this.formattedName;
        if (nameLength > formattedName.length()) {
            formattedName += " ".repeat(nameLength - formattedName.length());
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.isAssigned.length; i++) {
            builder.append(this.isAssigned[i] ? Integer.toString(i + 1) : " ".repeat(Integer.toString(i).length()));
            builder.append(',');
        }
        builder.setLength(builder.length() - 1);
        return String.format(
                "Student: { Name = %s ; Score =%3d ; Categories = [%s] ; }",
                formattedName,
                getCurrentScoreNoBias() - this.scoreTable.getBestPossibleScore(),
                builder);
    }
}
