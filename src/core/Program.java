/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import phases.ActiveMessages;
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
import messages.FiringEndedMessage;
import messages.GetRequestMessage;
import messages.GetResponseMessage;
import messages.InitMessage;
import messages.Message;
import messages.NotifyParticipationRequestMessage;
import messages.NotifyParticipationResponseMessage;
import messages.StopMessage;
import phases.Phase;
import phases.PhaseOne;
import phases.PhaseTwo;

/**
 *
 * @author martin
 */
public class Program implements Runnable {

    private boolean running;
    private String initialProgramLabel = null;
    private int messageIdCounter = 0;

    private boolean participationConfirmed = false;
    private final Set<String> participatedPrograms = new HashSet<>();

    private String label;
    private List<Rule> rules = new ArrayList<>();

    private final BlockingQueue<Object> messages = new LinkedBlockingQueue<>();
    private final Set<Literal> smallestModel = new HashSet<>();

    private Phase phase;
    private Router router;

    private final Map<Literal, Set<String>> askedLiterals = new HashMap<>();

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
        while (isRunning()) {
            try {
                Object message = getMessages().take();
                processMessage(message);
            } catch (InterruptedException ex) {
                Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void sendMessage(String receiverLabel, Object message) {
        router.sendMessage(receiverLabel, message);
    }

    public void receiveMessage(Object message) {
        messages.add(message);
    }

    private void processMessage(Object message) {
        if (message != null) {
            if (message instanceof InitMessage) {
                processInit();
            } else if (message instanceof StopMessage) {
                processStop();
            } else if (message instanceof ActivationMessage) {
                processActivation();
            } else if (message instanceof DependencyGraphBuiltMessage) {
                processDependencyGraphBuilt();
            } else {
                if (phase == null) {
                    this.phase = new PhaseOne(this);
                }
                phase.handleMessage(message);
            }
        }
    }

    private void processActivation() {
        phase = new PhaseTwo(this);
        router.sendMessage(label, new FireRequestMessage(generateMessageId(), label, new HashSet<>()));
    }

    private void processDependencyGraphBuilt() {
        router.sendMessage(participatedPrograms, new ActivationMessage(generateMessageId(), this.label));
    }

    private void processInit() {
        this.setInitialProgramLabel(label);
        router.sendMessage(label, new GetRequestMessage(generateMessageId(), label, label, new ArrayList<>()));
    }

    public void processStop() {
        this.setRunning(false);
        phase = null;
        System.out.println("Program#" + label + " ended with model: " + smallestModel + ", messagesSent: " + messageIdCounter);
//        return;
    }

    public int generateMessageId() {
        return messageIdCounter++;
    }

    public boolean isInitialProgram() {
        return initialProgramLabel.equals(label);
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

    /**
     * @return the initialProgramLabel
     */
    public String getInitialProgramLabel() {
        return initialProgramLabel;
    }

    /**
     * @param initialProgramLabel the initialProgramLabel to set
     */
    public void setInitialProgramLabel(String initialProgramLabel) {
        this.initialProgramLabel = initialProgramLabel;
    }

    /**
     * @return the participationConfirmed
     */
    public boolean isParticipationConfirmed() {
        return participationConfirmed;
    }

    /**
     * @param participationConfirmed the participationConfirmed to set
     */
    public void setParticipationConfirmed(boolean participationConfirmed) {
        this.participationConfirmed = participationConfirmed;
    }
}
