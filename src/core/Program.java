/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.FireMessage;
import messages.GetMessage;
import messages.InitMessage;

/**
 *
 * @author martin
 */
public class Program implements Runnable {

    private String label;
    private List<Rule> rules = new ArrayList<>();

    private Router router;

    private BlockingQueue<Object> messages = new LinkedBlockingQueue<>();
    private Map<Literal, Boolean> asked = new HashMap<>();
    private Set<Literal> smallestModel = new HashSet<>();

    public Program(String label) {
        this.label = label;
    }

    public Program(String label, Router router) {
        this.label = label;
        this.router = router;
    }

    @Override
    public void run() {
        System.out.println("Run: " + this.getLabel());
        while (true) {
            try {
                Object message = getMessages().take();
                processMessage(message);
            } catch (InterruptedException ex) {
                Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void receiveMessage(Object message) {
        messages.add(message);
    }

    private void processMessage(Object message) {
        if (message != null) {
            System.out.println("Program#" + this.label + ": " + message);
            if (message instanceof GetMessage) {
//                todo check literal, send value || ask for external

                checkRules();
                String from = ((GetMessage) message).getSenderLabel();
                Literal askedLit = ((GetMessage) message).getLit();
                if (smallestModel.contains(askedLit)) {
                    router.sendMessage(from, new FireMessage(this.label, askedLit, Boolean.TRUE));
                }
            } else if (message instanceof FireMessage) {
//                todo save value of external 
                Literal resolvedLit = ((FireMessage) message).getLit();
                Boolean isInModel = ((FireMessage) message).getIsInModel();
//                todo nedokoncene
            } else if (message instanceof InitMessage) {
//                todo odvodit co sa da, vyhladat externe a rozoslat spravy                
                checkRules();
            }
        }
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
    public BlockingQueue<Object> getMessages() {
        return messages;
    }

    private void checkRules() {
        rules.forEach((Rule r) -> {
            if (r.isFact()) {
                smallestModel.add(r.getHead());
            } else {
                r.getBody().stream().forEach((Literal lit) -> {
                    String litRef = lit.getValue().split(":")[0];
                    if (!litRef.equals(this.label)) {
                        if (!asked.containsKey(lit)) {
                            getRouter().sendMessage(litRef, new GetMessage(this.label, lit));
                            asked.put(lit, Boolean.FALSE);
                        }
                    }
                });
            }
        });
    }
}
