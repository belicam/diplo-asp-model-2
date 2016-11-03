/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author martin
 */
public class Program implements Runnable {

    private String label;
    private List<Rule> rules;
    private LinkedBlockingQueue<Literal> externalLiterals = new LinkedBlockingQueue<>();
    private Map<String, Program> allPrograms;

    public Program(String label, List<Rule> rules, Map<String, Program> agents) {
        this.label = label;
        this.rules = rules;
        this.allPrograms = agents;
    }

    public void fire() {
//         todo odvodit co sa da
//         todo pytat sa na externalLiterals
    }

    @Override
    public void run() {
        System.out.println(this.label);
    }
}
