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
import messages.FiringEndedMessage;
import messages.GetRequestMessage;
import messages.GetResponseMessage;
import messages.InitMessage;
import messages.Message;
import messages.NotifyParticipationRequestMessage;
import messages.NotifyParticipationResponseMessage;
import messages.StopMessage;
import solver.TreeSolver;

/**
 *
 * @author martin
 */
public class Program implements Runnable {

    private boolean running;
    private String initialProgramLabel = null;
    private int idCounter = 0;

    private final Set<String> participatedPrograms = new HashSet<>();
    private String label;
    private List<Rule> rules = new ArrayList<>();

    private final BlockingQueue<Object> messages = new LinkedBlockingQueue<>();
    private final Set<Literal> smallestModel = new HashSet<>();

    private Router router;
    private TreeSolver solver;
    private final ActiveMessagesStore activeMessages = new ActiveMessagesStore();

    private String parent = null;
    private Map<String, Boolean> children = null;
    private boolean notifyParticipationConfirmed = false;

    private final Map<Literal, Set<String>> askedLiterals = new HashMap<>();
    
    private final Map<String, Boolean> participatedFiringEnded = new HashMap<>();

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
            } else if (message instanceof FiringEndedMessage) {
                processFiringEnded(message);
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
        initialProgramLabel = ((GetRequestMessage) message).getInitialSender();

        ((GetRequestMessage) message).getLits().forEach(lit -> {
            if (!this.askedLiterals.containsKey(lit)) {
                this.getAskedLiterals().put(lit, new HashSet<>());
            }
            this.getAskedLiterals().get(lit).add(from);
        });

        if (this.children != null) {
            getRouter().sendMessage(from, new GetResponseMessage(idCounter++, this.label));
        } else {
            this.parent = from;
            checkRules(initialProgramLabel);
            getRouter().sendMessage(initialProgramLabel, new NotifyParticipationRequestMessage(idCounter++, this.label));
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
        FireRequestMessage requestMessage = (FireRequestMessage) message;
        Set<Literal> obtainedLiterals = requestMessage.getLits();
        String sender = requestMessage.getSenderLabel();

        smallestModel.addAll(obtainedLiterals);
        fire(requestMessage);

        // ziadne nove message z tejto `requestMessage` nevznikli, tak rovno odpovedam 
        if (activeMessages.messageHasNoChildren(requestMessage)) {
            getRouter().sendMessage(sender, new FireResponseMessage(idCounter++, label, requestMessage));
        }
    }

    private void processFireResponse(Object message) {
        FireResponseMessage responseMessage = (FireResponseMessage) message;
        FireRequestMessage initialRequestMessage = (FireRequestMessage) responseMessage.getRequestMessage();
        String senderLabel = responseMessage.getSenderLabel();

//        response som si poslal sam sebe
        if (senderLabel.equals(label)) {
            getRouter().sendMessage(initialProgramLabel, new FiringEndedMessage(label));
        } else {
//        vymazem v mape request message, na ktoru prisla odpoved | poslem response ak po vymazani je prazdne pole
            activeMessages.resolveChildMessage(senderLabel, initialRequestMessage).entrySet().forEach(resolved -> {
                getRouter().sendMessage(resolved.getKey(), new FireResponseMessage(idCounter++, label, resolved.getValue()));
            });
        }
    }

    private void processActivation(Object message) {
        solver = new TreeSolver((ArrayList<Rule>) rules);

//            init participatedFiringEnded
        if (isInitialProgram()) {
            participatedFiringEnded.clear();
            participatedPrograms.forEach((program) -> participatedFiringEnded.put(program, Boolean.FALSE));
        }

        getRouter().sendMessage(label, new FireRequestMessage(idCounter++, label, new HashSet<>()));
    }

    private void processNotifyParticipationRequest(Object message) {
        String senderLabel = ((NotifyParticipationRequestMessage) message).getSenderLabel();

        getParticipatedPrograms().add(senderLabel);
        getRouter().sendMessage(senderLabel, new NotifyParticipationResponseMessage(idCounter++, this.label));
    }

    private void processNotifyParticipationResponse(Object message) {
        this.notifyParticipationConfirmed = true;
        checkChildrenResponses();
    }

    private void processFiringEnded(Object message) {
        String sender = ((FiringEndedMessage) message).getSenderLabel();
        if (!participatedFiringEnded.get(sender)) {
            participatedFiringEnded.put(sender, Boolean.TRUE);

            // test  ci skoncili vsetci
            if (!participatedFiringEnded.containsValue(Boolean.FALSE)) {
                getRouter().broadcastMessage(new StopMessage());
                System.out.println("Program#" + label + " ended with model: " + smallestModel + ", messagesSent: " + idCounter);
            }
        }
    }

    private void processDependencyGraphBuilt() {
        getRouter().sendMessage(participatedPrograms, new ActivationMessage(idCounter++, this.label));
    }

    private void processInit() {
        this.initialProgramLabel = label;

        getRouter().sendMessage(label, new NotifyParticipationRequestMessage(idCounter++, label));

        checkRules(this.label);

        if (children.isEmpty()) {
            getRouter().sendMessage(this.label, new DependencyGraphBuiltMessage());
        }
    }

    private void fire(Object parentRequestMessage) {
        Set<Literal> newDerived = solver.findSmallestModel(smallestModel);
        newDerived.removeAll(smallestModel);

        System.out.println("core.Program.fire()#" + label + " newDerived: " + newDerived);

        if (!newDerived.isEmpty()) {
            Map<String, Set<Literal>> literalsToSend = new HashMap<>();
            newDerived.forEach(lit -> {
                if (askedLiterals.containsKey(lit)) {
                    askedLiterals.get(lit).forEach(prog -> {
                        if (!literalsToSend.containsKey(prog)) {
                            literalsToSend.put(prog, new HashSet<>());
                        }
                        literalsToSend.get(prog).add(lit);
                    });
                }
            });

            literalsToSend.entrySet().stream().forEach((entry) -> {
                Message childMessage = new FireRequestMessage(idCounter++, label, entry.getValue());
                activeMessages.addChildMessage(parentRequestMessage, entry.getKey(), childMessage);
                getRouter().sendMessage(entry.getKey(), childMessage);
            });
            smallestModel.addAll(newDerived);
        } else {
        }
    }

    private void checkChildrenResponses() {
        if (!children.containsValue(Boolean.FALSE) && this.notifyParticipationConfirmed) {
            if (!this.isInitialProgram()) {
                getRouter().sendMessage(this.parent, new GetResponseMessage(idCounter++, this.label));
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
            getRouter().sendMessage(key, new GetRequestMessage(idCounter++, label, initialSender, externals.get(key)));
            children.put(key, Boolean.FALSE);
        });
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
}
