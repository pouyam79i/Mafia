package ir.pm.mafia.controller.server;

import ir.pm.mafia.controller.communication.Receive;
import ir.pm.mafia.controller.communication.Send;
import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

/**
 * This class handles the connection between server and client.
 * With this class we can build multi thread server!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.6.2
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
     * Contains client nick name
     */
    private final String nickname;
    /**
     * hands sending process
     */
    private final Send sender;
    /**
     * handles receiving process
     */
    private final Receive receiver;
    /**
     * shared memory used to handle sending process
     */
    private final SharedMemory sendBox;
    /**
     * shared memory used to handle receiving process
     */
    private final SharedMemory receiveBox;
    /**
     * Tells the state of interruption
     * if interruption happens it will be true, else remains false!
     */
    private boolean clientHandlerInterrupted;
    /**
     * Character of player in the game!
     * It will be set (only once) when game has started.
     */
    private Character character;
    /**
     * Contains current state of client handler!
     */
    private volatile ClientState clientState;

    /**
     * Constructor of ClientHandler
     * Setup requirements fields and data
     * @param socket contains the connection information
     * @throws IOException if failed in any way,
     */
    public ClientHandler(Socket socket) throws Exception {
        try {
            if(socket == null)
                throw new Exception("Building clients failed because of null socket");

            // Hand shake process!
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            String handShake = inputStream.readUTF();
            if(handShake.equals("empty")){
                token = UUID.randomUUID().toString();
                outputStream.writeUTF(token);
            }else{
                token = handShake;
                outputStream.writeUTF("token accepted");
            }
            outputStream.flush();
            // Hand shake reading nickname
            nickname = inputStream.readUTF();

            // Memory allocation
            sendBox = new SharedMemory(true);
            receiveBox= new SharedMemory(true);
            sender = new Send(sendBox, outputStream);
            receiver = new Receive(receiveBox, inputStream);
            this.socket = socket;
            clientHandlerInterrupted = false;
            character = null;
            clientState = ClientState.ALIVE;
            threadName = "ClientHandler";
        }catch (Exception e){
            Logger.error("Constructing new client handler failed: " + e.getMessage(),
                    LogLevel.ServerFailed,
                    "server.ClientHandler");
            throw new Exception("Constructing new client handler failed");
        }
    }

    /**
     * This method puts a data box in send box
     * @param dataBox will be send
     */
    public void send(DataBox dataBox){
        if(!finished)
            sendBox.put(dataBox);
    }

    /**
     * This method checks if new data has received!
     * if we have new data returns it, else returns null!
     * @return DataBox
     */
    public DataBox checkReceiver(){
        if(!finished)
            return ((DataBox) receiveBox.get());
        return null;
    }

    /**
     * Runs the connection to the client
     */
    @Override
    public void run() {
        clientHandlerInterrupted = false;
        try {
            sender.start();
            receiver.start();
            while (!finished) Thread.onSpinWait();
        } catch (Exception e) {
            Logger.error("ClientHandler Failed while running: " + e.getMessage(),
                    LogLevel.ServerFailed,
                    "server.ClientHandler");
            clientHandlerInterrupted = true;
        }
        this.shutdown();
    }

    /**
     * shutdown the thread
     */
    @Override
    public void shutdown(){
        try {
            finished = true;
            socket.close();
            sender.shutdown();
            receiver.shutdown();
        } catch (IOException ignored) {}
        this.close();
    }

    /**
     * Updates current client state
     * if disconnected, cannot be updated!
     * @param newState of client state
     */
    public void updateClientState(ClientState newState){
        if(newState == clientState)
            return;
        // If disconnected cannot return to the game!
        if(clientState == ClientState.DISCONNECTED || newState == ClientState.DISCONNECTED){
            clientState = ClientState.DISCONNECTED;
            sender.setLocked(true);
            receiver.setLocked(true);
            this.shutdown();
            return;
        }
        // If dead cannot end ghost mode!
        if(clientState == ClientState.KILLED){
            return;
        }
        clientState = newState;
        if(newState == ClientState.GHOST || newState == ClientState.KILLED){
            receiver.setLocked(true);
        }
        else if(newState == ClientState.ALIVE){
            receiver.setLocked(false);
        }
    }

    // Setters
    public synchronized void setCharacter(Character character) {
        if(this.character == null)
            this.character = character;
    }

    // Getter
    public synchronized String getToken() {
        return token;
    }
    public synchronized boolean isConnected(){
        clientHandlerInterrupted = receiver.isInterrupted();
        return socket.isConnected() && (!clientHandlerInterrupted) && (!finished);
    }
    public synchronized SharedMemory getSendBox() {
        return sendBox;
    }
    public synchronized SharedMemory getReceiveBox() {
        return receiveBox;
    }
    public synchronized String getNickname(){
        return nickname;
    }
    public synchronized Character getCharacter() {
        return character;
    }
    public ClientState getClientState() {
        return clientState;
    }

}
