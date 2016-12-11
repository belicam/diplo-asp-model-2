/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solver;

import core.Literal;
import core.Rule;
import java.util.HashSet;

/**
 *
 * @author martin
 */
public class NodeRule extends Node {

    private Rule rule;
    private NodeLiteral literal;

    private int bodyNotSatisfiedCount;

    public NodeRule() {
        this.rule = new Rule();
        this.literal = null;
        this.bodyNotSatisfiedCount = 0;
    }

    public NodeRule(Rule rule, NodeLiteral literal) {
        this.rule = rule;
        this.literal = literal;
        this.bodyNotSatisfiedCount = rule.getBody().size();
    }

    public NodeRule(Rule rule) {
        this.rule = rule;
        this.literal = null;
        this.bodyNotSatisfiedCount = rule.getBody().size();
    }

    public void fire(NodeLiteral nlit, HashSet<Literal> smodel) {
        if (nlit != null) {
            bodyNotSatisfiedCount--;
        }
        if (bodyNotSatisfiedCount == 0) {
            smodel.add(this.literal.getLiteral());
            this.literal.fire(smodel);
        }
    }

    /**
     * @return the rule
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * @param rule the rule to set
     */
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    /**
     * @return the literal
     */
    public NodeLiteral getNodeLiteral() {
        return literal;
    }

    /**
     * @param literal the literal to set
     */
    public void setNodeLiteral(NodeLiteral literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        return (this.literal == null ? "" : this.literal.toString() + " <= ") + "R(" + this.rule.toString() + ")";
    }

    /**
     * @return the bodyNotSatisfiedCount
     */
    public int getBodyNotSatisfiedCount() {
        return bodyNotSatisfiedCount;
    }

    /**
     * @param bodyNotSatisfiedCount the bodyNotSatisfiedCount to set
     */
    public void setBodyNotSatisfiedCount(int bodyNotSatisfiedCount) {
        this.bodyNotSatisfiedCount = bodyNotSatisfiedCount;
    }

}
