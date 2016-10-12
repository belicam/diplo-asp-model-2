/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.Literal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author martin
 */
public class Cache {

    private HashMap<Literal, NodeLiteral> cache;

    public Cache() {
        this.cache = new HashMap<>();
    }

    public NodeLiteral getCachedNodeLiteral(Literal literal) {
        if (cache.containsKey(literal)) {
            return cache.get(literal);
        }
        NodeLiteral newnl = new NodeLiteral(literal);
        cache.put(literal, newnl);
        return newnl;
    }

    public ArrayList<NodeLiteral> getCachedNodeLiteral(ArrayList<Literal> literals) {
        ArrayList<NodeLiteral> result = new ArrayList<>();
        NodeLiteral newnl;
        for (Literal literal : literals) {
            if (cache.containsKey(literal)) {
                result.add(cache.get(literal));
            } else {
                newnl = new NodeLiteral(literal);
                cache.put(literal, newnl);
                result.add(newnl);
            }
        }
        return result;
    }

}
