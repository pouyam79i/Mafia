package ir.pm.mafia.controller.communication;

import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.io.ObjectInputStream;

/**
 * This class handles the process of sending data to the network!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class Receive extends Runnable {

    /**
     * This is a share memory used as receive box!
     * receive class writes new data to receive box!
     */
    private final SharedMemory receiveBox;
    /**
     * This object is used to receive data over network!
     */
    private final ObjectInputStream objectInputStream;

    /**
     * Constructor of Receive
     * Setup requirements before running receiving process
     * @param sharedMemory receive box
     * @param objectInputStream receiver object must be a (ObjectInputStream)
     */
    public Receive(SharedMemory sharedMemory, ObjectInputStream objectInputStream){
        receiveBox = sharedMemory;
        this.objectInputStream = objectInputStream;
    }

    /**
     * Running receiving process
     */
    @Override
    public void run() {
        try {
            DataBox dataBox = (DataBox) objectInputStream.readObject();
            if(dataBox != null)
                receiveBox.put(dataBox);
        } catch (Exception e) {
            Logger.error("Failed while receiving data: " + e.getMessage(),
                    LogLevel.ThreadWarning,
                    "communication.Receive");
        }
        done = true;
    }

}

