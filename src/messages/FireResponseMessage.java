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

    private Object requestMessage;

    public FireResponseMessage(int id, String senderLabel, Object requestMessage) {
        super(id, senderLabel);
        this.requestMessage = requestMessage;
    }

    @Override
    public String toString() {
        return "FireResponseMessage: Program#" + getSenderLabel() + " responds to fire";
    }

    /**
     * @return the requestMessage
     */
    public Object getRequestMessage() {
        return requestMessage;
    }

    /**
     * @param requestMessage the requestMessage to set
     */
    public void setRequestMessage(Message requestMessage) {
        this.requestMessage = requestMessage;
    }
}
