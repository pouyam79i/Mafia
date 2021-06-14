package ir.pm.mafia.controller.server;

import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.util.*;

/**
 * This class handles the clientHandlers,
 * it is kind of a list of clientHandlers :)
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.2
 */
public class ClientContainer extends Runnable {

    /**
     * Contains the connections, mapped with tokens
     */
    private final HashMap<String, ClientHandler> clientHandlers;
    /**
     * List of tokens maintains order
     */
    private final ArrayList<String> tokens;
    /**
     * Players nickname.
     * It must be unique.
     */
    private final ArrayList<String> nicknames;
    /**
     * It this shared memory I put new connection box.
     * Which contains a list of client handler.
     * used to update server loop (god loop) of game!
     */
    private final SharedMemory connectionBox;
    /**
     * Number of current connections
     */
    private int numberOfConnections;
    /**
     * if locked you cannot remove any connection.
     * even if they got disconnected.
     * It will be true when the game is started!
     */
    private volatile boolean locked;

    /**
     * Constructor of ClientContainer
     * Setups requirements
     * @param connectionBox used to share connections between threads!
     * @throws Exception if null input
     */
    public ClientContainer(SharedMemory connectionBox) throws Exception {
        if(connectionBox == null)
            throw new Exception("Null input");
        this.connectionBox = connectionBox;
        clientHandlers = new HashMap<String, ClientHandler>();
        tokens = new ArrayList<String>();
        nicknames = new ArrayList<String>();
        locked = false;
        threadName = "ClientContainer";
    }

    /**
     * Adds a new client handler to the list of client
     * @param newConnection will be added
     */
    public void add(ClientHandler newConnection){
        if(locked){
            if(newConnection != null)
                newConnection.shutdown();
            return;
        }
        if(newConnection == null)
            return;
        if(!(tokens.contains(newConnection.getToken()) || nicknames.contains(newConnection.getNickname()))){
            newConnection.start();
            tokens.add(newConnection.getToken());
            nicknames.add(newConnection.getNickname());
            clientHandlers.put(newConnection.getToken(), newConnection);
            shareNewConnectionBox();
        }
        else {
            newConnection.shutdown();
        }
    }

    /**
     * Lock the list
     * no client handler will be removed!
     */
    public void lock(){
        locked = true;
        shareNewConnectionBox();
    }

    /**
     * Closes all connections
     */
    public void closeAll(){
        Iterator<String> chTk = tokens.iterator();
        while (chTk.hasNext()){
            String token = chTk.next();
            ClientHandler clientHandler = clientHandlers.get(token);
            clientHandler.shutdown();
            clientHandlers.remove(token);
            chTk.remove();
        }
        shareNewConnectionBox();
    }

    /**
     * update the connections state,
     * if not locked it will remove disconnected connections
     */
    private void updateConnections(){
        if(locked){
            return;
        }
        try {
            ArrayList<String> tokens = new ArrayList<String>(this.tokens);
            Iterator<String> chTk = tokens.iterator();
            boolean updated = false;
            while (chTk.hasNext()){
                String token = chTk.next();
                ClientHandler clientHandler = clientHandlers.get(token);
                if((!clientHandler.isConnected())){
                    Logger.log("Client is disconnected so it will be removed",
                            LogLevel.ClientDisconnected,
                            "ClientContainer");
                    clientHandler.shutdown();
                    nicknames.remove(clientHandler.getNickname());
                    clientHandlers.remove(token);
                    this.tokens.remove(token);
                    updated = true;
                }
            }
            if(updated)
                shareNewConnectionBox();
        }catch (Exception ignored){}
    }

    /**
     * puts all the current connections in the connection box.
     * send ArrayList<ClientHandler>
     */
    private void shareNewConnectionBox(){
        ArrayList<ClientHandler> newConnections = new ArrayList<ClientHandler>();
        for (String str : tokens){
            newConnections.add(clientHandlers.get(str));
        }
        connectionBox.put(newConnections);
    }

    @Override
    public void run() {
        while ((!locked) && (!finished))
            updateConnections();
        this.shutdown();
    }

    // Getters
    public int getNumberOfConnections() {
        try {
            updateConnections();
            return tokens.size();
        } catch (Exception ignored){
         return -1;
        }
    }


}
