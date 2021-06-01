package ir.pm.mafia.controller.server;

import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server of game builds connection to clients and handles them.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class Server {

    /**
     * Welcoming server socket
     */
    private final ServerSocket serverSocket;
    /**
     * Executor service
     */
    private final ExecutorService executorService;
    /**
     * List of connected clients
     */
    private ArrayList<ClientHandler> clientHandlers;
    /**
     * number of connected clients
     */
    private int numberOfConnections;
    /**
     * state of server service
     * if true it means server is on!
     * else it means server is off!
     */
    private boolean state;

    /**
     * Server Constructor
     * Builds server requirements
     * @param port port of server
     * @throws Exception throws exception if it failed to build a safe server!
     */
    public Server(int port) throws Exception {
        try {
            serverSocket = new ServerSocket(port);
            executorService = Executors.newCachedThreadPool();
        }catch (IOException e){
            Logger.error("Field while constructing server" + e.getMessage(),
                    LogLevel.ServerFailed,
                    "Server's Constructor");
            throw new Exception("Field while constructing server");
        }
        state = true;
        numberOfConnections = 0;
    }

    /**
     * updates the number of connected clients
     */
    private synchronized void updateNumberOfConnections(){
        if(clientHandlers == null){
            numberOfConnections = 0;
            return;
        }
        Iterator<ClientHandler> chIt = clientHandlers.iterator();
        while (chIt.hasNext()){
            ClientHandler clientHandler = chIt.next();
            if((!clientHandler.getClientHandlerConnectionState()) || (clientHandler.isDone())){
                Logger.log("Client is disconnected so it will be removed",
                        LogLevel.ClientDisconnected,
                        "server.Server");
                chIt.remove();
            }
        }
        numberOfConnections = clientHandlers.size();
    }

    /**
     * This method waits for a client to join server
     * @param newSendBox shared memory used as send box of new connected client
     * @param newReceiveBox shared memory used as receive box of new connected client
     * @return true if the client connected successfully, else false!
     * @throws Exception if server is off and you try to call this method or because of null input!
     */
    public synchronized boolean waitForNewClient(SharedMemory newSendBox,
                                                 SharedMemory newReceiveBox) throws Exception{
        if(!state)
            throw new Exception("Server is shutdown");
        if(newSendBox == null || newReceiveBox == null)
            throw new Exception("Null input");
        ClientHandler newClient = null;
        try {
            Socket newConnectionSocket = serverSocket.accept();
            newClient = new ClientHandler(newConnectionSocket , newSendBox, newReceiveBox);
            Thread.sleep(10);
            executorService.execute(newClient);
            clientHandlers.add(newClient);
            updateNumberOfConnections();
            Logger.log("New Client Connected!", LogLevel.Report, "server.Server");
            return true;
        } catch (IOException | InterruptedException e) {
            Logger.error("Failed to join new client" + e.getMessage(),
                    LogLevel.ServerFailed,
                    "server.Server");
            if(newClient != null)
                newClient.close();
            return false;
        }
    }

    /**
     * shutdown server service
     */
    public synchronized void shutdown(){
        state = false;
        for(ClientHandler clientHandler : clientHandlers){
            clientHandler.close();
        }
        clientHandlers = null;
        executorService.shutdown();
        Logger.log("Server shutdown called!",
                LogLevel.ShutdownCall,
                "server.Server");
    }

    /**
     * Returns number of connections.
     * If server is off and this method is call it returns -1
     * @return current number of connections.
     */
    public synchronized int getNumberOfConnections() {
        if(!state)
            return -1;
        updateNumberOfConnections();
        return numberOfConnections;
    }

}
