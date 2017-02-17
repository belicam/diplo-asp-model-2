/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author martin
 */
public class ProgramGenerator {

    static Random rand = new Random();

    public static List<String> generate(int programsCount, String[] base, int maxRulesCount, int maxBodyCount) {
        System.out.println("Generating programs.");
        List<String> result = new ArrayList<>();

        for (int programLabel = 0; programLabel < programsCount; programLabel++) {
            result.add("#" + programLabel);
            int numRules = maxRulesCount - rand.nextInt(Math.round(maxRulesCount * 0.2f));

            Map<String, Set<Set<String>>> generated = new HashMap<>();
            for (int j = 0; j < numRules; j++) {
                String head = programLabel + ":" + base[rand.nextInt(base.length)];
                if (!generated.containsKey(head)) {
                    generated.put(head, new HashSet<>());
                }

                int numBodyLits = rand.nextInt(maxBodyCount);

                Set<String> body = new HashSet<>();
                for (int k = 0; k < numBodyLits; k++) {
                    int litRef = rand.nextInt(programsCount);
                    String bodyLit = litRef + ":" + base[rand.nextInt(base.length)];

                    if (!bodyLit.equals(head)) {
                        body.add(bodyLit);
                    }
                }

                generated.get(head).add(body);
            }

            generated.forEach((h, bodies) -> {
                bodies.forEach(b -> {
                    String rule = h + " :- " + String.join(", ", b);
                    result.add(rule);
                });
            });
        }

        System.out.println("Programs generated.");
        return result;
    }
}
