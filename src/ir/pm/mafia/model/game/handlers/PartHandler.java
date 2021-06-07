package ir.pm.mafia.model.game.handlers;

import ir.pm.mafia.controller.data.DataBase;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.model.loops.godloop.ReceiverHandler;
import ir.pm.mafia.model.loops.godloop.SenderHandler;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is the structure for all handles!
 * These classes are used in god loop (server loop)
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public abstract class PartHandler extends Runnable {

    /**
     * Client Handler will read this data base and send data
     */
    protected DataBase sharedSendingDataBase;
    /**
     * Part handler will read this data base and and it will analyze them
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
    private ArrayList<ReceiverHandler> receiverHandlers;
    /**
     * If locked it wont refresh the client list!
     */
    protected boolean locked;


//    public PartHandler(DataBase inputDataBase) throws Exception {
//        if(inputDataBase == null)
//            throw new Exception("Null input!");
//        this.inputDataBase = inputDataBase;
//        clientHandlers = new ArrayList<ClientHandler>();
//        senderHandlers = new ArrayList<SenderHandler>();
//        locked = false;
//    }

    public PartHandler(){
        inputDataBase = new DataBase();
        sharedSendingDataBase = inputDataBase;
        clientHandlers = new ArrayList<ClientHandler>();
        senderHandlers = new ArrayList<SenderHandler>();
        receiverHandlers = new ArrayList<ReceiverHandler>();
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
        clientHandlers = newClientHandlerList;
        int index = 0;
        for(ClientHandler newCH : newClientHandlerList){
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
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Refresh sender list will remove dead connections,
     * unless part handler is locker!
     */
    public synchronized void refreshSenderList(){
        if(locked)
            return;
        senderHandlers.removeIf(senderHandler -> !senderHandler.isRunning());
        receiverHandlers.removeIf(receiverHandler -> !receiverHandler.isRunning());
    }

    /**
     * Set lock is used when we dont want to update clients.
     * Used when game is started!
     * @param locked will be set
     */
    public synchronized void setLock(boolean locked){
        this.locked = locked;
    }

}
