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
public class FireResponseMessage extends Message {

    public FireResponseMessage(int id, String senderLabel, int referenceId) {
        super(id, senderLabel);
        this.setReferenceId(referenceId);
    }

    @Override
    public String toString() {
        return "FireResponseMessage: Program#" + getSenderLabel() + " responds to fire";
    }

}
