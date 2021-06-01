package ir.pm.mafia.controller.data;

import ir.pm.mafia.controller.data.boxes.GameState;

import java.io.Serializable;

/**
 * This class is the box of data.
 * Servers sends and client receives or vise versa!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class DataBox implements Serializable {

    /**
     * GameState contains the current state of game related data.
     * if the DataBox is send by client, it will be null!
     */
    private final GameState gameState;
    /**
     * data of data box!
     * It will be used in wanted parts
     */
    private final Data data;

    /**
     * Constructor of DataBox
     * Sets required fields.
     * It is immutable.
     * @param gameState is state of game
     * @param data is data of data box
     */
    public DataBox(GameState gameState, Data data){
        // Setting fields!
        this.gameState = gameState;
        this.data = data;
    }

    //Getters
    public GameState getGameState() {
        return gameState;
    }
    public Data getData() {
        return data;
    }

}
