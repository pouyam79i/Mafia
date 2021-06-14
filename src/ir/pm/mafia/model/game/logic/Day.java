package ir.pm.mafia.model.game.logic;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.controller.server.ClientState;
import ir.pm.mafia.model.game.handlers.PartHandler;
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
 * It handles Day of game.
 * And also respond to normal commands of players.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.1
 */
public class Day extends PartHandler {

    /**
     * Token of admin!
     */
    private final String adminToken;
    /**
     * used to skip day chat room
     */
    private final StateUpdater stateUpdater;
    /**
     * This is a list of confirmations.
     * Used to make sure all players have confirmed a order,
     * like skip!
     */
    private final HashMap<String, Boolean> confirmations;
    /**
     * if skipped is called, it will be true
     */
    private boolean skipCalled;

    /**
     * Constructor of Day
     * Setups requirements
     * @param adminToken token of admin
     * @param stateUpdater state update of game
     * @throws Exception if null input
     */
    public Day(String adminToken, StateUpdater stateUpdater) throws Exception {
        if(adminToken == null || stateUpdater == null)
            throw new Exception("Null input");
        this.adminToken = adminToken;
        this.stateUpdater = stateUpdater;
        gameState = new GameState(State.Day, null);
        confirmations = new HashMap<String, Boolean>();
        myState = State.Day;
        skipCalled = false;
        threadName = "Day";
    }

    /**
     * Logic of This part handler
     * Check if player command or
     * normal message!
     */
    @Override
    protected void applyLogic() {

        // If all confirmed to skip the day!
        if(checkConfirmations()){
            sendToAll(Color.GREEN_BOLD + "Skipping the day...");
            stateUpdater.advance();
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
        if(message.getMessageText().startsWith("@"))
            checkPlayerCommand(message);
        else{
            send(message);
        }
    }

    /**
     * Checks the command message!
     * @param message which is received
     */
    private void checkPlayerCommand(Message message){
        if(message == null)
            return;
        String messageText = message.getMessageText();
        String userToken = message.getSenderToken();
        if(messageText == null || userToken == null)
            return;
        if(!messageText.startsWith("@"))
            return;

        messageText = messageText.toUpperCase(Locale.ROOT);
        messageText = messageText.substring(1); // Removing '@'

        // Analyzing commands:

        // Lives returns alive players and out of game players
        if(messageText.startsWith(PlayerCommand.LIVES.toString())){
            StringBuilder serverRespond = new StringBuilder(Color.YELLOW_BOLD + "List of alive player:");
            int index = 0;
            for (ClientHandler ch : clientHandlers){
                if(ch.getClientState() == ClientState.ALIVE || ch.getClientState() == ClientState.GHOST){
                    serverRespond.append(Color.RESET + "\n");
                    serverRespond.append(Color.BLUE).append(index).append(Color.RED)
                            .append(" - ").append(Color.BLUE).append(ch.getNickname());
                }
                else{
                    serverRespond.append(Color.RESET + "\n");
                    serverRespond.append(Color.PURPLE_BOLD).append(index).append(Color.RED)
                            .append(" - ").append(Color.PURPLE_BOLD).append(ch.getNickname())
                            .append(" (OUT OF GAME)");
                }
                index++;
            }
            sendToUser(serverRespond.toString(), userToken);
        }
        // Close is used to leave the game
        else if(messageText.startsWith(PlayerCommand.CLOSE.toString())){
            ClientHandler clientHandler = null;
            for(ClientHandler ch : clientHandlers){
                if(ch.getToken().equals(userToken)){
                    clientHandler = ch;
                    break;
                }
            }
            if(clientHandler != null){
                if(clientHandler.getToken().equals(adminToken)){
                    sendToUser(Color.RED_BOLD + "You are admin and cannot close server!", adminToken);
                }
                clientHandler.shutdown();
                clientHandler.updateClientState(ClientState.DISCONNECTED);
                refreshSRHandlersList();
                sendToAll(Color.RED_BOLD + message.getSenderName() + " left the lobby!");
            }
        }
        // Used to confirm the skipping process
        else if(messageText.startsWith(PlayerCommand.SKIP.toString())){
            if(confirm(userToken)){
                sendToUser(Color.GREEN_BOLD + "You confirmed to skip!", userToken);
                Message exceptMe = new Message(userToken, Color.BLUE_BOLD + "GOD",
                        Color.GREEN_BOLD + message.getMessageText() + " Confirmed to skip!");
                send(exceptMe);
            }
            else {
                sendToUser(Color.YELLOW_BOLD + "You have already confirmed!", userToken);
            }
        }
        // Unknown commands
        else {
            sendToUser(Color.RED_BOLD + "Unknown command!", userToken);
        }
    }

    /**
     * If start has called player can confirm to start the game
     * @param token of player
     *
     */
    private boolean confirm(String token){
        if(!skipCalled)
            skipCalled = true;
        if(confirmations.containsKey(token)){
            if(!confirmations.get(token)){
                confirmations.put(token, true);
                return true;
            }
        }else {
            confirmations.put(token, true);
            return true;
        }
        return false;               // You previously confirmed to skipped
    }

    /**
     * check if all players have confirmed to skip
     * @return true if all confirmed to skipped
     */
    private boolean checkConfirmations(){
        if(!skipCalled)
            return false;
        try{
            for(ClientHandler ch : new ArrayList<ClientHandler>(clientHandlers)){
                if(ch.getClientState() != ClientState.ALIVE)
                    continue;
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

}
