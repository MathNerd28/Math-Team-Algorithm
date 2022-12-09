package org.victorschools.club.math;

public class ScoreTable {
    private final long   base;
    private final long[] scores;
    private final long   bestPossibleScore;

    public ScoreTable(Arguments args) {
        this.base = args.getScoreExpBase();
        this.scores = new long[args.getCategoryCount() + 1];
        this.scores[2] = 1;
        for (int i = 3; i < this.scores.length; i++) {
            this.scores[i] = this.scores[i - 1] * this.base;
        }
        long bestPossibleScore = 0;
        for (int i = args.getCategoriesPerStudent(); i >= 0; i--) {
            bestPossibleScore += this.scores[i];
        }
        this.bestPossibleScore = bestPossibleScore;
    }

    public long getScore(int ranking) {
        return this.scores[ranking];
    }

    public long getBase() {
        return this.base;
    }

    public long getBestPossibleScore() {
        return this.bestPossibleScore;
    }

    public int getCatCount() {
        return this.scores.length - 1;
    }
}
