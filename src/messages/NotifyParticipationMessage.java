/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

/**
 *
 * @author martin
 */
public class NotifyParticipationMessage {
    private String senderLabel;
    
    public NotifyParticipationMessage(String senderLabel) {
        this.senderLabel = senderLabel;
    }
    
    @Override
    public String toString() {
        return "NotifyParticipationMessage: Program#" + senderLabel + " is participating.";
    }

    /**
     * @return the senderLabel
     */
    public String getSenderLabel() {
        return senderLabel;
    }

    /**
     * @param senderLabel the senderLabel to set
     */
    public void setSenderLabel(String senderLabel) {
        this.senderLabel = senderLabel;
    }
}
