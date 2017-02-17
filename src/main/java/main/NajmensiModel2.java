/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

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
        Statistics.PROGRAMS_COUNT = 50;

        int[] rulesCnt = new int[]{10, 50, 100, 500, 1000, 5000, 10000, 20000, 40000};
        Statistics.singleThreadedVsNonDist(rulesCnt);
    }

}
