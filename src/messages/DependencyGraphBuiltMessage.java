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
public class DependencyGraphBuiltMessage extends Message {

    public DependencyGraphBuiltMessage() {
        super(null);
    }

    @Override
    public String toString() {
        return "DependencyGraphBuiltMessage";
    }

}
