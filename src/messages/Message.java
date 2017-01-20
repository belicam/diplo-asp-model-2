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
public abstract class Message {
    private String senderLabel;
    private int id;
    
    public Message(int id, String senderLabel) {
        this.senderLabel = senderLabel;
        this.id = id;
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

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

        
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (!(obj instanceof Message) || !(obj.getClass().equals(this.getClass()))) {
            return false;
        }
        
        Message msg = (Message) obj;
        return msg.getSenderLabel().equals(getSenderLabel()) && (msg.getId() == getId());
    }
    
    @Override
    public int hashCode() {
        return (getSenderLabel() + "#" + getId()).hashCode();
    }
}
