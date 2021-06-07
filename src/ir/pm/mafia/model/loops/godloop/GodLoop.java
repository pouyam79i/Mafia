package ir.pm.mafia.model.loops.godloop;

//import ir.pm.mafia.controller.data.Data;
//import ir.pm.mafia.controller.data.DataBase;
import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.model.game.handlers.Lobby;
import ir.pm.mafia.model.game.handlers.PartHandler;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.model.game.state.StateUpdater;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.util.ArrayList;
//import java.util.Iterator;

/**
 * This Class is the server main loop so it is called GodLoop!
 * Handled states and part handler!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
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
    private StateUpdater stateUpdater;
    /**
     * Current connection list.
     * Contains connected client handlers!
     */
    private ArrayList<ClientHandler> currentConnections;
//    /**
//     * Receiver Handler list.
//     * These are used to read data from clients,
//     * then these data will be saved in data base and part handler,
//     * will analyze that.
//     */
//    private ArrayList<ReceiverHandler> receiverHandlers;
    /**
     * Current part handler,
     * contains a logic analyzer for current state of game!
     */
    private PartHandler currentPart;
//    /**
//     * This will be given to part handlers!
//     * received data will be analyzed, and gathered
//     * in this data base!
//     */
//    private DataBase currentDataBase;
    /**
     * Game stated state!
     * if true it means game has started!
     */
    private boolean gameStarted;
    /**
     * Token of admin will be used to confirm admin commands!
     */
    private final String adminToken;
//    /**
//     * Input box is where add of received data boxes
//     * have been gathered.
//     * It has the SharedMemory structure.
//     */
//    private final SharedMemory inputBox;
    /**
     * In this shared memory we pull new list of client handlers.
     */
    private final SharedMemory connectionBox;

    /**
     * Constructor of GodLoop
     * Setups requirements!
     * @param connectionBox In this shared memory we pull new list of client handlers.
     * @param adminToken Token of admin
     * @throws Exception if failed to construct a safe GodLoop!
     */
    public GodLoop(SharedMemory connectionBox, String adminToken) throws Exception {
        if(connectionBox == null || adminToken == null)
            throw new Exception("Null input");
        this.connectionBox = connectionBox;
        this.adminToken = adminToken;
        currentConnections = new ArrayList<ClientHandler>();
//        receiverHandlers = new ArrayList<ReceiverHandler>();
//        inputBox = new SharedMemory(true);
//        currentDataBase = new DataBase();
        stateUpdater = new StateUpdater();
        currentState = State.Initial;
        gameStarted = false;
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
//        if(newConnectionList != null){
//            currentConnections = newConnectionList;
//            for(ClientHandler clientHandler : currentConnections){
//                boolean exists = false;
//                // Debug this shit #########################################################################
//                for(ReceiverHandler receiverHandler : receiverHandlers){
//                    if (receiverHandler.getClientHandler() == clientHandler) {
//                        exists = true;
//                        break;
//                    }
//                }
//                if(!exists){
//                    try {
//                        ReceiverHandler newReceiverHandler = new ReceiverHandler(clientHandler, inputBox);
//                        newReceiverHandler.start();
//                        receiverHandlers.add(newReceiverHandler);
//                    } catch (Exception ignored) {}
//                }
//            }
//        }
//        refreshReceiverHandlerList();
    }

//    /**
//     * It removes disconnected or finished receivers!
//     */
//    public void refreshReceiverHandlerList(){
//        if(gameStarted)
//            return;
//        Iterator<ReceiverHandler> rh = receiverHandlers.iterator();
//        while(rh.hasNext()){
//            ReceiverHandler cRH = rh.next();
//            if(!currentConnections.contains(cRH.getClientHandler()))
//                cRH.shutdown();
//                rh.remove();
//        }
//    }

    /**
     * When admin calls to start the game!
     * It will lock every thing :)
     */
    public void startTheGame(){
//        updateConnections();
        gameStarted = true;
    }

//    /**
//     * adds new data to data base!
//     * if game is not started, it will check admins commands
//     */
//    public void updateDateBase(){
//        while (true){
//            Data newData = (Data) inputBox.get();
//            if(newData == null)
//                break;
//            currentDataBase.add(newData);
//        }
//    }

    /**
     * Updates the part handler for the current sate of game
     * @param state current state of game
     */
    public void updatePartHandler(State state){

        // If state is not change it does nothing
        if(state == currentState)
            return;
//        // Building new data base for new part!
//        currentDataBase = new DataBase();

        // Shutting down current part
        if(currentPart != null)
            currentPart.shutdown();

        // Setting new state
        currentState = state;

        // Builds lobby for player
        if(state == State.Lobby){
            try {
                gameStarted = false;
//                Lobby newLobby = new Lobby(currentDataBase);
                Lobby newLobby = new Lobby();
                newLobby.setLock(gameStarted);
                newLobby.updateClientHandlers(currentConnections);
                newLobby.start();
                currentPart = newLobby;
            } catch (Exception ignored) {}
        }

        // Building a chatroom for day
        else if(state == State.Day){

        }

        // Building a voter for players in a day
        else if (state == State.Vote){

        }

        // Building requirement for players action + chat room for mafia!
        else if(state == State.Night){

        }

    }

    /**
     * This is the game loop of server!
     * Called god of game
     */
    @Override
    public void run() {
        stateUpdater.start();
        while (!finished) {
            updateConnections();
            updatePartHandler(stateUpdater.getCurrentState());
            currentPart.updateClientHandlers(currentConnections);
//            updateDateBase();
        }
    }

    /**
     * Shutdown this thread!
     */
    @Override
    public void shutdown(){
        if(stateUpdater != null)
            stateUpdater.shutdown();
        if(currentPart != null)
            currentPart.shutdown();
        this.close();
    }

}
