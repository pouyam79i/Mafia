package ir.pm.mafia.controller.client;

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

/**
 * This class builds a connection from client to server
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.2
 */
public class Client extends Runnable {

    /**
     * network socket
     */
    private final Socket socket;
    /**
     * sender handles sending process
     */
    private final Send sender;
    /**
     * receiver handles receiving process
     */
    private final Receive receiver;

    /**
     * Client constructor.
     * Builds a safe connection to server.
     * Sets requirements.
     * @param ip IP of server
     * @param port port of server
     * @param sendBox shared memory of send box
     * @param receiveBox shared memory of receive box
     * @throws Exception if we have null input or failed to build a connection
     */
    public Client(String ip, int port,SharedMemory sendBox, SharedMemory receiveBox) throws Exception {
        try {
            if(ip == null)
                throw new IOException("Null ip address");
            socket = new Socket(ip, port);
            sender = new Send(sendBox, new ObjectOutputStream(socket.getOutputStream()));
            receiver = new Receive(receiveBox, new ObjectInputStream(socket.getInputStream()));
        }catch (Exception e){
            Logger.error("Client constructor failed!: " + e.getMessage(),
                    LogLevel.ClientFailed, "client.Client");
            throw new Exception("Failed to build a connection");
        }
    }

    /**
     * Runs the client!
     * Begins connection to server!
     */
    @Override
    public void run() {
        try {
            Thread sender = new Thread(this.sender);
            Thread receiver = new Thread(this.receiver);
            sender.start();
            receiver.start();
            while (!finished) Thread.onSpinWait();
            this.sender.close();
            this.receiver.close();
            while (!(this.sender.isDone()));
            Thread.sleep(100);
            socket.close();
        } catch (IOException | InterruptedException e) {
            Logger.error("Failed to close client properly" + e.getMessage(),
                    LogLevel.ClientDisconnected,
                    "client.Client");
        }
        done = true;
    }

}
