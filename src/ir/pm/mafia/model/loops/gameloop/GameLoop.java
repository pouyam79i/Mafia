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
import ir.pm.mafia.view.ui.interfaces.NightUI;
import ir.pm.mafia.view.ui.interfaces.VoteUI;

import java.util.ArrayList;

/**
 * This is the game loop (client loop).
 * Checks game state and updates game UI!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0.3
 */
public class GameLoop extends Runnable {

    /**
     * Player information
     */
    private final Player player;
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
        threadName = "GameLoop";
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
        ArrayList<String> listOfPlayers = null;
        while (!finished){
            try {
                receivedDataBox = (DataBox) player.getReceiveBox().get();
                if(receivedDataBox == null)
                    continue;
                gameState = receivedDataBox.getGameState();
                if(gameState != null){
                    try {
                        listOfPlayers = gameState.getListOfPlayers();
                    }catch (Exception ignored){
                        listOfPlayers = null;
                    }
                    uiUpdater(gameState.getState(), listOfPlayers);
                }
                if(receivedDataBox.getData() != null)
                    sharedUIReader.put(receivedDataBox);
            }catch (Exception e){
                Logger.error("Failed in to process game loop" + e.getMessage(),
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
    private void uiUpdater(State state, ArrayList<String> sameStatePlayers){
        if(state == null)
            return;
        if(state == currentState)
            return;

        // Shutting down previous UI!
        if(currentUI != null){
            currentUI.shutdown();
            currentUI = null;
        }

        currentState = state;

        // Lobby builder!
        if(state == State.Lobby){
            try {
                currentUI = new ChatRoomUI(player.getSendBox(), sharedUIReader, player.getToken(),
                        player.getNickname(), "Lobby", false);
                currentUI.start();
            } catch (Exception e) {
                currentUI = null;
                Logger.error("Failed to build lobby" + e.getMessage(),
                        LogLevel.GameInterrupted, "GameLoop");
            }
        }

        // Building UI for day of game
        else if(state == State.Day){
            try {
                currentUI = new ChatRoomUI(player.getSendBox(), sharedUIReader, player.getToken(),
                        player.getNickname(), Color.YELLOW_BOLD + "Day", true);
                currentUI.start();
            } catch (Exception e) {
                Logger.error("Failed to build day chat room" + e.getMessage(),
                        LogLevel.GameInterrupted, "GameLoop");
            }
        }

        // Building UI for voting process
        else if(state == State.Vote){
            try {
                currentUI = new VoteUI(player.getSendBox(), sharedUIReader, player.getToken(),
                        player.getNickname(), sameStatePlayers);
                currentUI.start();
            }catch (Exception e){
                Logger.error("Failed to build vote room" + e.getMessage(),
                        LogLevel.GameInterrupted, "GameLoop");
            }
        }

        // Building UI for night
        else if(state == State.Night){
            try {
                currentUI = new NightUI(player.getSendBox(), sharedUIReader, player.getToken(),
                        player.getNickname(), sameStatePlayers);
            } catch (Exception e) {
                Logger.error("Failed to build night" + e.getMessage(),
                        LogLevel.GameInterrupted, "GameLoop");
            }
        }

        // Ending the game
        else if(state == State.FINISHED){
            if(currentUI != null)
                currentUI.shutdown();
            console.println(Color.YELLOW_BOLD + "Game Ended!");
            this.shutdown();
            gameEnded = true;
        }

    }

    // Getters
    public boolean isGameEnded() {
        return gameEnded;
    }

}
