/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import messages.Message;

/**
 *
 * @author martin
 */
public class ActiveMessagesStore {

    private Map<Object, Map<String, Set<Object>>> activeMessages = new HashMap<>();

    public void addChildMessage(Object keyMessage, String programLabel, Object childMessage) {
        if (activeMessages.containsKey(keyMessage)) {
            if (!activeMessages.get(keyMessage).containsKey(programLabel)) {
                activeMessages.get(keyMessage).put(programLabel, new HashSet<>());
            }
        } else {
            activeMessages.put(keyMessage, new HashMap<>());
            activeMessages.get(keyMessage).put(programLabel, new HashSet<>());
        }
        activeMessages.get(keyMessage).get(programLabel).add(childMessage);
    }

    public Map<String, Object> resolveChildMessage(String senderLabel, Object childMessage) {
        Map<String, Object> resolvedMessages = new HashMap<>();

        activeMessages.entrySet().forEach((messagesEntry) -> {
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

    public Map<String, Object> resolveChildMessage(String senderLabel, int childId) {
        Map<String, Object> resolvedMessages = new HashMap<>();

        activeMessages.entrySet().forEach((messagesEntry) -> {
            if (messagesEntry.getValue().containsKey(senderLabel)) {
                Set<Object> messagesToSender = messagesEntry.getValue().get(senderLabel);
                messagesToSender.removeIf((childMessage) -> ((Message) childMessage).getId() == childId);
                if (messagesToSender.isEmpty()) {
                    messagesEntry.getValue().remove(senderLabel);
                    
                    if (messagesEntry.getValue().isEmpty()) {
                        String resolvedLabel = ((Message) messagesEntry.getKey()).getSenderLabel();
                        resolvedMessages.put(resolvedLabel, messagesEntry.getKey());
                    }
                }
            }
        });

        activeMessages.entrySet().removeIf((messagesEntry) -> messageHasNoChildren(messagesEntry.getKey()));
        
        return resolvedMessages;
    }

    public boolean messageHasNoChildren(Object message) {
        return !activeMessages.containsKey(message) || activeMessages.get(message).isEmpty();
    }

    public void clearMessages() {
        activeMessages.clear();
    }

    public boolean noMessages() {
        return activeMessages.isEmpty();
    }

    /**
     * @return the activeMessages
     */
    public Map<Object, Map<String, Set<Object>>> getActiveMessages() {
        return activeMessages;
    }
}
