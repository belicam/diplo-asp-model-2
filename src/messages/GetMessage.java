/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import core.Literal;

/**
 *
 * @author martin
 */
public class GetMessage {
    private String senderLabel;
    private Literal lit;
    
    public GetMessage(String senderLabel, Literal lit) {
        this.senderLabel = senderLabel;
        this.lit = lit;
    }
    
    @Override
    public String toString() {
        return "GetMessage: Program#" + senderLabel + " asks for " + lit;
    }

    /**
     * @return the senderLabel
     */
    public String getSenderLabel() {
        return senderLabel;
    }

    /**
     * @return the lit
     */
    public Literal getLit() {
        return lit;
    }

    /**
     * @param senderLabel the senderLabel to set
     */
    public void setSenderLabel(String senderLabel) {
        this.senderLabel = senderLabel;
    }

    /**
     * @param lit the lit to set
     */
    public void setLit(Literal lit) {
        this.lit = lit;
    }
}
