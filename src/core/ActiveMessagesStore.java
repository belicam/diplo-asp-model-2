/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import messages.Message;

/**
 *
 * @author martin
 */
public class ActiveMessagesStore {

    private Map<Object, Map<String, Set<Object>>> activeMessages = new HashMap<>();

    public void addChildMessage(Object keyMessage, String programLabel, Object childMessage) {
        if (getActiveMessages().containsKey(keyMessage)) {
            if (!activeMessages.get(keyMessage).containsKey(programLabel)) {
                getActiveMessages().get(keyMessage).put(programLabel, new HashSet<>());
            }
        } else {
            getActiveMessages().put(keyMessage, new HashMap<>());
            getActiveMessages().get(keyMessage).put(programLabel, new HashSet<>());
        }
        getActiveMessages().get(keyMessage).get(programLabel).add(childMessage);
    }

    public Map<String, Object> resolveChildMessage(String senderLabel, Object childMessage) {
        Map<String, Object> resolvedMessages = new HashMap<>();

        getActiveMessages().entrySet().forEach((messagesEntry) -> {
            if (messagesEntry.getValue().containsKey(senderLabel)) {
                Set<Object> messagesToSender = messagesEntry.getValue().get(senderLabel);
                messagesToSender.remove(childMessage);
                if (messagesToSender.isEmpty()) {
                    messagesEntry.getValue().remove(senderLabel);

                    if (messagesEntry.getValue().isEmpty()) {
                        String resolvedLabel = ((Message) messagesEntry.getKey()).getSenderLabel();
                        resolvedMessages.put(resolvedLabel, messagesEntry.getKey());
                    }
                }
            }
        });

        return resolvedMessages;
    }

    public boolean messageHasNoChildren(Object message) {
        return !activeMessages.containsKey(message) || getActiveMessages().get(message).isEmpty();
    }

    public void clearMessages() {
        activeMessages.clear();
    }

    /**
     * @return the activeMessages
     */
    public Map<Object, Map<String, Set<Object>>> getActiveMessages() {
        return activeMessages;
    }
}
