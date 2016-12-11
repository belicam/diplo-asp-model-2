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
import messages.DependencyGraphBuiltMessage;
import messages.FireRequestMessage;
import messages.FireResponseMessage;
import messages.GetRequestMessage;
import messages.GetResponseMessage;
import messages.InitMessage;
import messages.NotifyParticipationRequestMessage;
import messages.NotifyParticipationResponseMessage;
import messages.StopMessage;
import solver.TreeSolver;

/**
 *
 * @author martin
 */
public class Program implements Runnable {

    private boolean isInitialProgram = false;
    private boolean notifyParticipationConfirmed = false;
    private boolean running;
    private final Set<String> participatedPrograms = new HashSet<>();
    private String label;
    private List<Rule> rules = new ArrayList<>();

    private Router router;
    private TreeSolver solver;

    private String parent = null;
    private Map<String, Boolean> children = null;
    private final BlockingQueue<Object> messages = new LinkedBlockingQueue<>();
    private final Map<Literal, Set<String>> askedLiterals = new HashMap<>();

    private Set<Literal> smallestModel = new HashSet<>();

    public Program(String label) {
        this.label = label;
        this.running = true;
    }

    public Program(String label, Router router) {
        this.label = label;
        this.router = router;
        this.running = true;
    }

    @Override
    public void run() {
//        System.out.println("Run: " + this.getLabel());
        while (isRunning()) {
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
            } else if (message instanceof NotifyParticipationRequestMessage) {
                processNotifyParticipationRequest(message);
            } else if (message instanceof NotifyParticipationResponseMessage) {
                processNotifyParticipationResponse(message);
            } else if (message instanceof DependencyGraphBuiltMessage) {
                processDependencyGraphBuilt();
            } else if (message instanceof StopMessage) {
                this.setRunning(false);
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
                this.getAskedLiterals().put(lit, new HashSet<>());
            }
            this.getAskedLiterals().get(lit).add(from);
        });

        if (this.children != null) {
            getRouter().sendMessage(from, new GetResponseMessage(this.label));
        } else {
            this.parent = from;
            checkRules(initialSender);
            getRouter().sendMessage(initialSender, new NotifyParticipationRequestMessage(this.label));
        }
    }

    private void processGetResponse(Object message) {
        String from = ((GetResponseMessage) message).getSenderLabel();
        if (!children.get(from)) {
            children.put(from, Boolean.TRUE);

            checkChildrenResponses();
        }
    }

    private void processFireRequest(Object message) {
//        todo
    }

    private void processFireResponse(Object message) {
//        todo
    }

    private void processActivation(Object message) {
        solver = new TreeSolver((ArrayList<Rule>) rules);
        fire();
    }

    private void processNotifyParticipationRequest(Object message) {
        String senderLabel = ((NotifyParticipationRequestMessage) message).getSenderLabel();

        getParticipatedPrograms().add(senderLabel);
        getRouter().sendMessage(senderLabel, new NotifyParticipationResponseMessage(this.label));
    }

    private void processNotifyParticipationResponse(Object message) {
        this.notifyParticipationConfirmed = true;
        checkChildrenResponses();
    }

    private void processDependencyGraphBuilt() {
        getRouter().broadcastMessage(new ActivationMessage(this.label));
        // getRouter().broadcastMessage(new StopMessage()); // todo remove after all finished
    }

    private void processInit() {
        this.isInitialProgram = true;
        this.notifyParticipationConfirmed = true;
        checkRules(this.label);

        if (children.isEmpty()) {
            getRouter().sendMessage(this.label, new DependencyGraphBuiltMessage());
        }
    }

    private void fire() {
//        System.out.println("Program#" + label + " is asked to share these literals: " + getAskedLiterals());
        Set<Literal> newDerived = solver.findSmallestModel(smallestModel);
        newDerived.removeAll(smallestModel);
        
        if (!newDerived.isEmpty()) {
//            todo filter already sent
            Map<String, List<Literal>> literalsToSend = new HashMap<>();
            newDerived.forEach(lit -> {
                askedLiterals.get(lit).forEach(prog -> {
                    if (!literalsToSend.containsKey(prog)) {
                        literalsToSend.put(prog, new ArrayList<>());
                    }
                    literalsToSend.get(prog).add(lit);
                });
            });

//            System.out.println(literalsToSend);   
            for (Map.Entry<String, List<Literal>> entry : literalsToSend.entrySet()) {
                getRouter().sendMessage(entry.getKey(), new FireRequestMessage(label, entry.getValue()));                
            }
            smallestModel.addAll(newDerived);
        }
    }

    private void checkChildrenResponses() {
        if (!children.containsValue(Boolean.FALSE) && this.notifyParticipationConfirmed) {
            if (!this.isInitialProgram) {
                getRouter().sendMessage(this.parent, new GetResponseMessage(this.label));
            } else {
                getRouter().sendMessage(this.label, new DependencyGraphBuiltMessage());
            }
        }
    }

    private void checkRules(String initialSender) {
        Map<String, List<Literal>> externals = new HashMap<>();

        rules.forEach(rule -> {
            rule.getBody().stream().forEach(lit -> {
                String litRef = lit.getProgramLabel();
                if (!litRef.equals(this.label)) {
                    if (!externals.containsKey(litRef)) {
                        externals.put(litRef, new ArrayList<>());
                    }
                    externals.get(litRef).add(lit);
                }
            });
        });

        children = new HashMap<>();
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

    /**
     * @return the participatedPrograms
     */
    public Set<String> getParticipatedPrograms() {
        return participatedPrograms;
    }

    /**
     * @return the askedLiterals
     */
    public Map<Literal, Set<String>> getAskedLiterals() {
        return askedLiterals;
    }

    /**
     * @return the smallestModel
     */
    public Set<Literal> getSmallestModel() {
        return smallestModel;
    }

    /**
     * @return the running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }
}
