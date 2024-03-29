package ir.pm.mafia.controller.server;

import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.player.Player;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

/**
 * Server of game builds connection to clients and handles them.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.4.7
 */
public class Server extends Runnable{

    /**
     * Welcoming server socket
     */
    private final ServerSocket serverSocket;
    /**
     * List of connected clients
     */
    private ClientContainer clientContainer;
    /**
     * It this shared memory I put new connection box.
     * Which contains a list of client handler.
     * used to update server loop (god loop) of game!
     */
    private final SharedMemory connectionBox;
    /**
     * This token is generated for admin
     * Only with token you can change server setting :)
     */
    private final String adminToken;
    /**
     * port of server
     */
    private final int port;
    /**
     * this the max number of connection
     */
    private volatile int maxConnectionNumber;
    /**
     * number of connected clients
     */
    private volatile int numberOfConnections;
    /**
     * Accepting mode is true when you are in lobby!
     * If game is started! no more connection is accepted!
     */
    private volatile boolean acceptingMode;

    /**
     * Server Constructor
     * Builds server requirements
     * @param port port of server
     * @throws Exception throws exception if it failed to build a safe server!
     */
    public Server(int port) throws Exception {
        try {
            serverSocket = new ServerSocket(port);
            connectionBox = new SharedMemory(true);
            clientContainer = new ClientContainer(connectionBox);
            this.port = port;
        }catch (IOException e){
            Logger.error("Field while constructing server" + e.getMessage(),
                    LogLevel.ServerFailed,
                    "Server");
            throw new Exception("Field while constructing server");
        }finally {
            // generating token for admin
            adminToken = UUID.randomUUID().toString();
            acceptingMode = true;
            maxConnectionNumber = 10;
            numberOfConnections = 0;
            threadName = "Server";
        }
    }

    /**
     * It ends the thread!
     * so no more client accepting happens!
     * but the server is alive!
     */
    public void endAccepting(){
        acceptingMode = false;
        clientContainer.lock();
        finished = false;
    }

    /**
     * shutdown server service
     */
    @Override
    public void shutdown(){
        try {
            serverSocket.close();
            this.close();
            clientContainer.closeAll();
            clientContainer = null;
            Logger.log("Server shutdown finished!",
                    LogLevel.ShutdownCall,
                    "Server");
        } catch (IOException e) {
            Logger.error("Failed to kill server socket" + e.getMessage(),
                    LogLevel.ServerFailed, "Server");
        }
    }

    /**
     * Server loop
     */
    @Override
    public void run() {
        clientContainer.start();
        while ((!finished) && acceptingMode){
            while (numberOfConnections < maxConnectionNumber) {
                try {
                    waitForNewClient();
                } catch (Exception e) {
                    Logger.error("Joining new client failed: " + e.getMessage(),
                            LogLevel.ServerFailed,
                            "Server");
                }
            }
        }
    }

    /**
     * updates the number of connected clients
     */
    private void updateNumberOfConnections(){
        try {
            numberOfConnections = clientContainer.getNumberOfConnections();
        }catch (Exception ignored){}
    }

    /**
     * This method waits for a client to join server
     * @throws Exception if server is off and you try to call this method or because of null input!
     */
    private void waitForNewClient() throws Exception{
        if(finished)
            throw new Exception("Server is shutdown");
        ClientHandler newClient = null;
        try {
            Socket newConnectionSocket = serverSocket.accept();
            newClient = new ClientHandler(newConnectionSocket);
            clientContainer.add(newClient);
            updateNumberOfConnections();
            Logger.log("New Client Connected!", LogLevel.Report, "Server");
        } catch (IOException | InterruptedException e) {
            Logger.error("Failed to join new client" + e.getMessage(),
                    LogLevel.ServerFailed,
                    "Server");
            if(newClient != null)
                newClient.shutdown();
        }
    }

    // Setters
    /**
     * Assigns a player with admin token!
     * @param player player will be admin!
     * @throws Exception if null player is given, which is going to fail be cause then we have no admin :(
     */
    public void setAdmin(Player player) throws Exception {
        if(player == null)
            throw new Exception("Null input");
        player.setToken(adminToken);
    }
    /**
     * Sets max number of connection.
     * It must be between 6 to 10.
     * @param maxConnectionNumber will be set as max number of connection
     * @return true if it could set this value!
     */
    public boolean setMaxConnectionNumber(int maxConnectionNumber) {
        if(maxConnectionNumber > 10 || maxConnectionNumber < 6)
            return false;
        if(maxConnectionNumber < numberOfConnections)
            return false;
        this.maxConnectionNumber = maxConnectionNumber;
        return true;
    }

    // Getters
    /**
     * Returns number of connections.
     * If server is off and this method is call it returns -1
     * @return current number of connections.
     */
    public int getNumberOfConnections() {
        try {
            if(finished)
                return -1;
            updateNumberOfConnections();
            return numberOfConnections;
        }catch (Exception ignored){
            return -2;
        }
    }
    public int getMaxConnectionNumber() {
        return maxConnectionNumber;
    }
    public int getPort() {
        return port;
    }
    public ClientContainer getClientContainer() {
        return clientContainer;
    }
    public SharedMemory getConnectionBox() {
        return connectionBox;
    }

}
