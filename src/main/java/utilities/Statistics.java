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

    public static int programsCount = 50;
    public static int maxRulesCount = 10;
    public static int iterationsCount = 10;

    public static String[] baseLits = "a,b,c,d,e,f".split(",");
    public static int maxBodySize = 10;

    private static class MeasuredValues {

        public List<Long> time;
        public List<Integer> rulesCount;

        public MeasuredValues(List<Long> time, List<Integer> rulesCount) {
            this.rulesCount = rulesCount;
            this.time = time;
        }
    }

    public static void singleThreadedVsNonDist() {
        GraphRenderer grenderer = new GraphRenderer(programsCount + " programs");

        String singleThreadedLabel = "Single-threaded distributed";
        String nonDistLabel = "Non-distributed";

        grenderer.newSeries(singleThreadedLabel);
        grenderer.newSeries(nonDistLabel);

        MeasuredValues measured;
        for (int i = 0; i < iterationsCount; i++) {
            List<String> generated = ProgramGenerator.generate(programsCount, baseLits, maxRulesCount, maxBodySize);

            measured = runSingleThreaded(generated);
            grenderer.addValuesToSeries(singleThreadedLabel, measured.rulesCount, measured.time);

            measured = runNonDist(generated);
            grenderer.addValuesToSeries(nonDistLabel, measured.rulesCount, measured.time);
        }
        grenderer.createGraph();
    }

    private static MeasuredValues runNonDist(List<String> generatedPrograms) {
        generatedPrograms.removeIf((line) -> !line.equals("#0") && line.contains("#"));

        Long start = System.nanoTime();
        Program p = ProgramParser.parseStream(generatedPrograms.stream()).get(0);
        TreeSolver solver = new TreeSolver(p.getRules());
        solver.findSmallestModel(new HashSet<>());
        Long end = System.nanoTime();

        MeasuredValues measured = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
        measured.time.add((end - start) / 1000);
        measured.rulesCount.add(p.getRules().size());

        return measured;
    }

    private static MeasuredValues runMultiThreaded(List<String> generatedPrograms) {
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

        return new MeasuredValues(time, rulesCount);
    }

    private static MeasuredValues runSingleThreaded(List<String> generatedPrograms) {
        Router router;

        router = new Router();
        List<Program> programs = ProgramParser.parseStream(generatedPrograms.stream());

        for (Program p : programs) {
            p.setRouter(router);
            router.addProgram(p);
        }

        router.sendMessage(programs.get(0).getLabel(), new InitMessage());

        while (programs.get(0).isRunning()) {
            programs.forEach(p -> p.doStep());
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

        return new MeasuredValues(time, rulesCount);
    }
}
