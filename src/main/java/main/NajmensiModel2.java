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
        Statistics.programsCount = 50;
        Statistics.maxRulesCount = 10;
        
        Statistics.singleThreadedVsNonDist();
    }

}
