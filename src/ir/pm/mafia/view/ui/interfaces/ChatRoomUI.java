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
 * @version 1.2
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
     * @param receivedData is the input data which will be used to update display!
     * @param myToken will be used to send data box
     * @param myName will be used to send data box
     * @param title of chat room
     * @throws Exception if failed to build UI
     */
    public ChatRoomUI(SharedMemory sendBox,
                      SharedMemory receivedData,
                      String myToken,
                      String myName,
                      String title) throws Exception {
        super(sendBox, receivedData, myToken, myName);
        this.title = title;
    }

    /**
     * Displays the chat room for user!
     */
    @Override
    protected void display() {
        Message message = null;
        // Used to do 3 loop before finishing it!
        int counterKill = 0;
        while ((!finished) || counterKill < 3){
            try {
                message = (Message) receivedData.get();
                if(message != null)
                    console.println(message.getSenderName() + ": " + message.getMessageText());
            }catch (Exception e){
                Logger.error("Failed to read display new message!", LogLevel.ThreadWarning, "ChatRoomUI");
            }
            if(finished)
                counterKill++;
        }
    }

    /**
     * updates chat room contents
     * @param args will be used to updated contents!
     */
    @Override
    public synchronized void update(String... args) {
        display();
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
        if(input != null)
            if(input.equals(""))
                return;
        Message message = new Message(myToken, myName, input);
        DataBox dataBox = new DataBox(null, message);
    }



    /**
     * Runs the chatroom
     */
    @Override
    public void run() {
        console.println("Chat room **** " + title + " ****");
        display();
        runListening();
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
