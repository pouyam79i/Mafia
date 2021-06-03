package ir.pm.mafia.controller.client;

import ir.pm.mafia.controller.communication.Receive;
import ir.pm.mafia.controller.communication.Send;
import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.io.*;
import java.net.Socket;

/**
 * This class builds a connection from client to server
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.3
 */
public class Client extends Runnable {

    /**
     * network socket
     */
    private Socket socket;
    /**
     * sender handles sending process
     */
    private Send sender;
    /**
     * receiver handles receiving process
     */
    private Receive receiver;
    /**
     * Client token
     * This token will be null for admin,
     * We set Amin token from another place!
     */
    private String myToken = null;

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

            // Hand shake process
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            if(myToken == null){
                outputStream.writeUTF("empty");
                myToken = inputStream.readUTF();
            }
            else
                outputStream.writeUTF(myToken);
            outputStream.flush();

            // Memory Allocation
            sender = new Send(sendBox, new ObjectOutputStream(socket.getOutputStream()));
            receiver = new Receive(receiveBox, new ObjectInputStream(socket.getInputStream()));
        }catch (Exception e){
            Logger.error("Client constructor failed!: " + e.getMessage(),
                    LogLevel.ClientFailed, "client.Client");
            throw new Exception("Failed to build a connection");
        }
    }

    /**
     * Client constructor.
     * Builds a safe connection to server.
     * Sets requirements.
     * Note that this constructor is used in admin mode :)
     * @param ip IP of server
     * @param port port of server
     * @param sendBox shared memory of send box
     * @param receiveBox shared memory of receive box
     * @throws Exception if we have null input or failed to build a connection
     */
    public Client(String ip, int port,SharedMemory sendBox,
                  SharedMemory receiveBox, String myToken) throws Exception {
        this.myToken = myToken;
        new Client(ip, port, sendBox, receiveBox);
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
            this.sender.shutdown();
            this.receiver.shutdown();
            socket.close();
        } catch (IOException e) {
            Logger.error("Failed to close client properly" + e.getMessage(),
                    LogLevel.ClientDisconnected,
                    "client.Client");
        }
    }

    // Getters
    public String getMyToken() {
        return myToken;
    }

}
