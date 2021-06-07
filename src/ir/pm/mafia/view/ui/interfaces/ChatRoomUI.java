package ir.pm.mafia.view.ui.interfaces;

import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.view.ui.Interface;

/**
 * This class contains the structure of chat room user interface!
 * It displays chat room new received messages!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.3.1
 */
public class ChatRoomUI extends Interface{

    /**
     * Contains title message of chat room interface
     */
    private final String title;

    /**
     * Constructor of ChatRoomUI
     * Setup requirements
     * @param sendBox of chat room
     * @param receivedBox is the input data which will be used to update display!
     * @param myToken will be used to send data box
     * @param myName will be used to send data box
     * @param title of chat room
     * @throws Exception if failed to build UI
     */
    public ChatRoomUI(SharedMemory sendBox,
                      SharedMemory receivedBox,
                      String myToken,
                      String myName,
                      String title) throws Exception {
        super(sendBox, receivedBox, myToken, myName);
        this.title = title;
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
                if(!(dataBox.getData() instanceof Message))
                    continue;
                Message message = (Message) dataBox.getData();
                    console.println(PURPLE_BOLD + message.getSenderName() +
                            RED_BOLD + ": " + BLUE + message.getMessageText());
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
        runListening();
    }

    /**
     * This one runs the listener
     */
    @Override
    public void runListening() {

        if(!listeningState){
            listener.start();
            listeningState = true;
        }
        String input = (String) listener.getInputBox().get();
        if(input == null)
            return;
        if(input.equals(""))
            return;
        Message message = new Message(myToken, myName, input);
        DataBox dataBox = new DataBox(null, message);
        sendBox.put(dataBox);
    }



    /**
     * Runs the chatroom
     */
    @Override
    public void run() {
        console.println(RED + "Chat room " + YELLOW_BOLD +
                "**** " + BLUE_BRIGHT + title + YELLOW_BOLD + " ****");
        listeningState = false;
        runListening();
        display();
        while (!finished)
            update();
    }

    /**
     * It will shutdown listener and this ui
     */
    @Override
    public void shutdown(){
        if(listener != null)
            listener.shutdown();
        listeningState = false;
        this.close();
    }

}
