package ir.pm.mafia.view.ui.interfaces;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.DataType;
import ir.pm.mafia.model.utils.memory.SharedMemory;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.model.game.logic.commands.PlayerCommand;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.view.console.Color;
import ir.pm.mafia.view.ui.Interface;

import java.util.ArrayList;
import java.util.Locale;

public class NightUI extends Interface {

    /**
     * List of alive players you can vote for
     */
    private final ArrayList<String> alivePlayers;

    /**
     * Constructor of NightUI
     * Setup requirements
     * @param sendBox of night action console
     * @param receivedBox is the input data which will be used to update display!
     * @param myToken will be used to send data box
     * @param myName will be used to send data box
     * @param alivePlayers list of alive players
     * @throws Exception if failed to build UI
     */
    public NightUI(SharedMemory sendBox,
                  SharedMemory receivedBox,
                  String myToken,
                  String myName,
                  ArrayList<String> alivePlayers) throws Exception {
        super(sendBox, receivedBox, myToken, myName);
        this.alivePlayers = alivePlayers;
        threadName = "NightUI";
    }

    /**
     * Displays the chat room for user!
     */
    @Override
    protected void display() {
        DataBox dataBox = null;
        // Used to do 2 loop before finishing it!
        int counterKill = 0;
        while (counterKill < 2){
            counterKill++;
            try {
                dataBox = (DataBox) receivedBox.get();
                if(dataBox == null)
                    continue;
                if(!(dataBox.getData().getDataType() == DataType.Message))
                    continue;
                Message message = (Message) dataBox.getData();
                console.println(PURPLE_BOLD + message.getSenderName() +
                        RED_BOLD + ": " + BLUE + message.getMessageText());
            }catch (Exception e){
                Logger.error("Failed to read display new message!",
                        LogLevel.ThreadWarning, "VoteUI");
            }
        }
    }

    /**
     * updates chat room contents
     * @param args will be used to updated contents!
     */
    @Override
    public synchronized void update(String... args) {
        display();
        Listen();
    }

    /**
     * This one runs the listener
     */
    @Override
    public void Listen() {
        String input = (String) listener.getInputBox().get();
        if(input == null)
            return;
        if(input.equals(""))
            return;
        Data data = buildRespond(input);
        if(data == null)
            return;
        DataBox dataBox = new DataBox(null, data);
        sendBox.put(dataBox);
        Logger.log("Sending data to server...", LogLevel.Report, "NightUI");
    }

    /**
     * Runs the vote room
     */
    @Override
    public void run() {
        console.println(BLUE +
                "**** " + PURPLE_BOLD + "Night" + BLUE + " ****");
        console.println(getListOfOption());
        if(!listeningState){
            listener.start();
            listeningState = true;
        }
        listeningState = false;
        display();
        while (!finished)
            update();
    }

    /**
     * It will shutdown listener and this ui
     */
    @Override
    public void shutdown(){
        finished = true;
        if(listener != null)
            listener.shutdown();
        listeningState = false;
        this.close();
    }

    /**
     * Build a proper data to send with given input
     * @param input of user
     * @return Vote (and can be Message for mayer)
     */
    private Data buildRespond(String input){
        if(input == null)
            return null;
        input = input.toUpperCase(Locale.ROOT);
        // Analyzing commands:

        // VOTE command
        if(input.startsWith("@" + PlayerCommand.ACTION.toString())){
            try {
                String playerName = null;
                try {
                    int index = Integer.parseInt(input.split(" ")[1]);
                     playerName = alivePlayers.get(index);
                }catch (Exception ignored){}
                if(playerName != null)
                    if(playerName.equals(myName)){
                        console.println(Color.YELLOW_BOLD + "Cannot act to yourself!");
                        return null;
                    }
                console.println(Color.GREEN_BOLD + "Sending action...");
                return new Message(myToken, myName, "@" +
                        PlayerCommand.ACTION.toString() + " " + playerName);
            }catch (Exception e){
                console.println(Color.RED + "Invalid input");
                return null;
            }
        }
        // HELP command ---> send my a help note
        else if(input.startsWith("@" + PlayerCommand.HELP.toString())){
            try {
                return new Message(myToken, myName, "@" + PlayerCommand.HELP.toString());
            }catch (Exception e){
                console.println(Color.RED + "Invalid input");
                return null;
            }
        }
        // LIST command returns list of options
        else if(input.startsWith("@" + PlayerCommand.LIST.toString())){
            try {
                return new Message(myToken, myName, "@" + PlayerCommand.HELP.toString());
            }catch (Exception e){
                console.println(Color.RED + "Invalid input");
                return null;
            }
        }
        return null;
    }

    /**
     * Get a proper string of list of players
     * @return String
     */
    private String getListOfOption(){
        int index = 0;
        StringBuilder list = new StringBuilder(YELLOW_BOLD + "This is list of player you can vote for:\n");
        if(alivePlayers == null){
            list = new StringBuilder(RED_BOLD + "No player!");
            return list.toString();
        }
        for(String name : alivePlayers){
            list.append("\n");
            if(name.equals(myName))
                list.append(PURPLE_BOLD).append(index).append(" - ").append(name).append(" (You)");
            else
                list.append(BLUE_BOLD).append(index).append(" - ").append(name);
            index++;
        }
        return list.toString();
    }

}
