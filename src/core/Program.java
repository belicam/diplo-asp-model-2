/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author martin
 */
public class Program implements Runnable {

    private String label;
    private List<Rule> rules = new ArrayList<>();
    private LinkedBlockingQueue<Literal> messages = new LinkedBlockingQueue<>();
    private Router router;

    public Program(String label) {
        this.label = label;
    }

    public Program(String label, Router router) {
        this.label = label;
        this.router = router;
    }
    
    public boolean get(Literal lit) {
//        todo
        return false;
    }

    public void fire(Literal lit, boolean isInModel) {
//         todo odvodit co sa da
//         todo pytat sa na messages
    }

    @Override
    public void run() {
        System.out.println("Run: " + this.getLabel());
        this.rules.forEach((r) -> checkRule(r));
        System.out.println(this.messages);
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    public void addRule(Rule r) {
        this.rules.add(r);
    }

    /**
     * @return the rules
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * @param rules the rules to set
     */
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * @return the router
     */
    public Router getRouter() {
        return router;
    }

    /**
     * @param router the router to set
     */
    public void setRouter(Router router) {
        this.router = router;
    }

    /**
     * @return the externalsNeeded
     */
    public LinkedBlockingQueue<Literal> getMessages() {
        return messages;
    }

    private void checkRule(Rule r) {
        r.getBody().stream().forEach((Literal lit) -> {
            String litRef = lit.getValue().split(":")[0];
            if (!litRef.equals(this.label)) {
                getMessages().add(lit);
            }
        });
    }
}
