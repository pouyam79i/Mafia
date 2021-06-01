package ir.pm.mafia.view.ui.interfaces;

import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.view.ui.Interface;

/**
 * This class contains the structure of chat room user interface!
 * It displays chat room new received messages!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class ChatRoomUI extends Interface{

    /**
     * This memory is used to read new messages!
     */
    private final SharedMemory sharedMemory;
    /**
     * Contains title message of chat room interface
     */
    private final String title;

    /**
     * Constructor of ChatRoomUI
     * Setup requirements
     * @param sharedMemory of chat room
     * @param title of chat room
     */
    public ChatRoomUI(SharedMemory sharedMemory, String title){
        super();
        // set shared location to be able to update new messages!
        this.sharedMemory = sharedMemory;
        this.title = title;
    }

    /**
     * Displays the chat room for user!
     */
    @Override
    protected void display() {
        console.println("Chat room **** " + title + " ****");
        Message message = null;
        // Used to do 3 loop before finishing it!
        int counterKill = 0;
        while ((!finished) || counterKill < 3){
            try {
                message = (Message) sharedMemory.get();
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
        // No use here
    }

    /**
     * Runs the chatroom
     */
    @Override
    public void run() {
        display();
    }

}
