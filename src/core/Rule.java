/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author martin
 */
public class Rule {

    private Literal head;
    private ArrayList<Literal> body;

    public Rule() {
        this.body = new ArrayList<>();
    }

    public Rule(Literal head, ArrayList<Literal> body) {
        this.head = head;
        this.body = body;
    }
    
    /**
     * @return the head
     */
    public Literal getHead() {
        return head;
    }

    /**
     * @param head the head to set
     */
    public void setHead(Literal head) {
        this.head = head;
    }

    public void addToBody(Literal l) {
        this.body.add(l);
    }

    /**
     * @return the body
     */
    public ArrayList<Literal> getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(ArrayList<Literal> body) {
        this.body = body;
    }

    public String toString() {
        String h = this.head == null ? "" : this.head.toString();
        String b = this.body.isEmpty() ? "" : this.body.toString();

        return h + " <- " + b;
    }

    public boolean isFact() {
        return this.body.isEmpty();
    }

    public boolean isBodySatisfied(HashSet<Literal> facts) {
        if (this.isFact()) {
            return true;
        }
        boolean res;
        for (Literal b : this.body) {
            res = false;
            if (facts.contains(b)) {
                res = true;
            }
            if (!res) {
                return false;
            }
        }
        return true;
    }
}
