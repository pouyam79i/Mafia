package ir.pm.mafia.controller.data;

import java.io.Serializable;

/**
 * This class contains the basic structure for all kind of data!
 * Used to transfer data between server and clients.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1
 */
public abstract class Data implements Serializable {

    /**
     * This is a unique token for each client!
     */
    protected final String senderToken;
    /**
     * Contains sender user nickname or username
     */
    protected final String senderName;
    /**
     * Contains type of data!
     */
    protected final DataType dataType;

    /**
     * Constructor of Data
     * Sets important fields
     * @param senderToken is the sender token!
     * @param senderName is the sender nickname or username!
     */
    protected Data(String senderToken, String senderName, DataType dataType) {
        // Insurance for not null fields
        if(senderToken == null)
            senderName = "Empty Address";
        if(senderName == null)
            senderName = "Unknown";
        if(dataType == null)
            dataType = DataType.Undefined;
        this.senderToken = senderToken;
        this.senderName = senderName;
        this.dataType = dataType;
    }

    // Getters
    public String getSenderToken() {
        return senderToken;
    }
    public String getSenderName() {
        return senderName;
    }
    public DataType getDataType() {
        return dataType;
    }

}
