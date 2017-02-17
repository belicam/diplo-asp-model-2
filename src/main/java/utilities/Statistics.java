/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import core.Program;
import core.Router;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import messages.InitMessage;
import solver.TreeSolver;

/**
 *
 * @author martin
 */
public class Statistics {

    public static final int MAX_RULE_BODY_SIZE = 10;

    public static int MAX_RULES_COUNT = 50;
    public static int PROGRAMS_COUNT = 50;
    public static int ITERATIONS_COUNT = 10;

    public static String[] BASE_LITERALS = "a,b,c,d,e,f".split(",");

    private static class MeasuredValues {

        public List<Long> time;
        public List<Integer> rulesCount;

        public MeasuredValues(List<Long> time, List<Integer> rulesCount) {
            this.rulesCount = rulesCount;
            this.time = time;
        }
    }

    public static void singleThreadedVsNonDist() {
        GraphRenderer grenderer = new GraphRenderer("Max rules: " + MAX_RULES_COUNT);

        String singleThreadedLabel = "Single-threaded distributed";
        String nonDistLabel = "Non-distributed";

        grenderer.newSeries(singleThreadedLabel);
        grenderer.newSeries(nonDistLabel);

        MeasuredValues measured;
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            System.out.println("Iteration " + i + " started.");
            List<String> generated = ProgramGenerator.generate(PROGRAMS_COUNT, BASE_LITERALS, MAX_RULES_COUNT, MAX_RULE_BODY_SIZE);

            measured = runSingleThreaded(generated);
            grenderer.addValuesToSeries(singleThreadedLabel, measured.rulesCount, measured.time);

            measured = runNonDist(generated);
            grenderer.addValuesToSeries(nonDistLabel, measured.rulesCount, measured.time);
            System.out.println("Iteration " + i + " ended.");
        }
        grenderer.createGraph();
    }

    private static MeasuredValues runNonDist(List<String> generatedPrograms) {
        System.out.println("Non-distributed started.");
        generatedPrograms.removeIf((line) -> !line.equals("#0") && line.contains("#"));

        Long start = System.nanoTime();
        Program p = ProgramParser.parseStream(generatedPrograms.stream()).get(0);
        TreeSolver solver = new TreeSolver(p.getRules());
        solver.findSmallestModel(new HashSet<>());
        Long end = System.nanoTime();

        MeasuredValues measured = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
        measured.time.add((end - start) / 1000);
        measured.rulesCount.add(p.getRules().size());

        System.out.println("Non-distributed ended.");
        return measured;
    }

    private static MeasuredValues runMultiThreaded(List<String> generatedPrograms) {
        System.out.println("Distributed(multi-threaded) started.");
        Router router;

        router = new Router();
        List<Program> programs = ProgramParser.parseStream(generatedPrograms.stream());

        ExecutorService executor = Executors.newCachedThreadPool();
        for (Program p : programs) {
            p.setRouter(router);
            router.addProgram(p);
            executor.execute(p);
        }

        router.sendMessage(programs.get(0).getLabel(), new InitMessage());
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        List<Long> time = programs.stream()
                .filter(p -> p.isParticipationConfirmed())
                .sorted((p1, p2) -> Integer.compare(p1.getRules().size(), p2.getRules().size()))
                .map(p -> (p.getEndTime() - p.getStartTime()) / 1000) // microseconds
                .collect(Collectors.toList());

        List<Integer> rulesCount = programs.stream()
                .filter(p -> p.isParticipationConfirmed())
                .sorted((p1, p2) -> Integer.compare(p1.getRules().size(), p2.getRules().size()))
                .map(p -> p.getRules().size())
                .collect(Collectors.toList());

        System.out.println("Distributed(multi-threaded) ended.");
        return new MeasuredValues(time, rulesCount);
    }

    private static MeasuredValues runSingleThreaded(List<String> generatedPrograms) {
        System.out.println("Distributed(single-threaded) started.");
        Router router;

        router = new Router();
        List<Program> programs = ProgramParser.parseStream(generatedPrograms.stream());

        for (Program p : programs) {
            p.setRouter(router);
            router.addProgram(p);
        }

        long start = System.nanoTime();

        router.sendMessage(programs.get(0).getLabel(), new InitMessage());
        while (programs.get(0).isRunning()) {
            programs.forEach(p -> p.doStep());
        }

        long end = System.nanoTime();

        Integer rulesCount = programs.stream().map(p -> p.getRules().size()).reduce(0, Integer::sum);

        MeasuredValues measured = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
        measured.rulesCount.add(rulesCount);
        measured.time.add((end - start) / 1000);

        System.out.println("Distributed(single-threaded) ended.");
        return measured;
    }
}
