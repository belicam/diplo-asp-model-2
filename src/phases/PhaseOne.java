/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phases;

import core.Literal;
import core.Program;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import messages.DependencyGraphBuiltMessage;
import messages.GetRequestMessage;
import messages.GetResponseMessage;
import messages.Message;
import messages.NotifyParticipationRequestMessage;
import messages.NotifyParticipationResponseMessage;

/**
 *
 * @author martin
 */
public class PhaseOne implements Phase {

    Program program;

    private ActiveMessages activeMessages;
    private Map<String, Object> resolvedParent = new HashMap<>();
    private boolean rulesChecked;

    public PhaseOne(Program program) {
        this.program = program;
        this.activeMessages = new ActiveMessages();
        this.rulesChecked = false;
    }

    @Override
    public void sendMessage(String receiverLabel, Object message) {
        program.sendMessage(receiverLabel, message);
    }

    @Override
    public void handleMessage(Object message) {
        if (message instanceof GetRequestMessage) {
            processGetRequest(message);
        } else if (message instanceof GetResponseMessage) {
            processGetResponse(message);
        } else if (message instanceof NotifyParticipationRequestMessage) {
            processNotifyParticipationRequest(message);
        } else if (message instanceof NotifyParticipationResponseMessage) {
            processNotifyParticipationResponse(message);
        }
    }

    private void processGetRequest(Object message) {
        System.out.println("phases.PhaseOne.processGetRequest()");
        GetRequestMessage request = (GetRequestMessage) message;
        String from = request.getSenderLabel();
        program.setInitialProgramLabel(request.getInitialSender());

        request.getLits().forEach(lit -> {
            if (!program.getAskedLiterals().containsKey(lit)) {
                program.getAskedLiterals().put(lit, new HashSet<>());
            }
            program.getAskedLiterals().get(lit).add(from);
        });

        if (rulesChecked) {
            sendMessage(from, new GetResponseMessage(program.generateMessageId(), program.getLabel(), request.getId()));
        } else {
            checkRules(message, program.getInitialProgramLabel());
            sendMessage(program.getInitialProgramLabel(), new NotifyParticipationRequestMessage(program.generateMessageId(), program.getLabel()));

            if (activeMessages.noMessages()) {
                sendMessage(from, new GetResponseMessage(program.generateMessageId(), program.getLabel(), request.getId()));
            }
        }
    }

    private void processGetResponse(Object message) {
        String from = ((GetResponseMessage) message).getSenderLabel();

        resolvedParent = activeMessages.resolveChildMessage(from, ((GetResponseMessage) message).getReferenceId());
        checkGetResponses();
    }

    private void processNotifyParticipationRequest(Object message) {
        String senderLabel = ((NotifyParticipationRequestMessage) message).getSenderLabel();

        program.getParticipatedPrograms().add(senderLabel);
        sendMessage(senderLabel, new NotifyParticipationResponseMessage(program.generateMessageId(), program.getLabel()));
    }

    private void processNotifyParticipationResponse(Object message) {
        program.setParticipationConfirmed(true);
        checkGetResponses();
    }

    private void checkRules(Object parentMessage, String initialSender) {
        Map<String, List<Literal>> externals = new HashMap<>();

        program.getRules().forEach(rule -> {
            rule.getBody().stream().forEach(lit -> {
                String litRef = lit.getProgramLabel();
                if (!litRef.equals(program.getLabel())) {
                    if (!externals.containsKey(litRef)) {
                        externals.put(litRef, new ArrayList<>());
                    }
                    externals.get(litRef).add(lit);
                }
            });
        });

        externals.keySet().forEach(key -> {
            Object childMessage = new GetRequestMessage(program.generateMessageId(), program.getLabel(), initialSender, externals.get(key));
            program.getRouter().sendMessage(key, childMessage);

            activeMessages.addChildMessage(parentMessage, key, childMessage);
        });
        rulesChecked = true;
    }

    private void checkGetResponses() {
        if (program.isParticipationConfirmed() && activeMessages.noMessages()) {
            if (program.isInitialProgram()) {
                sendMessage(program.getLabel(), new DependencyGraphBuiltMessage());
            } else {
                resolvedParent.entrySet().forEach((parent) -> {
                    int refId = ((Message) parent.getValue()).getId();
                    sendMessage(parent.getKey(), new GetResponseMessage(program.generateMessageId(), program.getLabel(), refId));
                });
            }
        }
    }
}
