package org.victorschools.club.math;

import java.util.List;

public class Main {
    public static void main(String... args) {
        long start = System.nanoTime();
        clearConsole();
        System.out.println("-------- START --------\n");
        System.out.print("Parse args: ");
        Arguments arguments = Arguments.parse(args);
        long section = printTime(start);

        System.out.print("Parse CSV: ");
        List<Student> students = ParseUtil.openAndParse(arguments);
        section = printTime(section);

        System.out.print("Assign teams: ");
        List<Team> teams = TeamGenerator.assignTeams(arguments, students, 1,
                TeamGenerator.Strategy.Sort.BY_COURSE_AND_GRADE,
                TeamGenerator.Strategy.Evaluate.avoidRankingClashes(arguments));
        section = printTime(section);

        System.out.println("\nAssigning categories...\n");

        for (Team team : teams) {
            Thread.ofPlatform()
                  .start(team::assignOptimally);
            while (!team.isFinished()) {
                printProgress(team);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.print('\r');
            System.out.println(team);
        }
        System.out.print("Assign categories: ");
        section = printTime(section);

        System.out.print("Total: ");
        printTime(start);
        System.out.println("\n--------- END ---------");
    }

    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
    }

    private static long printTime(long startNanos) {
        long endNanos = System.nanoTime();
        System.out.printf("%.1f ms%n", (endNanos - startNanos) / 1000000D);
        return endNanos;
    }

    private static void printProgress(Team team) {
        double progress = team.getProgress() * 100;
        int progressInt = (int) (progress / 5);
        System.out.printf("\r[%s%s] %5.1f%%", "*".repeat(progressInt),
                " ".repeat(20 - progressInt), progress);
    }
}
