/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import core.Constant;
import core.Program;
import core.ProgramGenerator;
import core.Router;
import core.Rule;
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

/**
 *
 * @author martin
 */
public class NajmensiModel2 {

    static Router router = new Router();

    public static ArrayList<Program> readRulesFromFile(String fileName) {
        ArrayList<Program> programs;
        programs = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach((String line) -> {
                line = line.trim().replaceAll(" ", "");
                if (!line.isEmpty()) {
                    if (line.charAt(0) == '#') {
                        // create instance of new program
                        String programName = line.substring(1);
                        Program newprogram = new Program(programName);

                        programs.add(newprogram);
//                        System.out.println("new program, name: " + programName);
                    } else {
                        String[] splitted = line.split(":-", 2);
                        Rule r = new Rule();

                        if (!splitted[0].isEmpty()) {
                            r.setHead(new Constant(splitted[0]));
                        }
                        if (!splitted[1].isEmpty()) {
                            String[] bodySplitted = splitted[1].split(",");
                            for (String bodyLit : bodySplitted) {
                                r.addToBody(new Constant(bodyLit));
                            }
                        }

                        if (!programs.isEmpty()) {
                            programs.get(programs.size() - 1).addRule(r);
                        }

                    }
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(NajmensiModel2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return programs;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String bs = "a,b,c,d";        
        ProgramGenerator.generate(2, bs.split(",")).stream().forEach(line -> System.out.println(line));
        
/*
        ArrayList<Program> programs = readRulesFromFile("rules1.txt");
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
            System.out.println("najmensimodel2.NajmensiModel2.main() all threads finished.");
        }
*/
    }

}
