package org.victorschools.club.math;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.random.RandomGenerator;

public class TeamGenerator {
    private static final RandomGenerator RANDOM = RandomGenerator.of(
            "Xoroshiro128PlusPlus");

    private TeamGenerator() {}

    @FunctionalInterface
    public interface Strategy
            extends BiFunction<List<Student>, Integer, List<Student>> {
        Strategy RANDOM = (List<Student> students, Integer count) -> {
            List<Student> copy = new ArrayList<>(students);
            for (int i = copy.size() - 1; i >= 0; i--) {
                int index = TeamGenerator.RANDOM.nextInt(copy.size());
                Student temp = copy.get(index);
                copy.set(index, copy.get(i));
                copy.set(i, temp);
            }
            return new ArrayList<>(copy.subList(0, count));
        };

        Strategy IN_ORDER = (List<Student> students,
                Integer count) -> new ArrayList<>(students.subList(0, count));

        class Evaluate implements Strategy {
            private final BiFunction<List<Student>, List<Student>, Long> scoringFunction;

            public Evaluate(
                    BiFunction<List<Student>, List<Student>, Long> scoringFunction) {
                this.scoringFunction = scoringFunction;
            }

            public Evaluate negate() {
                return new Evaluate(this.scoringFunction.andThen(l -> -l));
            }

            @Override
            public List<Student> apply(List<Student> students, Integer count) {
                List<Student> best = new ArrayList<>(
                        students.subList(0, count));
                List<Student> studentsCopy = new ArrayList<>(students);
                recursive(studentsCopy, best, Long.MAX_VALUE, new ArrayList<>(),
                        0);
                // Try every swap possible to see if it helps
                List<Student> remaining = new ArrayList<>(studentsCopy);
                remaining.removeAll(best);
                long bestScore = scoringFunction.apply(best, studentsCopy);
                outer: while (true) {
                    for (int i = 0; i < best.size(); i++) {
                        Student s1 = best.get(i);
                        for (int j = 0; j < remaining.size(); j++) {
                            Student s2 = remaining.get(j);
                            best.set(i, s2);
                            remaining.set(j, s1);
                            long score = scoringFunction.apply(best,
                                    studentsCopy);
                            if (score < bestScore) {
                                bestScore = score;
                                continue outer;
                            } else {
                                remaining.set(j, s2);
                            }
                        }
                        best.set(i, s1);
                    }
                    break;
                }
                return best;
            }

            private long recursive(List<Student> remaining, List<Student> best,
                    long bestScore, List<Student> team, int studentStart) {
                if (team.size() >= best.size()) {
                    return checkIfBetter(remaining, best, team, bestScore);
                } else {
                    if (!isDecent(remaining, team, bestScore, best.size())) {
                        return bestScore;
                    }
                    int max = remaining.size() - (best.size() - team.size());
                    for (int i = studentStart; i <= max; i++) {
                        team.add(remaining.remove(i));
                        bestScore = recursive(remaining, best, bestScore, team,
                                i);
                        remaining.add(i, team.remove(team.size() - 1));
                    }
                    return bestScore;
                }
            }

            private long checkIfBetter(List<Student> remaining,
                    List<Student> best, List<Student> team, long bestScore) {
                long score = this.scoringFunction.apply(team, remaining);
                if (score < bestScore) {
                    best.clear();
                    best.addAll(team);
                    return score;
                }
                return bestScore;
            }

            private boolean isDecent(List<Student> remaining,
                    List<Student> team, long bestScore, int studentCount) {
                if ((team.size() << 1) <= studentCount) {
                    return true;
                }
                long score = this.scoringFunction.apply(team, remaining);
                return score < bestScore;
            }

            @SuppressWarnings("java:S1488")
            public static Evaluate avoidRankingClashes(Arguments args) {
                int catsPerStudent = args.getCategoriesPerStudent();
                int catCount = args.getCategoryCount();
                Evaluate proto = new Evaluate(
                        (List<Student> team, List<Student> remaining) -> {
                            Team team2 = new Team(999, team, args);
                            team2.fastAssign();
                            return team2.getScore();
                        });

                Evaluate working = new Evaluate(
                        (List<Student> team, List<Student> remaining) -> {
                            if (team.isEmpty()) {
                                return 0L;
                            }
                            long score = 0;

                            int[] teamCounts = new int[catCount];
                            for (Student student : team) {
                                int[] rankings = student.getRankings();
                                for (int i = 0; i < rankings.length; i++) {
                                    if (rankings[i] <= catsPerStudent) {
                                        teamCounts[i]++;
                                    }
                                }
                            }
                            for (int i = 0; i < teamCounts.length; i++) {
                                score += teamCounts[i] * teamCounts[i];
                            }
                            int[] remainingCounts = new int[catCount];
                            for (Student student : remaining) {
                                int[] rankings = student.getRankings();
                                for (int i = 0; i < rankings.length; i++) {
                                    if (rankings[i] <= catsPerStudent) {
                                        remainingCounts[i]++;
                                    }
                                }
                            }
                            for (int i = 0; i < teamCounts.length; i++) {
                                score += remainingCounts[i]
                                        * remainingCounts[i];
                            }
                            return score;
                        });
                Evaluate attempt = new Evaluate(
                        (List<Student> team, List<Student> remaining) -> {
                            int[] teamCounts = new int[catCount];
                            for (Student student : team) {
                                int[] rankings = student.getRankings();
                                for (int i = 0; i < rankings.length; i++) {
                                    if (rankings[i] <= catsPerStudent) {
                                        teamCounts[i]++;
                                    }
                                }
                            }
                            int[] remainingCounts = new int[catCount];
                            for (Student student : remaining) {
                                int[] rankings = student.getRankings();
                                for (int i = 0; i < rankings.length; i++) {
                                    if (rankings[i] <= catsPerStudent) {
                                        remainingCounts[i]++;
                                    }
                                }
                            }
                            long score = 0;
                            for (int i = 0; i < teamCounts.length; i++) {
                                score += Math.abs(teamCounts[i] * team.size()
                                        - remainingCounts[i]
                                                * remaining.size());
                            }
                            return score;
                        });
                return working;
            }
        }

        class Sort implements Strategy {
            protected static final Comparator<Student> NAME_COMP = (Student s1,
                    Student s2) -> {
                int lastName = s1.getLastName()
                                 .compareTo(s2.getLastName());
                if (lastName == 0) {
                    return s1.getFirstName()
                             .compareTo(s2.getFirstName());
                } else {
                    return lastName;
                }
            };

            private static final Comparator<Student> GRADE_COMP = (Student s1,
                    Student s2) -> {
                int grade = Integer.compare(s2.getGrade(), s1.getGrade());
                return grade == 0 ? NAME_COMP.compare(s1, s2) : grade;
            };

            private static final Comparator<Student> COURSE_COMP = (Student s1,
                    Student s2) -> {
                int course = Integer.compare(s2.getCourse(), s1.getCourse());
                return course == 0 ? NAME_COMP.compare(s1, s2) : course;
            };

            private static final Comparator<Student> COURSE_AND_GRADE_COMP = (
                    Student s1, Student s2) -> {
                int diff = Integer.compare(s2.getGrade() + s2.getCourse(),
                        s1.getGrade() + s1.getCourse());
                return diff == 0 ? NAME_COMP.compare(s1, s2) : diff;
            };

            public static final Sort BY_NAME             = new Sort(NAME_COMP);
            public static final Sort BY_GRADE            = new Sort(GRADE_COMP);
            public static final Sort BY_COURSE           = new Sort(
                    COURSE_COMP);
            public static final Sort BY_COURSE_AND_GRADE = new Sort(
                    COURSE_AND_GRADE_COMP);

            private final Comparator<Student> comparator;

            public Sort(Comparator<Student> comparator) {
                this.comparator = comparator;
            }

            public Sort reversed() {
                return new Sort(this.comparator.reversed());
            }

            @Override
            public List<Student> apply(List<Student> students,
                    Integer teamSize) {
                List<Student> copy = new ArrayList<>(students);
                copy.sort(this.comparator);
                return new ArrayList<>(copy.subList(0, teamSize));
            }
        }
    }

    public static List<Team> assignTeams(Arguments args, List<Student> students,
            int fullTeams) {
        return assignTeams(args, students, fullTeams, Strategy.RANDOM);
    }

    public static List<Team> assignTeams(Arguments args, List<Student> students,
            int fullTeams, Strategy... strategies) {
        if (strategies.length == 0) {
            throw new IllegalArgumentException(
                    "Need at least 1 strategy to create teams");
        }
        int studentsPerTeam = args.getStudentsPerTeam();
        int[] teamCounts = new int[Math.ceilDiv(students.size(),
                args.getStudentsPerTeam())];
        int studentsRemaining = students.size();
        for (int i = 0; i < teamCounts.length; i++) {
            if (i < fullTeams) {
                if (studentsRemaining >= studentsPerTeam) {
                    teamCounts[i] = studentsPerTeam;
                    studentsRemaining -= studentsPerTeam;
                } else {
                    teamCounts[i] = studentsRemaining;
                    studentsRemaining = 0;
                }
            } else if (i == teamCounts.length - 1) {
                teamCounts[i] = studentsRemaining;
                studentsRemaining = 0;
            } else {
                teamCounts[i] = Math.ceilDiv(studentsRemaining,
                        teamCounts.length - i);
                studentsRemaining -= teamCounts[i];
            }
        }

        List<Student> studentsCopy = new ArrayList<>(students);
        List<Team> teams = new ArrayList<>();
        for (int i = 0, j = 0; !studentsCopy.isEmpty(); i++, j++) {
            if (teamCounts[i] >= studentsCopy.size()) {
                Team team = new Team(i + 1, studentsCopy, args);
                teams.add(team);
                studentsCopy.clear();
                continue;
            }
            if (j == strategies.length) {
                j--;
            }
            List<Student> selected = strategies[j].apply(studentsCopy,
                    teamCounts[i]);
            studentsCopy.removeAll(selected);
            Team team = new Team(i + 1, selected, args);
            teams.add(team);
        }
        return teams;
    }
}
