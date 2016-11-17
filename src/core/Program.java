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
import messages.ActivationMessage;
import messages.FireRequestMessage;
import messages.FireResponseMessage;
import messages.GetRequestMessage;
import messages.GetResponseMessage;
import messages.InitMessage;
import messages.NotifyParticipationMessage;

/**
 *
 * @author martin
 */
public class Program implements Runnable {

    private boolean isInitialProgram = false;
    private final Set<String> participatedPrograms = new HashSet<>();
    private String label;
    private List<Rule> rules = new ArrayList<>();

    private Router router;

    private String parent = null;
    private final BlockingQueue<Object> messages = new LinkedBlockingQueue<>();
    private final Map<String, Boolean> children = new HashMap<>();
    private final Map<Literal, List<String>> askedLiterals = new HashMap<>();
    private final Set<Literal> smallestModel = new HashSet<>();

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
            if (message instanceof GetRequestMessage) {
                processGetRequest(message);
            } else if (message instanceof GetResponseMessage) {
                processGetResponse(message);
            } else if (message instanceof FireRequestMessage) {
                processFireRequest(message);
            } else if (message instanceof FireResponseMessage) {
                processFireResponse(message);
            } else if (message instanceof ActivationMessage) {
                processActivation(message);
            } else if (message instanceof NotifyParticipationMessage) {
                processNotifyParticipation(message);
            } else if (message instanceof InitMessage) {
                processInit();
            }
        }
    }

    private void processGetRequest(Object message) {
        String from = ((GetRequestMessage) message).getSenderLabel();
        String initialSender = ((GetRequestMessage) message).getInitialSender();

        ((GetRequestMessage) message).getLits().forEach(lit -> {
            if (!this.askedLiterals.containsKey(lit)) {
                this.askedLiterals.put(lit, new ArrayList<>());
            }
            this.askedLiterals.get(lit).add(from);
        });
        
        if ((this.parent != null) || this.isInitialProgram) {
            getRouter().sendMessage(from, new GetResponseMessage(this.label));
            return;
        }

        this.parent = from;
        checkRules(initialSender);
        getRouter().sendMessage(initialSender, new NotifyParticipationMessage(this.label));
    }

    private void processGetResponse(Object message) {
        String from = ((GetResponseMessage) message).getSenderLabel();
        if (!children.get(from)) {
            children.put(from, Boolean.TRUE);

            if (!children.containsValue(Boolean.FALSE)) {
                if (!this.isInitialProgram) {
                    getRouter().sendMessage(this.parent, new GetResponseMessage(this.label));
                } else {
//                    fire to all participated
                    activate();
                    participatedPrograms.forEach(name -> getRouter().sendMessage(name, new ActivationMessage(this.label)));
                }
            }
        }
    }

    private void processFireRequest(Object message) {
//        todo
    }

    private void processFireResponse(Object message) {
//        todo
    }

    private void processActivation(Object message) {
//        todo odvodit co sa da, pozret pytane literaly -> poslat fire 
        activate();
    }

    private void processNotifyParticipation(Object message) {
        participatedPrograms.add(((NotifyParticipationMessage) message).getSenderLabel());
    }

    private void processInit() {
        this.isInitialProgram = true;
        checkRules(this.label);
    }

    private void activate() {
        System.out.println("Program#" + label + " is asked to share these literals: " + askedLiterals);
    }

    private void checkRules(String initialSender) {
        Map<String, List<Literal>> externals = new HashMap<>();

        rules.forEach(rule -> {
            rule.getBody().stream().forEach(lit -> {
                String litRef = lit.getValue().split(":")[0];
                if (!litRef.equals(this.label)) {
                    if (!externals.containsKey(litRef)) {
                        externals.put(litRef, new ArrayList<>());
                    }
                    externals.get(litRef).add(lit);
                }
            });
        });

        externals.keySet().forEach(key -> {
            getRouter().sendMessage(key, new GetRequestMessage(label, initialSender, externals.get(key)));
            children.put(key, Boolean.FALSE);
        });
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
}
