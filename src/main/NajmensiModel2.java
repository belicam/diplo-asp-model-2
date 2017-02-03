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
import javax.swing.JFrame;
import messages.InitMessage;

/**
 *
 * @author martin
 */
public class NajmensiModel2 {

    static Router router = new Router();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(640, 480);
//        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
        
        int programsCount = 100;
        String[] baseLits = "a,b,c,d".split(",");
        int maxBodySize = 6;
        int maxRulesCount = baseLits.length;

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
    }

}
