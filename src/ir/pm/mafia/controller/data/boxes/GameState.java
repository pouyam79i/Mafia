package ir.pm.mafia.controller.data.boxes;

import ir.pm.mafia.model.game.state.State;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is build by server.
 * It contains current state of game.
 * Server sends and client receive.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1
 */
public class GameState implements Serializable {

    /**
     * This contains the current state of game loop
     */
    private final State state;
    /**
     * This list contains users' token of the same state!
     */
    private final ArrayList<String> listOfPlayers;

    /**
     * Constructor of GameState
     * Sets important data
     * @param state state of game
     * @param listOfOtherUsers list of other users' token who are in the same state
     */
    public GameState(State state, ArrayList<String> listOfPlayers){
        this.state = state;
        this.listOfPlayers = listOfPlayers;
    }

    // Getters
    public State getState() {
        return state;
    }
    public ArrayList<String> getListOfPlayers() throws Exception {
        if(listOfPlayers == null)
            throw new Exception("Returning Null");
        return listOfPlayers;
    }

}
