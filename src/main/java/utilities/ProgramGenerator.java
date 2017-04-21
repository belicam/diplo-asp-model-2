/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author martin
 */
public class ProgramGenerator {

    static Random rand = new Random();

    @FunctionalInterface
    public interface Function4<A, B, C, D, R> {

        public R apply(A a, B b, C c, D d);
    }

    public static List<String> generateSequential(Integer programsCount, String[] base, Integer rulesCount, Integer maxBodyCount) {
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

    public static List<String> generateChained(Integer programsCount, String[] base, Integer rulesCount, Integer maxBodyCount) {
        List<String> result = new ArrayList<>();

        List<List<String>> allHeads = new ArrayList<>();

        for (int i = 0; i < programsCount; i++) {
            allHeads.add(generateHeads(rulesCount, generateProgramName(i), base));
        }

        List<String> collected = allHeads
                .stream()
                .reduce(new ArrayList<>(), (res, a) -> {
                    res.addAll(a);
                    return res;
                });

        String[] availableLiterals = collected.toArray(new String[collected.size()]);

        for (int i = 0; i < programsCount; i++) {
            List<List<String>> bodies = generateBodies(rulesCount, availableLiterals, maxBodyCount);
            result.addAll(joinProgramData(generateProgramName(i), allHeads.get(i), bodies));
        }

        return result;
    }

    public static List<String> generateSeparated(Integer programsCount, String[] base, Integer rulesCount, Integer maxBodyCount) {
        List<String> result = new ArrayList<>();

        List<List<String>> allHeads = new ArrayList<>();

        int firstGroup = rand.nextInt((programsCount - 1) / 20) + 1;
        for (int i = 0; i < firstGroup; i++) {
            allHeads.add(generateHeads(rulesCount, generateProgramName(i), base));
        }

        List<String> collectedHeads = allHeads
                .stream()
                .reduce(new ArrayList<>(), (res, a) -> {
                    res.addAll(a);
                    return res;
                });

        String[] availableLiterals = collectedHeads.toArray(new String[collectedHeads.size()]);

        for (int i = 0; i < firstGroup; i++) {
            List<List<String>> bodies = generateBodies(rulesCount, availableLiterals, maxBodyCount);
            result.addAll(joinProgramData(generateProgramName(i), allHeads.get(i), bodies));
        }

//        SECOND GROUP
        for (int i = firstGroup; i < programsCount; i++) {
            allHeads.add(generateHeads(rulesCount, generateProgramName(i), base));
        }

        collectedHeads = allHeads
                .stream()
                .reduce(new ArrayList<>(), (res, a) -> {
                    res.addAll(a);
                    return res;
                });

        availableLiterals = collectedHeads.toArray(new String[collectedHeads.size()]);

        for (int i = firstGroup; i < programsCount; i++) {
            List<List<String>> bodies = generateBodies(rulesCount, availableLiterals, maxBodyCount);
            result.addAll(joinProgramData(generateProgramName(i), allHeads.get(i), bodies));
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
