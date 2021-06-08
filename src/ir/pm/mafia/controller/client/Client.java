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
 * @version 1.5
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
     * Client token
     * This token will be null for admin,
     * We set Amin token from another place!
     */
    private String myToken = null;
    /**
     * Contains client nick name
     */
    private String nickname = null;

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
    public Client(String ip, int port,
                  SharedMemory sendBox,
                  SharedMemory receiveBox,
                  String myToken, String nickname) throws Exception {
        try {
            if(ip == null || nickname == null)
                throw new IOException("Null ip address");
            socket = new Socket(ip, port);

            // Hand shake process for token!
            this.myToken = myToken;
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            if(this.myToken != null){
                outputStream.writeUTF(this.myToken);
                outputStream.flush();
                String useless = inputStream.readUTF();
            }
            else{
                outputStream.writeUTF("empty");
                outputStream.flush();
                this.myToken = inputStream.readUTF();
            }
            // Hand shake process for nickname!
            this.nickname = nickname;
            outputStream.writeUTF(nickname);
            outputStream.flush();

            // Memory Allocation
            sender = new Send(sendBox, outputStream);
            receiver = new Receive(receiveBox, inputStream);
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
            sender.start();
            receiver.start();
            while (!finished) Thread.onSpinWait();
        } catch (Exception e) {
            Logger.error("Failed to close client properly" + e.getMessage(),
                    LogLevel.ClientDisconnected,
                    "Client");
        }
    }

    /**
     * shutdown
     */
    @Override
    public void shutdown(){
        this.sender.shutdown();
        this.receiver.shutdown();
        try {
            socket.close();
        } catch (IOException e) {}
        this.close();
    }

    // Getters
    public String getMyToken() {
        return myToken;
    }

}
