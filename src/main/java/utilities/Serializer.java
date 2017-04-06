/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author martin
 */
public class Serializer {

    public static Object deserialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        Object o = null;

        try {
            in = new ObjectInputStream(bis);
            o = in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
            }
        }
        return o;
    }

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;

        byte[] result = null;

        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.flush();
            result = bos.toByteArray();
        } catch (IOException ex) {
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {

            }
        }
        return result;

    }

}
