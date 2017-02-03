/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author martin
 */
public class ProgramGenerator {

    static Random rand = new Random();

    public static List<String> generate(int programsCount, String[] base, int maxRulesCount, int maxBodyCount) {
        List<String> result = new ArrayList<>();

        for (int programLabel = 0; programLabel < programsCount; programLabel++) {
            result.add("#" + programLabel);
            int numRules = rand.nextInt(maxRulesCount) + 1;

            for (int j = 0; j < numRules; j++) {
                String head = programLabel + ":" + base[rand.nextInt(base.length)];
                int numBodyLits = rand.nextInt(maxBodyCount);

                List<String> body = new ArrayList<>();
                for (int k = 0; k < numBodyLits; k++) {
                    int litRef = rand.nextInt(programsCount);
                    String bodyLit = litRef + ":" + base[rand.nextInt(base.length)];

                    if (!body.contains(bodyLit) && !bodyLit.equals(head)) {
                        body.add(bodyLit);
                    }
                }

                Collections.sort(body);
                String rule = head + " :- " + String.join(", ", body);
                if (!result.contains(rule)) {
                    result.add(rule);
                }
            }
        }

        return result;
    }
}
