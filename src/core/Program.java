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

    private boolean isInitialProgram = false;
    private boolean notifyParticipationConfirmed = false;
    private boolean running;
    private final Set<String> participatedPrograms = new HashSet<>();
    private String label;
    private List<Rule> rules = new ArrayList<>();

    private final Set<Literal> smallestModel = new HashSet<>();

    private Router router;
    private TreeSolver solver;

    private String parent = null;
    private Map<String, Boolean> children = null;
    private final BlockingQueue<Object> messages = new LinkedBlockingQueue<>();

    private final Map<Literal, Set<String>> askedLiterals = new HashMap<>();

    private Map<Message, List<Message>> fireMessages = new HashMap<>();

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
        FireRequestMessage requestMessage = (FireRequestMessage) message;
        Set<Literal> obtainedLiterals = requestMessage.getLits();
        String sender = requestMessage.getSenderLabel();

//        System.out.println("core.Program.processFireRequest()#" + label + " " + obtainedLiterals);
        smallestModel.addAll(obtainedLiterals);
        fire(requestMessage);

        if (!fireMessages.containsKey(requestMessage) || fireMessages.get(requestMessage).isEmpty()) {
            getRouter().sendMessage(sender, new FireResponseMessage(label, requestMessage));
        }
    }

    private void processFireResponse(Object message) {
        FireResponseMessage responseMessage = (FireResponseMessage) message;
        FireRequestMessage initialRequestMessage = (FireRequestMessage) responseMessage.getRequestMessage();

//        vymazem v mape request message, na ktoru prisla odpoved | poslem response ak po vymazani je prazdne pole
        fireMessages.entrySet().forEach((messagesEntry) -> {
            if (messagesEntry.getValue().contains(initialRequestMessage)) {
                messagesEntry.getValue().remove(initialRequestMessage);
//                pridana kontrola aby neposielal sebe samemu response | kvoli prvej fireRequestMessage v processActivation
                if (messagesEntry.getValue().isEmpty() && !messagesEntry.getKey().getSenderLabel().equals(label)) {
                    getRouter().sendMessage(messagesEntry.getKey().getSenderLabel(), new FireResponseMessage(label, messagesEntry.getKey()));
                }
            }
        });
        
        boolean fireMessagesResolved = fireMessages.values().stream().allMatch((requests) -> requests.isEmpty());
        if (fireMessagesResolved && this.isInitialProgram) {
            getRouter().broadcastMessage(new StopMessage());
            System.out.println("Program#" + label + " ended with model: " + smallestModel);
        }
    }

    private void processActivation(Object message) {
        solver = new TreeSolver((ArrayList<Rule>) rules);
        fire(new FireRequestMessage(label, new HashSet<>()));
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
        // getRouter().broadcastMessage(new StopMessage()); // stop message for dep graph testing
    }

    private void processInit() {
        this.isInitialProgram = true;
        this.notifyParticipationConfirmed = true;
        checkRules(this.label);

        if (children.isEmpty()) {
            getRouter().sendMessage(this.label, new DependencyGraphBuiltMessage());
        }
    }

    private void fire(Message parentRequestMessage) {
        Set<Literal> newDerived = solver.findSmallestModel(smallestModel);
        newDerived.removeAll(smallestModel);
//        System.out.println("core.Program.fire()#" + label + " " + newDerived);

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
                Message newRequestMessage = new FireRequestMessage(label, entry.getValue());
                if (parentRequestMessage == null) {
                    getRouter().sendMessage(entry.getKey(), newRequestMessage);
                } else if (fireMessages.containsKey(parentRequestMessage)) {
                    if (!fireMessages.get(parentRequestMessage).contains(newRequestMessage)) {
                        fireMessages.get(parentRequestMessage).add(newRequestMessage);
                        getRouter().sendMessage(entry.getKey(), newRequestMessage);
                    }
                } else {
                    fireMessages.put(parentRequestMessage, new ArrayList<>());
                    fireMessages.get(parentRequestMessage).add(newRequestMessage);
                    getRouter().sendMessage(entry.getKey(), newRequestMessage);
                }
            });
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
