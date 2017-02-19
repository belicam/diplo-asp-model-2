/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import core.Program;
import core.Router;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import messages.InitMessage;
import utilities.ProgramParser;
import utilities.Statistics;

/**
 *
 * @author martin
 */
public class NajmensiModel2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Statistics.PROGRAMS_COUNT = 10;

        int[] rulesCnt = new int[]{10, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000};
        Statistics.singleThreadedVsNonDist(rulesCnt);
        Statistics.multiThreadedVsNonDist(rulesCnt);
        Statistics.multiThreadedVsSingleThreaded(rulesCnt);
        

//        List<Program> programs = new ArrayList<>();
//        Router router = new Router();
//
//        try (Stream<String> stream = Files.lines(Paths.get("bugged.txt"))) {
//            programs = ProgramParser.parseStream(stream);
//        } catch (IOException ex) {
//            Logger.getLogger(NajmensiModel2.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        for (Program p : programs) {
//            p.setRouter(router);
//            router.addProgram(p);
//        }
//
//        ExecutorService executor = Executors.newCachedThreadPool();
//        for (Program p : programs) {
//            p.setRouter(router);
//            router.addProgram(p);
//            executor.execute(p);
//        }
//
//        router.sendMessage(programs.get(0).getLabel(), new InitMessage());
//        executor.shutdown();
//
//        try {
//            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//        }
//
    }

}
