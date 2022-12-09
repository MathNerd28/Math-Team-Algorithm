package org.victorschools.club.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Team {
    static final Comparator<Student> BIASES_FIRST = (
            Student s1, Student s2) -> {
        int compare = Integer.compare(s2.getBiasCount(), s1.getBiasCount());
        return compare == 0
                ? TeamGenerator.Strategy.Sort.NAME_COMP.compare(s1, s2)
                : compare;
    };

    private final AssignedStudent[] students;
    private final Arguments         args;
    private final ScoreTable        scores;
    private final String            id;
    private final int               catsPerStudent;
    private final int               catDiff;
    private volatile long           score      = -1;
    private volatile double         progress   = 0;
    private volatile boolean        isFinished = false;

    public Team(int id, Collection<Student> students, Arguments args) {
        this.id = calculateID(id);
        this.args = args;
        this.scores = new ScoreTable(args);
        this.students = students.stream()
                                .map(s -> new AssignedStudent(
                                        s).withScoringTable(scores))
                                .toArray(AssignedStudent[]::new);
        this.catsPerStudent = this.args.getCategoriesPerStudent();
        this.catDiff = this.scores.getCatCount() - this.catsPerStudent;
    }

    public AssignedStudent[] getStudents() {
        return this.students.clone();
    }

    public boolean isFinished() {
        return this.isFinished;
    }

    public double getProgress() {
        return this.progress;
    }

    public void assignOptimally() {
        this.isFinished = false;
        Arrays.sort(this.students, BIASES_FIRST);
        List<Assigner> assigners = new ArrayList<>();
        generateAssigners(assigners, new int[this.catsPerStudent], 0, 0);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<AssignedStudent[]>> futures = assigners.stream()
                                                           .map(executor::submit)
                                                           .toList();
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateProgress(assigners);
        }
        this.progress = 1D;
        AssignedStudent[] best = null;
        long bestScore = Long.MAX_VALUE;
        for (Future<AssignedStudent[]> future : futures) {
            try {
                AssignedStudent[] arr = future.get();
                long score = score(arr);
                if (score < bestScore) {
                    best = arr;
                    bestScore = score;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        this.score = bestScore;
        System.arraycopy(best, 0, this.students, 0, this.students.length);
        Arrays.sort(this.students, TeamGenerator.Strategy.Sort.NAME_COMP);
        this.isFinished = true;
    }

    private void updateProgress(List<Assigner> assigners) {
        double progress = 0;
        for (Assigner assigner : assigners) {
            progress += assigner.getProgress();
        }
        this.progress = progress / assigners.size();
    }

    private static long score(AssignedStudent[] students) {
        long sum = 0;
        for (int i = 0; i < students.length; i++) {
            long score = students[i].getCurrentScore();
            sum += score * score * score;
        }
        return sum;
    }

    private void generateAssigners(List<Assigner> assigners, int[] cats,
            int catIndex, int catStart) {
        if (catIndex == this.catsPerStudent) {
            assigners.add(new Assigner(cats));
            return;
        }
        int maxCat = catDiff + catIndex;
        for (int i = catStart; i <= maxCat; i++) {
            cats[catIndex] = i;
            generateAssigners(assigners, cats, catIndex + 1, i + 1);
        }
    }

    private double normalizedScore() {
        return Math.cbrt(this.score / (double) this.students.length);
    }

    @Override
    public String toString() {
        int maxNameLength = Arrays.stream(this.students)
                                  .map(Student::getFormattedName)
                                  .mapToInt(String::length)
                                  .max()
                                  .orElseGet(() -> 0);
        StringBuilder builder = new StringBuilder().append(
                String.format("Team %s (Score = %.2f)        %n", getId(),
                        normalizedScore()));
        for (int i = 0; i < this.students.length; i++) {
            builder.append(this.students[i].toString(maxNameLength));
            builder.append('\n');
        }
        return builder.toString();
    }

    class Assigner implements Callable<AssignedStudent[]> {
        private final AssignedStudent[] working;
        private final AssignedStudent[] best;
        private final int[]             workingCounts;
        private final int               catsPerStudent;
        private final int               maxStudentsPerCat;
        // catCount - catsPerStudent
        private final int     catDiff;
        private final long    maxBranch;
        private final long    choose;
        private long          bestScore;
        private volatile long currentBranch;

        Assigner(int[] initialCategories) {
            this.workingCounts = new int[Team.this.scores.getCatCount()];
            this.working = new AssignedStudent[Team.this.students.length];
            this.best = new AssignedStudent[Team.this.students.length];
            for (int i = 0; i < this.working.length; i++) {
                this.working[i] = Team.this.students[i].clone();
                this.working[i].clearAssignments();
            }
            for (int i = 0; i < initialCategories.length; i++) {
                this.working[0].assign(initialCategories[i]);
                this.workingCounts[initialCategories[i]]++;
            }

            Arguments args = Team.this.args;
            this.catDiff = Team.this.catDiff;
            this.catsPerStudent = Team.this.catsPerStudent;
            this.maxStudentsPerCat = args.getMaxStudentsPerCategory();
            this.bestScore = Long.MAX_VALUE;
            this.choose = choose(this.workingCounts.length,
                    this.catsPerStudent);
            this.maxBranch = calculateBranches(1, 0);
        }

        @Override
        public AssignedStudent[] call() throws Exception {
            recursive(1, 0, 0);
            return this.best;
        }

        public double getProgress() {
            return (double) this.currentBranch / this.maxBranch;
        }

        private long calculateBranches(int currentStudent, int catIndex) {
            return (pow(choose, (this.working.length - currentStudent - 1))
                    * (choose(this.workingCounts.length - catIndex,
                            this.catsPerStudent - catIndex)));
        }

        private static long pow(long base, long exp) {
            if (exp == 0) {
                return 1;
            } else if (exp == 1) {
                return base;
            } else if ((exp & 1) == 0) {
                return pow(base * base, exp / 2);
            } else {
                return base * (pow(base * base, exp / 2));
            }
        }

        private static long choose(int n, int k) {
            long nCk = 1;
            for (int i = 1; i <= k; i++) {
                nCk *= (n - i + 1);
                nCk /= i;
            }
            return nCk;
        }

        private void recursive(int studentIndex, int catIndex, int catStart) {
            if (catIndex >= this.catsPerStudent) {
                studentIndex++;
                catIndex = 0;
                catStart = 0;
            }
            if (studentIndex >= this.working.length) {
                checkScore();
                this.currentBranch++;
                return;
            } else if (catIndex == 0 && !isScoreBetter(studentIndex + 1)) {
                this.currentBranch += calculateBranches(studentIndex, catIndex);
                return;
            }
            int maxCat = catDiff + catIndex;
            for (int i = catStart; i <= maxCat; i++) {
                if (workingCounts[i] < maxStudentsPerCat) {
                    working[studentIndex].assign(i);
                    workingCounts[i]++;
                    recursive(studentIndex, catIndex + 1, i + 1);
                    working[studentIndex].unassign(i);
                    workingCounts[i]--;
                }
            }
        }

        private void checkScore() {
            long score = calculateScore();
            if (score < this.bestScore) {
                this.bestScore = score;
                for (int i = 0; i < this.working.length; i++) {
                    this.best[i] = this.working[i].clone();
                }
            }
        }

        private long calculateScore() {
            return calculateScore(this.working.length);
        }

        private long calculateScore(int numStudents) {
            long sum = 0;
            for (int i = 0; i < numStudents; i++) {
                long score = this.working[i].getCurrentScore();
                sum += score * score * score;
            }
            return sum;
        }

        private boolean isScoreBetter(int numStudents) {
            return this.bestScore > calculateScore(numStudents);
        }
    }

    public Arguments getArgs() {
        return this.args;
    }

    public ScoreTable getScores() {
        return this.scores;
    }

    public String getId() {
        return this.id;
    }

    public int getCatsPerStudent() {
        return this.catsPerStudent;
    }

    public int getCatDiff() {
        return this.catDiff;
    }

    public long getScore() {
        return this.score;
    }

    private static String calculateID(int id) {
        if (id == 0) {
            return "?";
        } else if (id == 1) {
            return "A";
        }
        id--;
        StringBuilder builder = new StringBuilder();
        while (id != 0) {
            builder.append((char) (id % 26 + 'A'));
            id /= 26;
        }
        return builder.toString();
    }
}
