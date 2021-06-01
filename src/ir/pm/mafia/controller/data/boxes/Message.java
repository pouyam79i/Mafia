package ir.pm.mafia.controller.data.boxes;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.DataType;

/**
 * This is the data box of message.
 * Contains the structure of message.
 * Used to transfer data of chat rooms between clients and server!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class Message extends Data {

    /**
     * Contains the text of message!
     */
    private final String messageText;

    /**
     * Construct a message data.
     * Sets required fields.
     * @param senderToken sender's token
     * @param senderName sender's username
     * @param messageText data (text) of message
     */
    public Message(String senderToken, String senderName, String messageText) {
        // Setting basics requirement of data
        super(senderToken, senderName, DataType.Message);
        // Setting text of data
        if(messageText == null)
            messageText = "";
        this.messageText = messageText;
    }

    // Getters!
    public String getMessageText() {
        return messageText;
    }

}
