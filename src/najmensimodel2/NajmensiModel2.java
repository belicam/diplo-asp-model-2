/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package najmensimodel2;

import core.Constant;
import core.Program;
import core.Rule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author martin
 */
public class NajmensiModel2 {

    static Map<String, Program> programs = new HashMap<>();

    public static ArrayList<Rule> readRulesFromFile() {
        ArrayList<Rule> rules = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get("rules.txt"))) {
            stream.forEach((String line) -> {
                line = line.replaceAll(" ", "");
                if (!line.isEmpty()) {
                    if (line.charAt(0) == '#') {
                        // create instance of new program
                        String programName = line.charAt(1) + "";
                        Program newprogram = new Program(programName, new ArrayList<>(), programs);
                        programs.put(programName, newprogram);
//                        System.out.println("new program, name: " + programName);
                    } else {
                        String[] splitted = line.split(":-", 2);
                        Rule r = new Rule();
                        rules.add(r);

                        if (!splitted[0].isEmpty()) {
                            r.setHead(new Constant(splitted[0]));
                        }
                        if (!splitted[1].isEmpty()) {
                            String[] bodySplitted = splitted[1].split(",");
                            for (String bodyLit : bodySplitted) {
                                r.addToBody(new Constant(bodyLit));
                            }
                        }
                    }
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(NajmensiModel2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rules;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList<Rule> rules = readRulesFromFile();
        System.out.println(rules);
        System.out.println(programs);
//        System.out.println("NajmensiModel.TreeSolver");
//        TreeSolver ts = new TreeSolver(rules);
////        System.out.println(ts.getTrees());
//        System.out.println(ts.findSmallestModel());
    }

}
