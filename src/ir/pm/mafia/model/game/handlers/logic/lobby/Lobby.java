package ir.pm.mafia.model.game.handlers.logic.lobby;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.DataBase;
import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.model.game.handlers.PartHandler;
import ir.pm.mafia.model.game.handlers.SenderHandler;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.model.game.state.StateUpdater;
import ir.pm.mafia.view.console.Color;

import java.util.Locale;

/**
 * It handles Lobby of game!
 * And also applies admin order!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.0.1
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
     * Builds requirements for lobby.
     * @param adminToken admin token.
     * @param stateUpdater contains state updater of game!
     */
    public Lobby(String adminToken, StateUpdater stateUpdater) {
        super();
        sharedSendingDataBase = new DataBase();
        this.stateUpdater = stateUpdater;
        this.adminToken = adminToken;
        gameState = new GameState(State.Lobby, null);
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
            applyLogic();
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
    protected void applyLogic() {

        //  Checking inputs
        if(lastRead >= inputDataBase.getSize())
            return;
        DataBox newDataBox = null;
        Data data = (Data) inputDataBase.readData(lastRead);
        lastRead++;
        if(data == null)
            return;
        if(!(data instanceof Message))
            return;

        // Sending other players message normally
        if(!data.getSenderToken().equals(adminToken)){
            newDataBox = new DataBox(gameState, data);
            sharedSendingDataBase.add(newDataBox);
            return;
        }

        // Checking if admin has sent command!
        if(!((Message) data).getMessageText().startsWith("@")){
            newDataBox = new DataBox(gameState, data);
            sharedSendingDataBase.add(newDataBox);
            return;
        }

        // If it came here it means that we have command from admin!
        String adminCommand = ((Message) data).getMessageText().toUpperCase(Locale.ROOT);
        adminCommand = adminCommand.substring(1); // Removing @
        String serverRespond = null;

        // Start the game
        if(adminCommand.startsWith(AdminCommand.START.toString())){
            stateUpdater.setGameStarted(true);
            serverRespond = Color.GREEN_BOLD + "Game started!";
        }

        // End server
        else if(adminCommand.startsWith(AdminCommand.CLOSE.toString())){
            serverRespond = Color.YELLOW_BOLD + "In feature versions will be available :(";
        }

        // Return current people in server!
        else if(adminCommand.startsWith(AdminCommand.LIVES.toString())){
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

        // Setting game new settings
        else if(adminCommand.startsWith(AdminCommand.SET.toString())){
            try {
                String[] props = adminCommand.split(" ");
                boolean correctSet = false;
                if(props[1].equals(AdminCommand.TIME.toString())){
                    int value = Integer.parseInt(props[3]);
                    if(props[2].equals(AdminCommand.DAY.toString()))
                        correctSet = stateUpdater.setDayTimer(value);
                    else if(props[2].equals(AdminCommand.VOTE.toString()))
                        correctSet = stateUpdater.setVoteTimer(value);
                    else if(props[2].equals(AdminCommand.NIGHT.toString()))
                        correctSet = stateUpdater.setNightTimer(value);
                    else
                        serverRespond = Color.RED + "Unknown property";
                    if(correctSet)
                        serverRespond = Color.GREEN + "Setting changed successfully";
                    else if(serverRespond == null)
                        serverRespond = Color.RED + "This value is not allowed!";
                }else {
                    serverRespond = Color.RED + "Unknown structure for " + Color.BLUE + "@" + Color.RED + "SET";
                }
            }catch (Exception e){
                serverRespond = Color.RED + "Unknown structure for " + Color.BLUE + "@" + Color.RED + "SET";
            }
        }
        else {
            serverRespond = Color.RED + "Unknown command!";
        }

        // Returning respond to admin
        Message serverToAdmin = new Message(null, Color.BLUE_BOLD + "SERVER", serverRespond);
        serverToAdmin.setReceiverToke(adminToken);
        newDataBox = new DataBox(gameState, serverToAdmin);
        sharedSendingDataBase.add(newDataBox);

    }

}
