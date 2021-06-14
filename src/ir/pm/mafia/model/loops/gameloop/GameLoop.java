package ir.pm.mafia.model.loops.gameloop;

import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.model.player.Player;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;
import ir.pm.mafia.view.console.Color;
import ir.pm.mafia.view.console.Console;
import ir.pm.mafia.view.ui.Interface;
import ir.pm.mafia.view.ui.interfaces.ChatRoomUI;

/**
 * This is the game loop (client loop).
 * Checks game state and updates game UI!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0.1
 */
public class GameLoop extends Runnable {

    /**
     * Player information
     */
    private Player player;
    /**
     * Current UI of game;
     */
    private Interface currentUI;
    /**
     * Current state of game
     */
    private State currentState;
    /**
     * This is the shared memory we give to UI handlers!
     */
    private final SharedMemory sharedUIReader;
    /**
     * console is used to print information
     */
    private final Console console;
    /**
     * Tells if game has ended!
     */
    private boolean gameEnded;

    /**
     * Constructor of GameLoop.
     * @param player contains player info and shared memories
     * @throws Exception if client was not connected when we initialized server!
     */
    public GameLoop(Player player) throws Exception {
        if(player == null)
            throw new Exception("Null input");
        sharedUIReader = new SharedMemory(true);
        console = Console.getConsole();
        this.player = player;
        currentUI = null;
        currentState = null;
        gameEnded = false;
    }

    /**
     * Running player game loop
     */
    @Override
    public void run() {
        if(currentUI == null)
            console.println(Color.YELLOW + "Waiting for server respond!");
        DataBox receivedDataBox = null;
        GameState gameState;
        while (!finished){
            try {
                receivedDataBox = (DataBox) player.getReceiveBox().get();
                if(receivedDataBox == null)
                    continue;
                gameState = receivedDataBox.getGameState();
                if(gameState == null)
                    continue;
                uiUpdater(gameState.getState());
                if(receivedDataBox.getData() != null)
                    sharedUIReader.put(receivedDataBox);
            }catch (Exception e){
                Logger.error("Failed in to process game loop",
                        LogLevel.GameInterrupted, "GameLoop");
            }
        }
    }

    /**
     * Shutdown the thread!
     */
    @Override
    public void shutdown(){
        finished = true;
        if(currentUI != null)
            currentUI.shutdown();
        this.close();
    }

    /**
     * Updates Current UI!
     * According to changed state
     */
    private void uiUpdater(State state){
        if(state == null)
            return;
        if(state == currentState)
            return;

        // Shutting down previous UI!
        if(currentUI != null)
            currentUI.shutdown();
        currentState = state;

        // Lobby builder!
        if(state == State.Lobby){
            try {
                currentUI = new ChatRoomUI(player.getSendBox(), sharedUIReader, player.getToken(),
                        player.getNickname(), "Lobby");
                currentUI.start();
            } catch (Exception e) {
                currentUI = null;
                Logger.error("Failed to build lobby" + e.getMessage(),
                        LogLevel.GameInterrupted, "GameLoop");
            }
        }

        // Building UI for day of game
        else if(state == State.Day){

        }

        // Building UI for voting process
        else if(state == State.Vote){

        }

        // Building UI for night
        else if(state == State.Night){

        }

        // Ending the game
        else if(state == State.FINISHED){
            if(currentUI != null)
                currentUI.shutdown();
            console.println(Color.YELLOW_BOLD + "Game Ended!");
            gameEnded = true;
        }

    }

    // Getters
    public boolean isGameEnded() {
        return gameEnded;
    }

}
