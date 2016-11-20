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
public class NotifyParticipationResponseMessage {
    private String senderLabel;
    
    public NotifyParticipationResponseMessage(String senderLabel) {
        this.senderLabel = senderLabel;
    }
    
    @Override
    public String toString() {
        return "NotifyParticipationResponseMessage: Program#" + senderLabel + " is participating.";
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
