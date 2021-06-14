package ir.pm.mafia.model.game.logic.lobby;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.controller.server.Server;
import ir.pm.mafia.model.game.handlers.PartHandler;
import ir.pm.mafia.model.game.handlers.SenderHandler;
import ir.pm.mafia.model.game.logic.commands.AdminCommand;
import ir.pm.mafia.model.game.logic.commands.PlayerCommand;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.model.game.state.StateUpdater;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.view.console.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * It handles Lobby of game!
 * And also applies admin order!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.1.2
 */
public class Lobby extends PartHandler {

    /**
     * Admin token.
     * Admin token required to confirm admin orders.
     */
    private final String adminToken;
    /**
     * We are going to define some props for it
     * using admin commands!
     */
    private final StateUpdater stateUpdater;
    /**
     * This is a list of confirmations.
     * Used to make sure all players have confirmed a order,
     * like start!
     */
    private final HashMap<String, Boolean> confirmations;
    /**
     * If admin called the start command!
     * it means check confirmation
     */
    private boolean startCall;
    /**
     * Server of game
     */
    private final Server server;

    /**
     * Builds requirements for lobby.
     * @param adminToken admin token.
     * @param stateUpdater contains state updater of game!
     */
    public Lobby(String adminToken, StateUpdater stateUpdater, Server server) throws Exception {
        super();
        if(server == null || stateUpdater == null || adminToken == null)
            throw new Exception("Null input!");
        this.stateUpdater = stateUpdater;
        this.adminToken = adminToken;
        this.server = server;
        gameState = new GameState(State.Lobby, null);
        confirmations = new HashMap<String, Boolean>();
        startCall = false;
        myState = State.Lobby;
    }

    /**
     * This method runs the part handler!
     */
    @Override
    public void run() {
        lastRead = 0;
        // Telling game loop that we are in lobby! (Lobby hand shake)
        DataBox newDataBox = new DataBox(gameState, null);
        sharedSendingDataBase.add(newDataBox);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}

        // running lobby logic
        while (!finished){
            try {
                applyLogic();
            }catch (Exception e){
                Logger.error("Failed while applying message" + e.getMessage(),
                        LogLevel.ThreadWarning, "Lobby");
            }
        }
    }

    // Defining logic for lobby:
    /**
     * Checks admin order.
     * existing commands are in AdminCommand.java
     * commands starts with '@' and typed in capital
     * there is help with commands in game!
     * like:
     *      - @START
     *      - @
     */
    @Override
    protected void applyLogic(){

        // If all confirmed to start the game!
        if(checkConfirmations()){
            sendToAll(Color.GREEN_BOLD + "Game is starting...");
            stateUpdater.setGameStarted(true);
            try {
                Thread.sleep(5000); // Hold to change state
            }catch (Exception ignored){}
            return;
        }

        if(lastRead >= inputDataBase.getSize())
            return;
        Data data = (Data) inputDataBase.readData(lastRead);
        lastRead++;
        if(data == null)
            return;
        if(!(data instanceof Message))
            return;
        Message message = (Message) data;
        // Checking commands
        if(message.getMessageText().startsWith("@")){
            if(message.getSenderToken().equals(adminToken))
                checkAdminCommand(message.getMessageText());    // if admin command
            else
                checkUserCommand(message);                      // if user command
        }
        // Sending normal message (cheat chat)
        else {
            send(message);
        }
    }

    // Built in methods to help applyLogic()

    /**
     * Send a message to a specific user
     * @param messageText will be sent
     * @param userToken is the receiver token
     */
    @Override
    protected void sendToUser(String messageText, String userToken){
        if(messageText == null || userToken == null)
            return;
        Message message = new Message(null, Color.BLUE_BOLD + "SERVER", messageText);
        message.setReceiverToke(userToken);
        send(message);
    }

    /**
     * Send a text from server to all players
     * @param messageText is text of message
     */
    @Override
    protected void sendToAll(String messageText){
        Message newMessage = new Message(null, Color.BLUE_BOLD + "SERVER",
                messageText);
        send(newMessage);
    }

    /**
     * Checks admin command and respond to that
     * @param adminMessageText text of admin command
     */
    private void checkAdminCommand(String adminMessageText){
        if(adminMessageText == null)
            return;
        if(!adminMessageText.startsWith("@"))
            return;
        adminMessageText = adminMessageText.toUpperCase(Locale.ROOT);
        adminMessageText = adminMessageText.substring(1); // Removing '@' from command
        String serverRespond = null;

        // Analyzing commands:

        // START command, used to start the game
        if(adminMessageText.startsWith(AdminCommand.START.toString())){
            try {
                if(clientHandlers.size() > server.getMaxConnectionNumber() || clientHandlers.size() < 6){
                    sendToUser(Color.YELLOW_BOLD + "At least 6 players needed to call the start!",
                            adminToken);
                    return;
                }
            }catch (Exception e){
                sendToUser(Color.RED_BOLD + "Failed to call start!", adminToken);
                return;
            }
            if(startCall){
                sendToUser(Color.YELLOW_BOLD + "You already called a confirmation process",
                        adminToken);
                return;
            }
            startCall = true;
            resetConfirmations();
            confirm(adminToken);
            sendToUser(Color.GREEN_BOLD + "Starting the confirmation process!", adminToken);
            Message message = new Message(adminToken, Color.BLUE_BOLD + "SERVER",
                    Color.GREEN_BOLD + "Please confirm to start the game!");
            send(message);
            return;
        }
        // SET command, used to change setting
        else if(adminMessageText.startsWith(AdminCommand.SET.toString())){
            try {
                String[] props = adminMessageText.split(" ");
                int value = Integer.parseInt(props[3]);
                boolean correctSet = false;
                if(props[1].equals(AdminCommand.TIME.toString())){
                    if(props[2].equals(AdminCommand.DAY.toString())){
                        if(stateUpdater.setDayTimer(value))
                            serverRespond = Color.GREEN_BOLD + "Day time is set to " + Color.YELLOW_BOLD + value;
                    }
                    else if(props[2].equals(AdminCommand.VOTE.toString())){
                        if(stateUpdater.setVoteTimer(value))
                            serverRespond = Color.GREEN_BOLD + "Vote time is set to " + Color.YELLOW_BOLD + value;
                    }
                    else if(props[2].equals(AdminCommand.NIGHT.toString())){
                        if(stateUpdater.setNightTimer(value))
                            serverRespond = Color.GREEN_BOLD + "Night time is set to " + Color.YELLOW_BOLD + value;
                    }
                    else {
                        serverRespond = Color.RED_BOLD + "Unknown property for SET TIME";
                    }
                }
                // Changing max player value
                else if(props[1].equals(AdminCommand.MAX.toString())){
                    if(props[2].equals(AdminCommand.PLAYER.toString())){
                        if(setMaxPlayer(value)){
                            serverRespond = Color.GREEN_BOLD + "Max player is set to " +
                                    Color.YELLOW_BOLD + value;
                        }
                    }
                    else {
                        serverRespond = Color.RED_BOLD + "Unknown property for SET MAX";
                    }
                }
                else {
                    serverRespond = Color.RED_BOLD + "Wrong SET structure!";
                }
                if(serverRespond == null)
                    serverRespond = Color.RED_BOLD + "Invalid value to SET";
            }catch (Exception e){
                serverRespond = Color.RED_BOLD + "Wrong SET structure!";
            }
        }
        // CLOSE command, ends lobby and server
        else if(adminMessageText.startsWith(AdminCommand.CLOSE.toString())){
            sendToUser(Color.YELLOW_BOLD + "No confirmation is in process!", adminToken);
            return;
        }
        // LIVES command, returns current list of player
        else if(adminMessageText.startsWith(AdminCommand.LIVES.toString())){
            serverRespond = Color.YELLOW_BOLD + "List of player:";
            int index = 0;
            for (SenderHandler sh : senderHandlers){
                serverRespond += Color.RESET + "\n";
                serverRespond += Color.BLUE + index + Color.RED + " - " +
                        Color.BLUE + sh.getClientHandler().getNickname();
                if(sh.getClientHandler().getToken().equals(adminToken))
                    serverRespond += Color.GREEN_BOLD + " (Admin)";
                index++;
            }
        }
        // REMOVE command, used to remove a player
        else if(adminMessageText.startsWith(AdminCommand.REMOVE.toString())){
            sendToUser(Color.YELLOW_BOLD + "This feature will be added!", adminToken);
            return;
        }
        // END command, used to end a process like confirmation process
        else if(adminMessageText.startsWith(AdminCommand.END.toString())){
            if(startCall){
                startCall = false;
                resetConfirmations();
                sendToAll(Color.YELLOW_BOLD + "Starting has been canceled!");
                return;
            }
            else {
                sendToUser(Color.YELLOW_BOLD + "No confirmation is in process!", adminToken);
                return;
            }
        }
        // Unknown command
        else {
            serverRespond = Color.RED_BOLD + "Unknown command!";
        }
        sendToUser(serverRespond, adminToken);
    }

    /**
     * Message of user
     * @param userCommandMessage (message) contains command of user
     */
    private void checkUserCommand(Message userCommandMessage){
        if(userCommandMessage == null)
            return;
        if(!userCommandMessage.getMessageText().startsWith("@"))
            return;
        String userCommand = userCommandMessage.getMessageText();
        String userToken = userCommandMessage.getSenderToken();
        userCommand = userCommand.toUpperCase(Locale.ROOT);
        userCommand = userCommand.substring(1); // Removing '@' from command!
        String serverRespond = null;

        // Analyzing user command:

        // Confirming a process like starting game
        if(userCommand.startsWith(PlayerCommand.CONFIRM.toString())){
            if(startCall){
                if(confirm(userToken)){
                    Message msg = new Message(userCommandMessage.getSenderToken(),
                            Color.BLUE_BOLD + "GOD",
                            Color.GREEN_BOLD + userCommandMessage.getSenderName() + " Confirmed!");
                    sendToUser(Color.GREEN_BOLD + "You confirmed", userToken);
                    send(msg);
                }else {
                    sendToUser(Color.YELLOW_BOLD + "You already confirmed!",
                            userToken);
                }
                return;
            }
            serverRespond = Color.YELLOW_BOLD + "No confirmation is in process!";
        }
        // LIVES command, return the list of players
        else if(userCommand.startsWith(PlayerCommand.LIVES.toString())){
            serverRespond = Color.YELLOW_BOLD + "List of player:";
            int index = 0;
            for (SenderHandler sh : senderHandlers){
                serverRespond += Color.RESET + "\n";
                serverRespond += Color.BLUE + index + Color.RED + " - " +
                        Color.BLUE + sh.getClientHandler().getNickname();
                if(sh.getClientHandler().getToken().equals(adminToken))
                    serverRespond += Color.GREEN_BOLD + " (Admin)";
                if(sh.getClientHandler().getToken().equals(userToken))
                    serverRespond += Color.YELLOW_BOLD + " (You)";
                index++;
            }
        }
        // CLOSE command, leaving lobby!
        else if(userCommand.startsWith(PlayerCommand.CLOSE.toString())){
            ClientHandler clientHandler = null;
            for(ClientHandler ch : clientHandlers){
                if(ch.getToken().equals(userToken)){
                    clientHandler = ch;
                    break;
                }
            }
            if(clientHandler != null){
                clientHandler.shutdown();
                refreshSRHandlersList();
                sendToAll(Color.RED_BOLD + userCommandMessage.getSenderName() + " left the lobby!");
                return;
            }
        }
        // Unknown command
        else {
            serverRespond = Color.RED_BOLD + "Unknown command!";
        }
        sendToUser(serverRespond, userToken);
    }

    // Confirmation tools:

    /**
     * If start has called player can confirm to start the game
     * @param token of player
     *
     */
    private boolean confirm(String token){
        if(startCall){
            if(confirmations.containsKey(token)){
                if(!confirmations.get(token)){
                    confirmations.put(token, true);
                    return true;
                }
            }else {
                confirmations.put(token, true);
                return true;
            }
        }
        return false;
    }

    /**
     * check if all players have confirmed
     * @return true if all confirmed
     */
    private boolean checkConfirmations(){
        if(!startCall)
            return false;
        try{
            for(ClientHandler ch : new ArrayList<ClientHandler>(clientHandlers)){
                if(confirmations.containsKey(ch.getToken())) {
                    if (!confirmations.get(ch.getToken()))
                        return false;
                }else {
                    confirmations.put(ch.getToken(), false);
                    return false;
                }
            }
            return true;
        }catch (Exception e){
            Logger.error("Failed to check confirmation list", LogLevel.ThreadWarning, "Lobby");
            return false;
        }
    }

    /**
     * Reset confirmations!
     */
    private void resetConfirmations(){
        try{
            for(ClientHandler ch : new ArrayList<ClientHandler>(clientHandlers)){
                if(confirmations.containsKey(ch.getToken()))
                    confirmations.put(ch.getToken(), false);
            }
        }catch (Exception e){
            Logger.error("Failed to reset confirmation", LogLevel.ThreadWarning, "Lobby");
        }
    }

    // Setters
    private boolean setMaxPlayer(int value){
        try {
            refreshSRHandlersList();
            if(value < 6 || value < senderHandlers.size() || value > 10)
                return false;
            return server.setMaxConnectionNumber(value);
        }catch (Exception e){
            return false;
        }
    }

}
