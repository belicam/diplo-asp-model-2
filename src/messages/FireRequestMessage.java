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
public class FireRequestMessage extends Message {
    private Set<Literal> lits;
    
    public FireRequestMessage(String senderLabel, Set<Literal> lits) {
        super(senderLabel);
        this.lits = lits;
    }

    @Override
    public String toString() {
        return "FireRequestMessage: Program#" + getSenderLabel() + " sends " + getLits();
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
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (!(obj instanceof FireRequestMessage)) {
            return false;
        }
        
        FireRequestMessage msg = (FireRequestMessage) obj;
        return msg.getSenderLabel().equals(getSenderLabel()) && msg.getLits().equals(getLits());
    }
    
    @Override
    public int hashCode() {
        return (getSenderLabel() + getLits().toString()).hashCode();
    }
}
