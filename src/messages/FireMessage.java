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
public class FireMessage {
    private String senderLabel;
    private Literal lit;
    private Boolean isInModel;
    
    public FireMessage(String senderLabel, Literal lit, Boolean isInModel) {
        this.senderLabel = senderLabel;
        this.lit = lit;
        this.isInModel = isInModel;
    }

    @Override
    public String toString() {
        return "FireMessage: Program#" + senderLabel + " sends " + lit + " value: " + isInModel;
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
     * @return the lit
     */
    public Literal getLit() {
        return lit;
    }

    /**
     * @param lit the lit to set
     */
    public void setLit(Literal lit) {
        this.lit = lit;
    }

    /**
     * @return the isInModel
     */
    public Boolean getIsInModel() {
        return isInModel;
    }

    /**
     * @param isInModel the isInModel to set
     */
    public void setIsInModel(Boolean isInModel) {
        this.isInModel = isInModel;
    }
}
