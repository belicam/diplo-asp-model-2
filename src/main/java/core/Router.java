/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author martin
 */
public class Router {

    private Map<String, Program> programs;

    public Router() {
        this.programs = new HashMap<>();
    }

    public void addProgram(Program p) {
        getPrograms().put(p.getLabel(), p);
    }

    public void sendMessage(String programName, Object message) {
//        System.out.println("`" + message + "` sent to: Program#" + programName);
        getPrograms().get(programName).receiveMessage(message);
    }
    
    public void broadcastMessage(Object message) {
//        System.out.println("Broadcasting: `" + message + "`");
        getPrograms().values().forEach(program -> program.receiveMessage(message));
    }
    
    public void sendMessage(Set<String> programs, Object message) {
//        System.out.println("`" + message + "` sent to programs: " + programs);
        programs.forEach(programName -> getPrograms().get(programName).receiveMessage(message));        
    }

    /**
     * @return the programs
     */
    public Map<String, Program> getPrograms() {
        return programs;
    }

    public void setPrograms(Map<String, Program> programs) {
        this.programs = programs;
    }
}
