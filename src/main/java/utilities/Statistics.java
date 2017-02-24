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
import messages.InitMessage;
import solver.TreeSolver;

/**
 *
 * @author martin
 */
public class Statistics {

    public static final int MAX_RULE_BODY_SIZE = 10;

//    public static int MAX_RULES_COUNT = 50;
    public static int PROGRAMS_COUNT;
    public static int ITERATIONS_COUNT = 100;

    public static String[] BASE_LITERALS = "a,b,c,d,e,f".split(",");

    private static class MeasuredValues {

        public List<Long> time;
        public List<Integer> rulesCount;

        public MeasuredValues(List<Long> time, List<Integer> rulesCount) {
            this.rulesCount = rulesCount;
            this.time = time;
        }
    }

    public static void singleThreadedVsNonDist(int[] rulesCount) {
        GraphRenderer grenderer = new GraphRenderer("Number of programs: " + PROGRAMS_COUNT);

        String singleThreadedLabel = "Single-threaded distributed";
        String nonDistLabel = "Non-distributed";

        grenderer.newSeries(singleThreadedLabel);
        grenderer.newSeries(nonDistLabel);

        MeasuredValues measuredSingle = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
        MeasuredValues measuredNonDist = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
        for (int cnt : rulesCount) {
            for (int i = 0; i < ITERATIONS_COUNT; i++) {
                System.out.println("Rules count: " + cnt + "| Iteration " + i + " started.");
                int programRulesCount = cnt / PROGRAMS_COUNT;
                List<String> generated = ProgramGenerator.generate(PROGRAMS_COUNT, BASE_LITERALS, programRulesCount, MAX_RULE_BODY_SIZE);

                measuredSingle = runSingleThreaded(generated, measuredSingle);

                measuredNonDist = runNonDist(generated, measuredNonDist);
                System.out.println("Rules count: " + cnt + "| Iteration " + i + " ended.");
            }
        }

        grenderer.addValuesToSeries(singleThreadedLabel, measuredSingle.rulesCount, measuredSingle.time);
        grenderer.addValuesToSeries(nonDistLabel, measuredNonDist.rulesCount, measuredNonDist.time);
        grenderer.createGraph();
    }

    public static void multiThreadedVsNonDist(int[] rulesCount) {
        GraphRenderer grenderer = new GraphRenderer("Number of programs: " + PROGRAMS_COUNT);

        String multiThreadedLabel = "Multi-threaded distributed";
        String nonDistLabel = "Non-distributed";

        grenderer.newSeries(multiThreadedLabel);
        grenderer.newSeries(nonDistLabel);

        MeasuredValues measuredMulti = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
        MeasuredValues measuredNonDist = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
        for (int cnt : rulesCount) {
            for (int i = 0; i < ITERATIONS_COUNT; i++) {
                System.out.println("Rules count: " + cnt + "| Iteration " + i + " started.");
                int programRulesCount = cnt / PROGRAMS_COUNT;
                List<String> generated = ProgramGenerator.generate(PROGRAMS_COUNT, BASE_LITERALS, programRulesCount, MAX_RULE_BODY_SIZE);

                measuredMulti = runMultiThreaded(generated, measuredMulti);

                measuredNonDist = runNonDist(generated, measuredNonDist);
                System.out.println("Rules count: " + cnt + "| Iteration " + i + " ended.");
            }
        }

        grenderer.addValuesToSeries(multiThreadedLabel, measuredMulti.rulesCount, measuredMulti.time);
        grenderer.addValuesToSeries(nonDistLabel, measuredNonDist.rulesCount, measuredNonDist.time);
        grenderer.createGraph();
    }

    public static void multiThreadedVsSingleThreaded(int[] rulesCount) {
        GraphRenderer grenderer = new GraphRenderer("Number of programs: " + PROGRAMS_COUNT);

        String multiThreadedLabel = "Multi-threaded distributed";
        String singleThreadedLabel = "Single-threaded distributed";

        grenderer.newSeries(multiThreadedLabel);
        grenderer.newSeries(singleThreadedLabel);

        MeasuredValues measuredMulti = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
        MeasuredValues measuredSingle = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
        for (int cnt : rulesCount) {
            for (int i = 0; i < ITERATIONS_COUNT; i++) {
                System.out.println("Rules count: " + cnt + "| Iteration " + i + " started.");
                int programRulesCount = cnt / PROGRAMS_COUNT;
                List<String> generated = ProgramGenerator.generate(PROGRAMS_COUNT, BASE_LITERALS, programRulesCount, MAX_RULE_BODY_SIZE);

                measuredMulti = runMultiThreaded(generated, measuredMulti);
                measuredSingle = runSingleThreaded(generated, measuredSingle);
                System.out.println("Rules count: " + cnt + "| Iteration " + i + " ended.");
            }
        }

        grenderer.addValuesToSeries(multiThreadedLabel, measuredMulti.rulesCount, measuredMulti.time);
        grenderer.addValuesToSeries(singleThreadedLabel, measuredSingle.rulesCount, measuredSingle.time);
        grenderer.createGraph();
    }


    private static MeasuredValues runNonDist(List<String> generatedPrograms, MeasuredValues measured) {
        System.out.println("Non-distributed started.");
        generatedPrograms.removeIf((line) -> !line.equals("#0") && line.contains("#"));

        long start = System.nanoTime();
        Program p = ProgramParser.parseStream(generatedPrograms.stream()).get(0);
        TreeSolver solver = new TreeSolver(p.getRules());
        solver.findSmallestModel(new HashSet<>());
        long end = System.nanoTime();

        measured.time.add((end - start) / 1000);
        measured.rulesCount.add(p.getRules().size());

        System.out.println("Non-distributed ended.");
        return measured;
    }

    private static MeasuredValues runMultiThreaded(List<String> generatedPrograms, MeasuredValues measured) {
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

        long start = System.nanoTime();
        router.sendMessage(programs.get(0).getLabel(), new InitMessage());
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        long end = System.nanoTime();

        System.out.println("Distributed(multi-threaded) ended.");

        int rulesCount = programs.stream().map(p -> p.getRules().size()).reduce(0, Integer::sum);

        measured.rulesCount.add(rulesCount);
        measured.time.add((end - start) / 1000);

        return measured;
    }

    private static MeasuredValues runSingleThreaded(List<String> generatedPrograms, MeasuredValues measured) {
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

        int rulesCount = programs.stream().map(p -> p.getRules().size()).reduce(0, Integer::sum);

        measured.rulesCount.add(rulesCount);
        measured.time.add((end - start) / 1000);

        System.out.println("Distributed(single-threaded) ended.");
        return measured;
    }
}
