package ir.pm.mafia.model.game.logic.lobby;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.DataBase;
import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.model.game.handlers.PartHandler;
import ir.pm.mafia.model.game.handlers.SenderHandler;
import ir.pm.mafia.model.game.logic.commands.AdminCommand;
import ir.pm.mafia.model.game.logic.commands.PlayerCommand;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.model.game.state.StateUpdater;
import ir.pm.mafia.view.console.Color;

import java.util.HashMap;
import java.util.Locale;

/**
 * It handles Lobby of game!
 * And also applies admin order!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.0.2
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
     * Max player number.
     * Default is 10.
     */
    private int maxPlayer;
    /**
     * This is a list of confirmations.
     * Used to make sure all players have confirmed a order,
     * like start!
     */
    private HashMap<ClientHandler, Boolean> confirmations;
    /**
     * If admin called the start command!
     * it means check confirmation
     */
    private boolean startCall;

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
        confirmations = new HashMap<ClientHandler, Boolean>();
        maxPlayer = 10;
        startCall = false;
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

        // Refresh confirmation list!
        refreshConfirmationList();

        // Checking confirmation list
        if(checkConfirmation()){
            // Returning respond to all players
            String serverRespond = Color.GREEN_BOLD + "Game started!";
            Message serverToAll = new Message(null, Color.BLUE_BOLD + "SERVER", serverRespond);
            DataBox newDataBox = new DataBox(gameState, serverToAll);
            sharedSendingDataBase.add(newDataBox);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            stateUpdater.setGameStarted(true);
            return;
        }

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
            // Checking if player has sent command!
            if(((Message) data).getMessageText().startsWith("@")){

                String playerCommand = ((Message) data).getMessageText().toUpperCase(Locale.ROOT);
                playerCommand = playerCommand.substring(1); // Removing @
                String serverRespond = null;

                // Applying confirmation
                if(playerCommand.startsWith(PlayerCommand.CONFIRM.toString())){
                    // Add confirmation
                    if(startCall){
                        for(ClientHandler ch : clientHandlers){
                            if(ch.getToken().equals(data.getSenderToken())){
                                confirmations.put(ch, true);
                                break;
                            }
                        }
                        serverRespond = Color.GREEN_BOLD + "Confirmed!";
                    }
                    else {
                        serverRespond = Color.YELLOW + "No confirmation is in process!";
                    }
                }
                // Returning players in server
                else if(playerCommand.startsWith(PlayerCommand.LIVES.toString())){
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
                // Used to exit the game!
                else if(playerCommand.startsWith(PlayerCommand.CLOSE.toString())){
                    ClientHandler clientHandler = null;
                    for(ClientHandler ch : clientHandlers){
                        if(ch.getToken().equals(data.getSenderToken())){
                            clientHandler = ch;
                            break;
                        }
                    }
                    if(clientHandler == null)
                        return; // Just in case :)
                    clientHandler.shutdown();
                    serverRespond = Color.RED + data.getSenderName() + " left!";
                    Message serverToPlayer = new Message(null, Color.BLUE_BOLD
                            + "SERVER", serverRespond);
                    newDataBox = new DataBox(gameState, serverToPlayer);
                    sharedSendingDataBase.add(newDataBox);
                    return;
                }
                else{
                    serverRespond = Color.RED + "Unknown command!";
                }
                Message serverToPlayer = new Message(null, Color.BLUE_BOLD
                        + "SERVER", serverRespond);
                serverToPlayer.setReceiverToke(data.getSenderToken());
                newDataBox = new DataBox(gameState, serverToPlayer);
                sharedSendingDataBase.add(newDataBox);
                return;
            }
            // normal message handler!
            else{
                newDataBox = new DataBox(gameState, data);
                sharedSendingDataBase.add(newDataBox);
                return;
            }
        }

        // Checking if admin has not sent command!
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
            int currentNumberOfPlayer = getCurrentNumberOfPlayer();
            if(maxPlayer >=  currentNumberOfPlayer && currentNumberOfPlayer >= 6){
                startCall = true;
                for(ClientHandler ch : clientHandlers){
                    if(ch.getToken().equals(data.getSenderToken())){
                        confirmations.put(ch, true);
                        System.out.println("Strat called");
                        break;
                    }
                }
                // Alerting other players except admin
                serverRespond = Color.YELLOW_BOLD + "Game started called!\n" +
                        "Please confirm if you are ready";
                Message serverToPlayer = new Message(data.getSenderToken(),
                        Color.BLUE_BOLD + "SERVER", serverRespond);
                newDataBox = new DataBox(gameState, serverToPlayer);
                sharedSendingDataBase.add(newDataBox);
                // Sending to admin a respond as well
                serverRespond = Color.GREEN_BOLD + "Game start is called!\n"
                        + Color.YELLOW_BOLD + "Waiting for player confirmation...";
            }
            else {
                serverRespond = Color.RED_BOLD + "Invalid number pf player!\n" +
                        Color.YELLOW_BOLD + "Current number of player" +
                        Color.PURPLE_BOLD + ": " + Color.RED_BOLD + currentNumberOfPlayer;
            }
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
                // Changing game timing property
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
                }
                // Setting max number of player
                else if(props[1].equals(AdminCommand.MAX.toString())){
                    int value = Integer.parseInt(props[3]);
                    if(props[2].equals(AdminCommand.PLAYER.toString()))
                        correctSet = setMaxPlayer(value);
                    else
                        serverRespond = Color.RED + "Unknown property";
                }
                else {
                    serverRespond = Color.RED + "Unknown structure for " + Color.BLUE + "@" + Color.RED + "SET";
                }
                if(correctSet)
                    serverRespond = Color.GREEN + "Setting changed successfully";
                else if(serverRespond == null)
                    serverRespond = Color.RED + "This value is not allowed!";
                else {
                    serverRespond = Color.RED + "Unknown structure for " + Color.BLUE + "@" + Color.RED + "SET";
                }
            }catch (Exception e){
                serverRespond = Color.RED + "Unknown structure for " + Color.BLUE + "@" + Color.RED + "SET";
            }
        }

        // Ending confirmation process.
        else if(adminCommand.startsWith(AdminCommand.SET.toString())){
            if(startCall){
                startCall = false;
                resetConfirmations();
            }
        }

        // Rejecting unknown commands
        else {
            serverRespond = Color.RED + "Unknown command!";
        }

        // Returning respond to admin
        Message serverToAdmin = new Message(null, Color.BLUE_BOLD + "SERVER", serverRespond);
        serverToAdmin.setReceiverToke(adminToken);
        newDataBox = new DataBox(gameState, serverToAdmin);
        sharedSendingDataBase.add(newDataBox);

    }

    /**
     * Checks if all players have confirmed
     * @return true if all confirmed
     */
    private boolean checkConfirmation(){
        if(!startCall)
            return false;
        refreshConfirmationList();
        for(ClientHandler ch : clientHandlers){
            if(!confirmations.containsKey(ch)){
                // Just in case :)
                return false;
            }
            if(!confirmations.get(ch)){
                return false;
            }
        }
        return true;
    }

    /**
     * Add new connection to confirmation list!
     * but default confirmation value if false!
     */
    private void refreshConfirmationList(){
        for(ClientHandler ch : clientHandlers){
            if(!confirmations.containsKey(ch)){
                confirmations.put(ch, false);
            }
        }
    }

    /**
     * Reset the confirmation list
     */
    private void resetConfirmations(){
        // Will not reset if start is called!
        if(startCall)
            return;
        confirmations = new HashMap<ClientHandler, Boolean>();
        refreshConfirmationList();
    }

    // Setters
    public boolean setMaxPlayer(int maxPlayer) {
        if(maxPlayer > 10 || maxPlayer < 6)
            return false;
        this.maxPlayer = maxPlayer;
        return true;
    }

    // Getters
    public int getCurrentNumberOfPlayer() {
        refreshSenderList();
        return senderHandlers.size();
    }

}
