/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import core.Program;
import core.Router;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    private static final String folderMeasuredValues = "measured_values/";
    private static final String distMultiFilePrefix = "dist_multi_";
    private static final String distSingleFilePrefix = "dist_single_";
    private static final String nonDistFilePrefix = "non_dist_";

    private static class MeasuredValues implements Serializable {

        public List<Long> time;
        public List<Integer> rulesCount;

        public MeasuredValues(List<Long> time, List<Integer> rulesCount) {
            this.rulesCount = rulesCount;
            this.time = time;
        }
    }

    public static void measure(int[] rulesCount) {
        for (int cnt : rulesCount) {
            MeasuredValues measuredMulti = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
            MeasuredValues measuredSingle = new MeasuredValues(new ArrayList<>(), new ArrayList<>());
            MeasuredValues measuredNonDist = new MeasuredValues(new ArrayList<>(), new ArrayList<>());

            for (int i = 0; i < ITERATIONS_COUNT; i++) {
                System.out.println("Rules count: " + cnt + "| Iteration " + i + " started.");
                int programRulesCount = cnt / PROGRAMS_COUNT;

                List<String> generated = ProgramGenerator.generateLinked(PROGRAMS_COUNT, BASE_LITERALS, programRulesCount, MAX_RULE_BODY_SIZE);
                List<Program> distProgs = ProgramParser.parseStream(generated.stream());

                measuredMulti = runMultiThreaded(distProgs, measuredMulti);

                distProgs.forEach(p -> p.reset());
                measuredSingle = runSingleThreaded(distProgs, measuredSingle);

                for (int j = 0; j < distProgs.size(); j++) {
                    distProgs.get(j).reset();
                    if (j > 0) {
                        distProgs.get(0).getRules().addAll(distProgs.get(j).getRules());
                    }
                }
                measuredNonDist = runNonDist(Collections.singletonList(distProgs.get(0)), measuredNonDist);

                System.out.println("Rules count: " + cnt + "| Iteration " + i + " ended.");
            }
            saveMeasured(measuredMulti, measuredSingle, measuredNonDist, cnt);
        }
    }

    public static void makeGraph() {
        GraphRenderer grenderer = new GraphRenderer("Number of programs: " + PROGRAMS_COUNT);

        final String multiThreadedLabel = "Multi-threaded distributed";
        final String singleThreadedLabel = "Single-threaded distributed";
        final String nonDistLabel = "Non-distributed";

        grenderer.newSeries(multiThreadedLabel);
        grenderer.newSeries(singleThreadedLabel);
        grenderer.newSeries(nonDistLabel);

        MeasuredValues measuredMulti = loadMeasured(distMultiFilePrefix);
        grenderer.addValuesToSeries(multiThreadedLabel, measuredMulti.rulesCount, measuredMulti.time);

        MeasuredValues measuredSingle = loadMeasured(distSingleFilePrefix);
        grenderer.addValuesToSeries(singleThreadedLabel, measuredSingle.rulesCount, measuredSingle.time);

        MeasuredValues measuredNonDist = loadMeasured(nonDistFilePrefix);
        grenderer.addValuesToSeries(nonDistLabel, measuredNonDist.rulesCount, measuredNonDist.time);

        grenderer.finalizeSeries();

//        vytvorenie grafu po 2 seriach 
        String[] allSeries = new String[]{multiThreadedLabel, singleThreadedLabel, nonDistLabel};
        List<String> seriesToGraph = new ArrayList<>();
        for (int i = 0; i < allSeries.length - 1; i++) {
            for (int j = i + 1; j < allSeries.length; j++) {
                seriesToGraph.clear();
                seriesToGraph.add(allSeries[i]);
                seriesToGraph.add(allSeries[j]);
                grenderer.createGraph(seriesToGraph);
            }
        }
    }

    private static MeasuredValues runNonDist(List<Program> programs, MeasuredValues measured) {
        System.out.println("Non-distributed started.");

        long start = System.nanoTime();
        Program p = programs.get(0);
        TreeSolver solver = new TreeSolver(p.getRules());
        solver.findSmallestModel(new HashSet<>());
        long end = System.nanoTime();

        measured.time.add((end - start) / 1000);
        measured.rulesCount.add(p.getRules().size());

        System.out.println("Non-distributed ended.");
        return measured;
    }

    private static MeasuredValues runMultiThreaded(List<Program> programs, MeasuredValues measured) {
        System.out.println("Distributed(multi-threaded) started.");
        Router router;

        router = new Router();

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

    private static MeasuredValues runSingleThreaded(List<Program> programs, MeasuredValues measured) {
        System.out.println("Distributed(single-threaded) started.");
        Router router;

        router = new Router();

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

    private static void saveMeasured(MeasuredValues distMulti, MeasuredValues distSingle, MeasuredValues nonDist, int rulesCount) {
        try {
            Files.write(Paths.get(folderMeasuredValues + distMultiFilePrefix + rulesCount + ".ser"), Serializer.serialize(distMulti));
            Files.write(Paths.get(folderMeasuredValues + distSingleFilePrefix + rulesCount + ".ser"), Serializer.serialize(distSingle));
            Files.write(Paths.get(folderMeasuredValues + nonDistFilePrefix + rulesCount + ".ser"), Serializer.serialize(nonDist));
        } catch (IOException ex) {
            Logger.getLogger(Statistics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static MeasuredValues loadMeasured(String filePrefix) {
        MeasuredValues measured = new MeasuredValues(new ArrayList<>(), new ArrayList<>());

        try (Stream<Path> stream = Files.list(Paths.get(folderMeasuredValues))) {
            List<Path> filtered = stream
                    .filter(path -> path.getFileName().toString().startsWith(filePrefix))
                    .collect(Collectors.toList());

            filtered.forEach((path) -> {
                try {
                    MeasuredValues fromFile = (MeasuredValues) Serializer.deserialize(Files.readAllBytes(path));
                    measured.rulesCount.addAll(fromFile.rulesCount);
                    measured.time.addAll(fromFile.time);
                } catch (IOException ex) {
                    Logger.getLogger(Statistics.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(Statistics.class.getName()).log(Level.SEVERE, null, ex);
        }

        return measured;
    }
}
