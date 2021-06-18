package ir.pm.mafia.model.game.handlers;

import ir.pm.mafia.model.utils.memory.DataBase;
import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;
import ir.pm.mafia.view.console.Color;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is the structure for all handles!
 * These classes are used in god loop (server loop)
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.6
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
    protected DataBase inputDataBase;
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
    protected ArrayList<ReceiverHandler> receiverHandlers;
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
        myState = State.Initial;
        gameState = null;
        lastRead = 0;
        locked = false;
        finished = false;
        threadName = "PartHandler";
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
            if(!newCH.isConnected()){
                ch.remove();
                continue;
            }
            boolean exists = false;
            for(SenderHandler sh : senderHandlers){
                if(sh.getClientHandler() == newCH){
                    exists = true;
                    break;
                }
            }
            if(!exists){
                try {
                    SenderHandler newSH = new SenderHandler(newCH, sharedSendingDataBase);
                    ReceiverHandler newRH = new ReceiverHandler(newCH, inputDataBase);
                    newRH.start();
                    newSH.start();
                    senderHandlers.add(newSH);
                    receiverHandlers.add(newRH);
                    if(myState == State.Lobby){
                        // Notifying other players is any body has joined the server!
                        Message serverRespond = new Message(newCH.getToken(),
                                Color.BLUE_BOLD + "SERVER"
                                , Color.GREEN + newCH.getNickname() + " Joined!");
                        GameState gameState = new GameState(myState, null);
                        DataBox dataBox = new DataBox(gameState, serverRespond);
                        sharedSendingDataBase.add(dataBox);
                    }
                } catch (Exception e) {
                    newCH.shutdown();
                    Logger.error("Failed to hock the connection to ClientHandler" + e.getMessage(),
                            LogLevel.ClientDisconnected, "PartHandler");
                }
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
            if(!cRH.getClientHandler().isConnected()){
                cRH.shutdown();
                rh.remove();
            }
        }
    }

    /**
     * This method runs the part handler!
     */
    @Override
    public void run() {
        lastRead = 0;
        // Telling game loop that we are in lobby! (Lobby hand shake)
        sharedSendingDataBase.add(new DataBox(gameState, null));
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}

        // running lobby logic
        while (!finished){
            try {
                applyLogic();
            }catch (Exception ignored){}
        }
    }

    /**
     * Shut downs the part handler!
     */
    @Override
    public void shutdown(){
        finished = true;
        for(ReceiverHandler rh : receiverHandlers){
            rh.shutdown();
        }
        for(SenderHandler sh : senderHandlers){
            sh.shutdown();
        }
        this.close();
    }

    /**
     * Checks the inputs and apply new changes!
     * Changes are accorded to the part of game!
     */
    protected abstract void applyLogic();

    /**
     * Sending a message
     * @param message will be send
     */
    protected void send(Message message){
        if(message == null)
            return;
        sharedSendingDataBase.add(new DataBox(gameState, message));
    }

    /**
     * Send a message to a specific user
     * @param messageText will be sent
     * @param userToken is the receiver token
     */
    protected void sendToUser(String messageText, String userToken){
        if(messageText == null || userToken == null)
            return;
        Message message = new Message(null, Color.BLUE_BOLD + "GOD", messageText);
        message.setReceiverToken(userToken);
        send(message);
    }

    /**
     * Send a text from server to all players
     * @param messageText is text of message
     */
    protected void sendToAll(String messageText){
        Message newMessage = new Message(null, Color.BLUE_BOLD + "GOD",
                messageText);
        send(newMessage);
    }

    /**
     * Finds a client handler by its token
     * @param token of client handler
     * @return ClientHandler, if not found returns null
     */
    protected ClientHandler getClientHandler(String token){
        if(clientHandlers == null)
            return null;
        for (ClientHandler ch : clientHandlers)
            if(ch.getToken().equals(token))
                return ch;
        return null;
    }

    /**
     * Finds a client handler by its nickname
     * @param nickname of client handler
     * @return ClientHandler, if not found returns null
     */
    protected ClientHandler getClientHandlerByName(String nickname){
        if(clientHandlers == null)
            return null;
        for (ClientHandler ch : clientHandlers)
            if(ch.getNickname().equals(nickname))
                return ch;
        return null;
    }

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
