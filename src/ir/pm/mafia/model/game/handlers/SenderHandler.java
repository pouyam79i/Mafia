package ir.pm.mafia.model.game.handlers;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.DataBase;
import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.model.utils.multithreading.Runnable;

/**
 * This Class handles sending data from server to client
 * while client must read same data box!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.2.2
 */
public class SenderHandler extends Runnable {

    /**
     * contains a connection
     */
    private final ClientHandler clientHandler;
    /**
     * is the token of this connection!
     */
    private final String clientToken;
    /**
     * shared data base! client must read its data!
     */
    private final DataBase sharedSendBox;
    /**
     * last read index of data base
     */
    private int lastRead;

    /**
     * Setup a sending connection to client!
     * @param clientHandler contains connection property
     * @param sharedSendBox is the shared data base! client reads this
     * @throws Exception if failed to build a proper SenderHandler!
     */
    public SenderHandler(ClientHandler clientHandler, DataBase sharedSendBox) throws Exception {
        if(clientHandler == null || sharedSendBox == null)
            throw new Exception("Null input");
        this.clientHandler = clientHandler;
        this.sharedSendBox = sharedSendBox;
        clientToken = clientHandler.getToken();
        lastRead = 0;
    }

    /**
     * Runs a Sender handler!
     */
    @Override
    public void run() {
        while (clientHandler.isConnected()){
            while (lastRead < sharedSendBox.getSize()){
                DataBox newDataBox = (DataBox) sharedSendBox.readData(lastRead);
                lastRead++;
                if(newDataBox == null)
                    continue;
                Data data = newDataBox.getData();
                if(data == null && newDataBox.getGameState() != null){
                    clientHandler.send(newDataBox);
                }
                else if((!clientToken.equals(data.getSenderToken()) && data.getReceiverToken().equals("EMPTY")) ||
                        clientToken.equals(data.getReceiverToken())){
                    clientHandler.send(newDataBox);
                }
            }
        }
        this.shutdown();
    }

    //Getters
    public ClientHandler getClientHandler() {
        return clientHandler;
    }

}
