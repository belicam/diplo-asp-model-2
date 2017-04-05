/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.util.ArrayList;
import java.util.Collection;
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

    public static List<String> generate(int programsCount, String[] base, int rulesCount, int maxBodyCount) {
        System.out.println("Generating programs.");
        List<String> result = new ArrayList<>();

        for (int programLabel = 0; programLabel < programsCount; programLabel++) {
            result.add("#" + programLabel);

            Map<String, Set<Set<String>>> generated = new HashMap<>();
            int generatedCount = 0;
            while (generatedCount < rulesCount) {
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

                if (generated.get(head).add(body)) {
                    generatedCount++;
                }
            }

            generated.forEach((h, bodies) -> {
                bodies.forEach(b -> {
                    String rule = h + ":-" + String.join(",", b);
                    result.add(rule);
                });
            });
        }

//        result.forEach(line -> System.out.println(line));
//        System.out.println("------------------------------------");
        System.out.println("Programs generated. Number of rules: " + (result.size() - programsCount));
        return result;
    }

    public static List<String> generateLinked(int programsCount, String[] base, int rulesCount, int maxBodyCount) {
        List<String> result = new ArrayList<>();

        List<List<String>> allHeads = new ArrayList<>();
        List<List<List<String>>> allBodies = new ArrayList<>();

        allHeads.add(generateHeads(rulesCount, generateProgramName(0), base));
        allBodies.add(null);

        for (int i = 1; i < programsCount; i++) {
            String[] arrayHeads = allHeads.get(i - 1).toArray(new String[allHeads.get(i - 1).size()]);
            allBodies.add(generateBodies(rulesCount, arrayHeads, maxBodyCount));
            allHeads.add(generateHeads(rulesCount, generateProgramName(i), base));
        }

        String[] arrayHeads = allHeads.get(allHeads.size() - 1).toArray(new String[allHeads.get(allHeads.size() - 1).size()]);
        allBodies.set(0, generateBodies(rulesCount, arrayHeads, maxBodyCount));

        for (int i = 0; i < programsCount; i++) {
            result.addAll(joinProgramData(generateProgramName(i), allHeads.get(i), allBodies.get(i)));
        }
        return result;
    }

    private static List<String> generateHeads(int count, String programName, String[] base) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(programName + ":" + base[rand.nextInt(base.length)]);
        }
        return result;
    }

    private static List<List<String>> generateBodies(int count, String[] availableLiterals, int maxBodyCount) {
        List<List<String>> bodies = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int numBodyLits = rand.nextInt(maxBodyCount);
            List<String> body = new ArrayList<>();
            for (int k = 0; k < numBodyLits; k++) {
                body.add(availableLiterals[rand.nextInt(availableLiterals.length)]);
            }
            bodies.add(body);
        }
        return bodies;
    }

    private static String generateProgramName(int id) {
        return "agent" + id;
    }

    private static List<String> joinProgramData(String programName, List<String> heads, List<List<String>> bodies) {
        List<String> result = new ArrayList<>();
        result.add("#" + programName);

        for (int i = 0; i < heads.size(); i++) {
            String rule = heads.get(i) + ":-" + String.join(",", bodies.get(i));
            result.add(rule);
        }
        return result;
    }
}
