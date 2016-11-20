/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author martin
 */
public abstract class Literal implements Comparable<Literal> {
    protected String value;
    private String programLabel;
    
        /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
        this.setProgramLabel(value.split(":")[0]);
    }

    /**
     * @return the programLabel
     */
    public String getProgramLabel() {
        return programLabel;
    }

    /**
     * @param programLabel the programLabel to set
     */
    public void setProgramLabel(String programLabel) {
        this.programLabel = programLabel;
    }

}
