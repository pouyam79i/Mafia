package ir.pm.mafia.view.ui.interfaces;

import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.DataType;
import ir.pm.mafia.model.utils.memory.SharedMemory;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.model.game.logic.commands.PlayerCommand;
import ir.pm.mafia.model.utils.fio.FileUtils;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.view.console.Color;
import ir.pm.mafia.view.ui.Interface;

import java.util.Locale;

/**
 * This class contains the structure of chat room user interface!
 * It displays chat room new received messages!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.4.1
 */
public class ChatRoomUI extends Interface{

    /**
     * Contains title message of chat room interface
     */
    private final String title;
    /**
     * save mode tell if you want to save received messages or not!
     */
    private final boolean saveMode;
    /**
     * Used to save chat room information
     */
    private FileUtils fileUtils = null;

    /**
     * Constructor of ChatRoomUI
     * Setup requirements
     * @param sendBox of chat room
     * @param receivedBox is the input data which will be used to update display!
     * @param myToken will be used to send data box
     * @param myName will be used to send data box
     * @param title of chat room
     * @param saveMode save mode
     * @throws Exception if failed to build UI
     */
    public ChatRoomUI(SharedMemory sendBox,
                      SharedMemory receivedBox,
                      String myToken,
                      String myName,
                      String title,
                      boolean saveMode) throws Exception {
        super(sendBox, receivedBox, myToken, myName);
        this.title = title;
        this.saveMode = saveMode;
        if(saveMode){
            while (fileUtils == null)
                fileUtils = FileUtils.getFileUtils();
        }else
            fileUtils = null;
        threadName = "Chatroom";
    }

    /**
     * Displays the chat room for user!
     */
    @Override
    protected void display() {
        DataBox dataBox = null;
        // Used to do 3 loop before finishing it!
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
                    if(saveMode)
                        if(fileUtils != null)
                            fileUtils.addToReaderBox(message);
            }catch (Exception e){
                Logger.error("Failed to read display new message!",
                        LogLevel.ThreadWarning, "ChatRoomUI");
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
        if(saveMode){
            if(input.toUpperCase(Locale.ROOT).equals("@" + PlayerCommand.HISTORY.toString())){
                if(fileUtils != null){
                    console.println(Color.YELLOW_BOLD + "Printing previous messages...");
                    fileUtils.printPreviousMessages();
                    console.println(Color.YELLOW_BOLD + "Done Printing previous messages!");
                }
                return;
            }
        }
        Message message = new Message(myToken, myName, input);
        DataBox dataBox = new DataBox(null, message);
        sendBox.put(dataBox);
    }

    /**
     * Runs the chatroom
     */
    @Override
    public void run() {
        if(saveMode)
            if(fileUtils != null)
                fileUtils.start();
        if(!listeningState){
            listener.start();
            listeningState = true;
        }
        console.println(RED + "Chat room " + YELLOW_BOLD +
                "**** " + BLUE_BRIGHT + title + YELLOW_BOLD + " ****");
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
        if(fileUtils != null)
            fileUtils.shutdown();
        listeningState = false;
        this.close();
    }

}
