package ir.pm.mafia.model.loops.godloop;

import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.model.utils.multithreading.Runnable;

/**
 * Handles receiving data boxes from client handler in god loop (server loop).
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1
 */
public class ReceiverHandler extends Runnable {

    /**
     * Share memory used to put server receiving data from client!
     * With this shared location we transfer date between client handlers and god loop (server loop).
     */
    private final SharedMemory inputBox;
    /**
     * Client handler contains connection
     */
    private final ClientHandler clientHandler;

    /**
     * Constructor of ReceiverHandler
     * Setups requirement to read clients!
     * @param inputBox input is where we share data for server loop!
     * @param clientHandler contains connection
     * @throws Exception if failed to build a safe ReceiverHandler
     */
    public ReceiverHandler(SharedMemory inputBox, ClientHandler clientHandler) throws Exception {
        if(inputBox == null || clientHandler == null)
            throw new Exception("Null input");
        this.inputBox = inputBox;
        this.clientHandler = clientHandler;
    }

    /**
     * Runs the process of reading!
     */
    @Override
    public void run() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        while (clientHandler.isConnected()){
            DataBox newDataBox = clientHandler.checkReceiver();
            if(newDataBox != null)
                inputBox.put(newDataBox.getData());
        }
        this.close();
    }

    // Getters
    public ClientHandler getClientHandler() {
        return clientHandler;
    }

}
