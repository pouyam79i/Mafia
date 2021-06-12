package ir.pm.mafia.model.game.handlers;

import ir.pm.mafia.controller.data.DataBase;
import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.model.utils.multithreading.Runnable;
import ir.pm.mafia.view.console.Color;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is the structure for all handles!
 * These classes are used in god loop (server loop)
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.4.3
 */
public abstract class PartHandler extends Runnable {

    /**
     * Client Handler will read this data base and send 'Data'
     */
    protected DataBase sharedSendingDataBase;
    /**
     * Part handler will read this data base and and it will analyze
     * received 'DataBox'
     */
    protected final DataBase inputDataBase;
    /**
     * List of client handlers in this part
     */
    protected ArrayList<ClientHandler> clientHandlers;
    /**
     * List of sender handler!
     */
    protected ArrayList<SenderHandler> senderHandlers;
    /**
     * Receiver Handler list.
     * These are used to read data from clients,
     * then these data will be saved in data base and part handler,
     * will analyze that.
     */
    protected final ArrayList<ReceiverHandler> receiverHandlers;
    /**
     * contains game state for clients!
     */
    protected GameState gameState;
    /**
     * Last read is used to read from input data base;
     */
    protected int lastRead;
    /**
     * If locked it wont refresh the client list!
     */
    protected boolean locked;
    /**
     * Contains my current state
     */
    protected State myState;

    /**
     * Constructor of PartHandler,
     * which handles game logic!
     * Setups basic requirements!
     */
    protected PartHandler(){
        inputDataBase = new DataBase();
        sharedSendingDataBase = new DataBase();
        clientHandlers = new ArrayList<ClientHandler>();
        senderHandlers = new ArrayList<SenderHandler>();
        receiverHandlers = new ArrayList<ReceiverHandler>();
        gameState = null;
        lastRead = 0;
        locked = false;
    }

    /**
     * Updates connections (ClientHandlers)
     * @param newClientHandlerList new client handler list
     */
    public synchronized void updateClientHandlers(ArrayList<ClientHandler> newClientHandlerList){
        if (locked)
            return;
        if(newClientHandlerList == null)
            return;
        if(finished)
            return;
        clientHandlers = newClientHandlerList;
        Iterator<ClientHandler> ch = clientHandlers.iterator();
        while (ch.hasNext()){
            ClientHandler newCH = ch.next();
            boolean exists = false;
            for(SenderHandler sh : senderHandlers){
                if(sh.getClientHandler() == newCH){
                    exists = true;
                    break;
                }
            }
            if(!newCH.isConnected()){
                ch.remove();
            }
            if(!exists){
                try {
                    SenderHandler newSH = new SenderHandler(newCH, sharedSendingDataBase);
                    ReceiverHandler newRH = new ReceiverHandler(newCH, inputDataBase);
                    newRH.start();
                    newSH.start();
                    if(myState == State.Lobby){
                        // Notifying other players is any body has joined the server!
                        Message serverRespond = new Message(newCH.getToken(),
                                Color.BLUE_BOLD + "SERVER"
                                , Color.GREEN + newCH.getNickname() + " Joined!");
                        GameState gameState = new GameState(myState, null);
                        DataBox dataBox = new DataBox(gameState, serverRespond);
                        sharedSendingDataBase.add(dataBox);
                    }
                    senderHandlers.add(newSH);
                    receiverHandlers.add(newRH);
                } catch (Exception ignored) {}
            }
        }
        refreshSRHandlersList();
    }

    /**
     * Refresh handlers, it will remove dead connections,
     * unless part handler is locker!
     */
    public synchronized void refreshSRHandlersList(){
        if(locked)
            return;
        // Removing useless ReceiverHandler
        Iterator<SenderHandler> sh = senderHandlers.iterator();
        while (sh.hasNext()){
            SenderHandler cSH = sh.next();
            if(!cSH.getClientHandler().isConnected()){
                cSH.shutdown();
                sh.remove();
            }
        }
        // Removing useless ReceiverHandler
        Iterator<ReceiverHandler> rh = receiverHandlers.iterator();
        while (rh.hasNext()){
            ReceiverHandler cRH = rh.next();
            if(!cRH.getClientHandler().isConnected())
                cRH.shutdown();
                rh.remove();
        }
    }

    /**
     * Shut downs the part handler!
     */
    @Override
    public void shutdown(){
        finished = true;
        for(SenderHandler sh : senderHandlers){
            sh.shutdown();
        }
        for(ReceiverHandler rh : receiverHandlers){
            rh.shutdown();
        }
        this.close();
    }

    /**
     * Checks the inputs and apply new changes!
     * Changes are accorded to the part of game!
     */
    protected abstract void applyLogic();

    // Setters
    /**
     * Set lock is used when we dont want to update clients.
     * Used when game is started!
     * @param locked will be set
     */
    public synchronized void setLock(boolean locked){
        this.locked = locked;
    }

}
