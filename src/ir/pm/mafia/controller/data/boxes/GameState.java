package ir.pm.mafia.controller.data.boxes;

import ir.pm.mafia.controller.data.State;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is build by server.
 * It contains current state of game.
 * Server sends and client receive.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class GameState implements Serializable {

    /**
     * This contains the current state of game loop
     */
    private final State state;
    /**
     * This list contains other users' names/nickname of the same state!
     */
    private final ArrayList<String> listOfOtherUsers;

    /**
     * Constructor of GameState
     * Sets important data
     * @param state state
     * @param listOfOtherUsers list of other users in the same state
     */
    public GameState(State state, ArrayList<String> listOfOtherUsers){
        this.state = state;
        this.listOfOtherUsers = listOfOtherUsers;
    }

    // Getters
    public State getState() {
        return state;
    }
    public String getOtherUser(){
        if(listOfOtherUsers.size() > 0)
            return listOfOtherUsers.remove(0);
        else
            return null;
    }

}
