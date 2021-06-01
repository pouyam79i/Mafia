package ir.pm.mafia.controller.server;

import ir.pm.mafia.controller.communication.Receive;
import ir.pm.mafia.controller.communication.Send;
import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

/**
 * This class handles the connection between server and client.
 * With this class we can build multi thread server!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1
 */
public class ClientHandler extends Runnable {

    /**
     * socket contains the connection information
     */
    private final Socket socket;
    /**
     * Token is a unique Value assigned to each client
     */
    private final String token;
    /**
     * hands sending process
     */
    private final Send sender;
    /**
     * handles receiving process
     */
    private final Receive receiver;
    /**
     * Tells the state of interruption
     * if interruption happens it will be true, else remains false!
     */
    private boolean clientHandlerInterrupted;

    /**
     * Constructor of ClientHandler
     * Setup requirements fields and data
     * @param socket contains the connection information
     * @param sendBox shared memory used to handle sending process
     * @param receiveBox shared memory used to handle receiving process
     * @throws IOException if failed in any way,
     */
    public ClientHandler(Socket socket, SharedMemory sendBox, SharedMemory receiveBox) throws IOException {
        try {
            if(socket == null || sendBox == null || receiveBox == null)
                throw new IOException("Building clients failed because of null input");
            sender = new Send(sendBox, new ObjectOutputStream(socket.getOutputStream()));
            receiver = new Receive(receiveBox, new ObjectInputStream(socket.getInputStream()));
            this.socket = socket;
        }catch (Exception e){
            Logger.error("Constructing new client handler failed: " + e.getMessage(),
                    LogLevel.ServerFailed,
                    "server.ClientHandler");
            throw new IOException("Constructing new client handler failed");
        }
        clientHandlerInterrupted = false;
        token = UUID.randomUUID().toString();
    }

    /**
     * Runs the connection to the client
     */
    @Override
    public void run() {
        try {
            Thread sender = new Thread(this.sender);
            Thread receiver = new Thread(this.receiver);
            sender.start();
            receiver.start();
            while (!finished);
            this.sender.close();
            this.receiver.close();
            // Waiting about 0.1 second to make sure interruption does not happen
            Thread.sleep(100);
            socket.close();
        } catch (IOException | InterruptedException e) {
            Logger.error("ClientHandler Failed while running: " + e.getMessage(),
                    LogLevel.ServerFailed,
                    "server.ClientHandler");
            clientHandlerInterrupted = true;
        }
        done = true;
    }

    //Getter
    public synchronized String getToken() {
        return token;
    }
    public synchronized boolean getClientHandlerConnectionState(){
        return socket.isConnected() && (!clientHandlerInterrupted);
    }

}
