package ir.pm.mafia.model.game.logic;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.controller.server.ClientState;
import ir.pm.mafia.model.game.handlers.PartHandler;
import ir.pm.mafia.model.game.logic.commands.PlayerCommand;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.view.console.Color;

import java.util.Locale;

/**
 * It handles Day of game.
 * And also respond to normal commands of players.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.0.1
 */
public class Day extends PartHandler {

    /**
     * Token of admin!
     */
    private String adminToken;

    /**
     * Constructor of Day
     * Setups requirements
     */
    public Day(String adminToken) {
        gameState = new GameState(State.Day, null);
        myState = State.Day;
        this.adminToken = adminToken;
        threadName = "Day";
    }

    /**
     * Logic of This part handler
     * Check if player command or
     * normal message!
     */
    @Override
    protected void applyLogic() {
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
                    sendToUser(Color.RED_BOLD + "You are admin! cannot leave!", adminToken);
                }
                clientHandler.shutdown();
                clientHandler.updateClientState(ClientState.DISCONNECTED);
                refreshSRHandlersList();
                sendToAll(Color.RED_BOLD + message.getSenderName() + " left the lobby!");
            }
        }
        // Unknown commands
        else {
            sendToUser(Color.RED_BOLD + "Unknown command!", userToken);
        }
    }

}
