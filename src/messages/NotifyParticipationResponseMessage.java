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
public class NotifyParticipationResponseMessage extends Message {
    public NotifyParticipationResponseMessage(String senderLabel) {
        super(senderLabel);
    }
    
    @Override
    public String toString() {
        return "NotifyParticipationResponseMessage: participation confirmed";
    }
}
