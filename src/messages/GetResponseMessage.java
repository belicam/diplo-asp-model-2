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
public class GetResponseMessage extends Message {
    
    public GetResponseMessage(int id, String senderLabel) {
        super(id, senderLabel);
    }
    
    @Override
    public String toString() {
        return "GetResponseMessage: Program#" + getSenderLabel() + " responds to get";
    }
}
