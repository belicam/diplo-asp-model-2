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
public class NotifyParticipationRequestMessage {
    private String senderLabel;
    
    public NotifyParticipationRequestMessage(String senderLabel) {
        this.senderLabel = senderLabel;
    }
    
    @Override
    public String toString() {
        return "NotifyParticipationRequestMessage: Program#" + senderLabel + " is participating.";
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
