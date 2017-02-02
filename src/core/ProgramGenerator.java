/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author martin
 */
public class ProgramGenerator {

    static Random rand = new Random();

    public static List<String> generate(int programsCount, String[] base) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < programsCount; i++) {
            result.add("#" + i);
            int numRules = rand.nextInt(base.length * programsCount) + 1;

            for (int j = 0; j < numRules; j++) {
                String head = base[rand.nextInt(base.length)];
                int numBodyLits = rand.nextInt(base.length - 1);

                List<String> body = new ArrayList<>();
                for (int k = 0; k < numBodyLits; k++) {
                    String bodyLit = generateBodyLit(i, head, programsCount, base);
                    while (body.contains(bodyLit)) {
                        bodyLit = generateBodyLit(i, head, programsCount, base);
                    }
                    body.add(bodyLit);
                }
                result.add(i + ":" + head + " :- " + String.join(", ", body));
            }
        }

        return result;
    }

    private static String generateBodyLit(int headLabel, String headLit, int programsCount, String[] base) {
        int litRef = rand.nextInt(programsCount);
        String bodyLit = base[rand.nextInt(base.length)];
        if (litRef == headLabel) {
            while (bodyLit.equals(headLit)) {
                bodyLit = base[rand.nextInt(base.length)];
            }
        }
        return litRef + ":" + bodyLit;
    }

}
