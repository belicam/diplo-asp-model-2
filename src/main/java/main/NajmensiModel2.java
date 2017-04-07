/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import utilities.ProgramGenerator;
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
        Statistics.ITERATIONS_COUNT = 100;

        int[] rulesCnt = new int[]{100000, 200000, 300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000};
        Statistics.measure(rulesCnt, ProgramGenerator::generateRandom);
        Statistics.makeGraph();

        Statistics.measure(rulesCnt, ProgramGenerator::generateSequential);
        Statistics.makeGraph();

        Statistics.measure(rulesCnt, ProgramGenerator::generateChained);
        Statistics.makeGraph();
    }

}
