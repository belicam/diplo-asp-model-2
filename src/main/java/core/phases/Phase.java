/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.phases;

/**
 *
 * @author martin
 */
public interface Phase {
    public void handleMessage(Object message);
    public void sendMessage(String receiverLabel, Object message);
}
