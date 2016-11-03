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

/**
 *
 * @author martin
 */
public class Program implements Runnable {

    private String label;
    private List<Rule> rules = new ArrayList<>();
    private Set<String> externalsNeeded = new HashSet<>();
    private Router router;

    public Program(String label) {
        this.label = label;
    }

    public Program(String label, Router router) {
        this.label = label;
        this.router = router;
    }

    public void fire() {
//         todo odvodit co sa da
//         todo pytat sa na externalLiterals
    }

    @Override
    public void run() {
        System.out.println(this.getLabel());
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

        checkRule(r);
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

        this.rules.forEach((r) -> checkRule(r));
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
    public Set<String> getExternalsNeeded() {
        return externalsNeeded;
    }

    /**
     * @param externalNeeded the externalsNeeded to set
     */
    public void setExternalsNeeded(Set<String> externalNeeded) {
        this.externalsNeeded = externalNeeded;
    }

    private void checkRule(Rule r) {
        r.getBody().stream().forEach((Literal lit) -> {
            String litRef = lit.getValue().split(":")[0];
            if (!litRef.equals(this.label)) {
                getExternalsNeeded().add(litRef);
            }
        });
    }
}
