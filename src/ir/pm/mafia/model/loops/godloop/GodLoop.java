package ir.pm.mafia.model.loops.godloop;

import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.controller.server.Server;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;
import ir.pm.mafia.model.game.logic.*;
import ir.pm.mafia.model.game.handlers.PartHandler;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.model.game.state.StateUpdater;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;
import ir.pm.mafia.view.console.Color;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This Class is the server main loop so it is called GodLoop!
 * Handled states and part handler!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0.4
 */
public class GodLoop extends Runnable {

    /**
     * Current state of game.
     * Defined by State enum.
     */
    private State currentState;
    /**
     * State updater!
     * update state according to time :)
     */
    private final StateUpdater stateUpdater;
    /**
     * Current connection list.
     * Contains connected client handlers!
     */
    private ArrayList<ClientHandler> currentConnections;
    /**
     * Current part handler,
     * contains a logic analyzer for current state of game!
     */
    private PartHandler currentPart;
    /**
     * Game stated state!
     * if true it means game has started!
     */
    private volatile boolean gameStarted;
    /**
     * Token of admin will be used to confirm admin commands!
     */
    private final String adminToken;
    /**
     * In this shared memory we pull new list of client handlers.
     */
    private final SharedMemory connectionBox;
    /**
     * Server of game
     */
    private final Server server;
    /**
     * character of player
     */
    private HashMap<ClientHandler, Character> playerCharacters;
    /**
     * token of mayer
     */
    private String mayerToken;
    /**
     * Game starter.
     * sets characters.
     */
    private GameStarter gameStarterOfThisApp;

    /**
     * Constructor of GodLoop
     * Setups requirements!
     * @param server server of game
     * @param adminToken Token of admin
     * @throws Exception if failed to construct a safe GodLoop!
     */
    public GodLoop(Server server, String adminToken) throws Exception {
        if(adminToken == null || server == null)
            throw new Exception("Null input");
        this.server = server;
        this.adminToken = adminToken;
        this.connectionBox = server.getConnectionBox();
        currentConnections = new ArrayList<ClientHandler>();
        stateUpdater = new StateUpdater();
        playerCharacters = null;
        currentState = State.Initial;
        gameStarted = false;
        threadName = "GodLoop";
    }

    /**
     * Updated receiver list with new connections.
     */
    public void updateConnections(){
        if(gameStarted)
            return;
        ArrayList<ClientHandler> newConnectionList;
        try {
            newConnectionList = (ArrayList<ClientHandler>) connectionBox.get();
        }catch (Exception e){
            newConnectionList = null;
        }
        if(newConnectionList == null)
            return;
        currentConnections = newConnectionList;
    }

    /**
     * Updates the part handler for the current sate of game
     * @param state current state of game
     */
    public void updatePartHandler(State state){

        // If state is not change it does nothing
        if(state == currentState)
            return;

        // Shutting down current part
        if(currentPart != null){
            currentPart.shutdown();
        }

        // Setting new state
        currentState = state;

        // Builds lobby for player
        if(state == State.Lobby){
            try {
                gameStarted = false;
                Lobby newLobby = new Lobby(adminToken, stateUpdater, server);
                newLobby.setLock(false);
                newLobby.start();
                newLobby.updateClientHandlers(currentConnections);
                currentPart = newLobby;
            } catch (Exception ignored) {}
        }

        // Calls the starter! (Will be done once) and set characters
        else if(state == State.STARTED){
            GameStarter starter = null;
            while (playerCharacters == null){
                try {
                    gameStarted = true;
                    server.endAccepting();
                    starter = new GameStarter(currentConnections);
                    starter.setCharacter();
                    playerCharacters = starter.getGameCharacters();
                } catch (Exception e) {
                    playerCharacters = null;
                }
            }
            gameStarterOfThisApp = starter;
            GameState cgs = new GameState(State.Lobby, null);
            Message message = null;
            DataBox dataBox = null;
            for(ClientHandler ch : currentConnections){
                if(playerCharacters.get(ch).getGroup() == Group.Mafia){
                    message = new Message(null, Color.BLUE_BOLD + "GOD",
                            Color.PURPLE_BOLD + "You are " +
                                    Color.RED_BOLD + playerCharacters.get(ch).toString());
                }
                else {
                    if(playerCharacters.get(ch).getCharacterName() == CharacterName.Mayer){
                        mayerToken = ch.getToken();
                    }
                    message = new Message(null, Color.BLUE_BOLD + "GOD",
                            Color.PURPLE_BOLD + "You are " +
                                    Color.GREEN_BOLD + playerCharacters.get(ch).toString());
                }
                dataBox = new DataBox(cgs, message);
                ch.send(dataBox);
            }
            if(starter != null) // Just in case! :)
                starter.applyLogic();

            // First day of game! It has no voting
            try {
                Day firstDay = new Day(adminToken, stateUpdater, false);
                firstDay.updateClientHandlers(currentConnections);
                firstDay.start();
                firstDay.setLock(true);
                currentPart = firstDay;
            }catch (Exception e){
                Logger.error("Failed to build first day chat room " + e.getMessage(),
                        LogLevel.GameInterrupted, "GodLoop");
            }

            // Holding here for 30 sec!
            try {
                Thread.sleep(30000);
            }catch (InterruptedException ignored) {}
            stateUpdater.advance();
        }

        // Building a chatroom for day
        else if(state == State.Day){
            try {
                Day day = new Day(adminToken, stateUpdater, true);
                day.updateClientHandlers(currentConnections);
                day.start();
                day.setLock(true);
                currentPart = day;
            }catch (Exception e){
                Logger.error("Failed to build first day chat room " + e.getMessage(),
                        LogLevel.GameInterrupted, "GodLoop");
            }
        }

        // Building a voter for players in a day
        else if (state == State.Vote){
            Logger.log("Proceeding to vote", LogLevel.Report, "GodLoop");
            try {
                Vote votingHandler = new Vote(stateUpdater, mayerToken);
                votingHandler.updateClientHandlers(currentConnections);
                votingHandler.setLock(true);
                votingHandler.resetGhostClients();
                votingHandler.initial();
                stateUpdater.advance();
                votingHandler.start();
                currentPart = votingHandler;
            } catch (Exception e) {
                Logger.error("Failed to build voting process!" + e.getMessage(),
                        LogLevel.GameInterrupted, "GodLoop");
            }
        }

        // Building requirement for players action + chat room for mafia!
        else if(state == State.Night){
            Logger.log("Proceeding to night", LogLevel.Report, "GodLoop");
            try {
                Night night = new Night(gameStarterOfThisApp.getCitizenTeam(),
                        gameStarterOfThisApp.getMafiaTeam(), stateUpdater);
                night.updateClientHandlers(currentConnections);
                night.initial();
                night.setLock(true);
                night.start();
                currentPart = night;
            } catch (Exception e) {
                Logger.error("Failed to build night process!" + e.getMessage(),
                        LogLevel.GameInterrupted, "GodLoop");
            }
        }

        // Building requirement for players action + chat room for mafia!
        else if(state == State.FINISHED){
            if(currentPart != null)
                currentPart.shutdown();
            this.shutdown();
        }
    }

    /**
     * This is the game loop of server!
     * Called god of game.
     */
    @Override
    public void run() {
        stateUpdater.start();
        while (!finished) {
            try {
                updateConnections();
                updatePartHandler(stateUpdater.getCurrentState());
                currentPart.updateClientHandlers(currentConnections);
            }catch (Exception e){
                Logger.error("Failed to complete god loop cycle" + e.getMessage(),
                        LogLevel.ThreadWarning, "GodLoop");
            }
        }
    }

    /**
     * Shutdown this thread!
     */
    @Override
    public void shutdown(){
        finished = true;
        stateUpdater.shutdown();
        if(currentPart != null)
            currentPart.shutdown();
        server.shutdown();
        this.close();
    }

}
