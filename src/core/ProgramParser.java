/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author martin
 */
public class ProgramParser {

    public static List<Program> parseStream(Stream<String> lineStream) {
        List<Program> programs = new ArrayList<>();

        lineStream.forEach((String line) -> {
            line = line.trim().replaceAll(" ", "");
            if (!line.isEmpty()) {
                if (line.charAt(0) == '#') {
                    // create instance of new program
                    String programName = line.substring(1);
                    Program newprogram = new Program(programName);

                    programs.add(newprogram);
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
        return programs;
    }
}
