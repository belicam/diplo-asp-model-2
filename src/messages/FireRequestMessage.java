/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import core.Literal;
import java.util.Set;

/**
 *
 * @author martin
 */
public class FireRequestMessage {
    private String senderLabel;
    private Set<Literal> lits;
    
    public FireRequestMessage(String senderLabel, Set<Literal> lits) {
        this.senderLabel = senderLabel;
        this.lits = lits;
    }

    @Override
    public String toString() {
        return "FireRequestMessage: Program#" + senderLabel + " sends " + lits;
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
     * @return the lits
     */
    public Set<Literal> getLits() {
        return lits;
    }

    /**
     * @param lits the lits to set
     */
    public void setLits(Set<Literal> lits) {
        this.lits = lits;
    }
}
