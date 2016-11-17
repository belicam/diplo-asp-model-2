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
public class FireResponseMessage {

//     todo
    private String senderLabel;

    public FireResponseMessage() {

    }

    @Override
    public String toString() {
        return "FireResponseMessage: Program#" + senderLabel + "responds to fire";
    }
}
