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
public class FiringEndedMessage extends Message {

    public FiringEndedMessage(String senderLabel) {
        super(-1, senderLabel);
    }
        
    @Override
    public String toString() {
        return "FiringEndedMessage: Program#" + getSenderLabel() + " has ended firing.";
    }
}
