/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import core.Program;
import utilities.ProgramGenerator;
import utilities.ProgramParser;
import core.Router;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import messages.InitMessage;
import utilities.GraphRenderer;

/**
 *
 * @author martin
 */
public class NajmensiModel2 {

    static Router router;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int programsCount = 80;
        String[] baseLits = "a,b,c,d,e,f".split(",");
        int maxBodySize = 10;
        int maxRulesCount = 60;

        GraphRenderer grenderer = new GraphRenderer();

        for (int i = 0; i < 5; i++) {
            router = new Router();
            List<String> generated = ProgramGenerator.generate(programsCount, baseLits, maxRulesCount, maxBodySize);
            List<Program> programs = new ArrayList<>();

//        try (Stream<String> stream = Files.lines(Paths.get("rules1.txt"))) {
//            programs = ProgramParser.parseStream(stream);
//        } catch (IOException ex) {
//            Logger.getLogger(NajmensiModel2.class.getName()).log(Level.SEVERE, null, ex);
//        }
            programs = ProgramParser.parseStream(generated.stream());

            ExecutorService executor = Executors.newCachedThreadPool();
            programs.stream().forEach((Program p) -> {
                p.setRouter(router);
                router.addProgram(p);
                executor.execute(p);
            });

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

            grenderer.addValues(rulesCount, time);
        }
        grenderer.createGraph();
    }

}
