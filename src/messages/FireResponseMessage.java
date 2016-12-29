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

    private Message requestMessage;

    public FireResponseMessage(String senderLabel, Message requestMessage) {
        super(senderLabel);
        this.requestMessage = requestMessage;
    }

    @Override
    public String toString() {
        return "FireResponseMessage: Program#" + getSenderLabel() + " responds to fire";
    }

    /**
     * @return the requestMessage
     */
    public Message getRequestMessage() {
        return requestMessage;
    }

    /**
     * @param requestMessage the requestMessage to set
     */
    public void setRequestMessage(Message requestMessage) {
        this.requestMessage = requestMessage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof FireResponseMessage)) {
            return false;
        }

        FireResponseMessage msg = (FireResponseMessage) obj;
        return msg.getSenderLabel().equals(getSenderLabel()) && msg.getRequestMessage().equals(getRequestMessage());
    }

    @Override
    public int hashCode() {
        return (getSenderLabel() + getRequestMessage().toString()).hashCode();
    }

}
