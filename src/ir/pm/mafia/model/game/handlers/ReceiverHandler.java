package ir.pm.mafia.model.game.handlers;

import ir.pm.mafia.controller.data.DataBase;
import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.model.utils.multithreading.Runnable;

/**
 * Handles receiving data boxes from client handler in god loop (server loop).
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.2.2
 */
public class ReceiverHandler extends Runnable {

    /**
     * Share memory used to put server receiving data from client!
     * With this shared location we transfer date between client handlers and god loop (server loop).
     */
    private final DataBase inputDataBase;
    /**
     * Client handler contains connection
     */
    private final ClientHandler clientHandler;

    public ReceiverHandler(ClientHandler clientHandler, DataBase inputDataBase) throws Exception {
        if(inputDataBase == null || clientHandler == null)
            throw new Exception("Null input");
        this.inputDataBase = inputDataBase;
        this.clientHandler = clientHandler;
        threadName = "ReceiverHandler";
        finished = false;
    }

    /**
     * Runs the process of reading!
     */
    @Override
    public void run() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        while (clientHandler.isConnected() && (!finished)){
            try {
                DataBox newDataBox = clientHandler.checkReceiver();
                if(newDataBox != null)
                    inputDataBase.add(newDataBox.getData());
            }catch (Exception ignored){}
        }
        this.shutdown();
    }

    /**
     * Shutdowns
     */
    @Override
    public void shutdown(){
        finished = true;
        this.close();
    }

    // Getters
    public ClientHandler getClientHandler() {
        return clientHandler;
    }

}
