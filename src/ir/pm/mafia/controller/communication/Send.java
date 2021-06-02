package ir.pm.mafia.controller.communication;

import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.io.ObjectOutputStream;

/**
 * This class handles the process of sending data to the network!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1
 */
public class Send extends Runnable {

    /**
     * This is a share memory used as send box!
     * Send class reads send box and sends new data!
     */
    private final SharedMemory sendBox;
    /**
     * This object is used to send data over network!
     */
    private final ObjectOutputStream objectOutputStream;

    /**
     * Constructor of Send
     * Setup requirements before running sending process
     * @param sharedMemory send box
     * @param objectOutputStream sender object must be a (ObjectOutputStream)
     */
    public Send(SharedMemory sharedMemory, ObjectOutputStream objectOutputStream) throws Exception{
        if(sharedMemory == null || objectOutputStream == null)
            throw new Exception("Null input");
        sendBox = sharedMemory;
        this.objectOutputStream = objectOutputStream;
    }

    /**
     * Running sending process
     */
    @Override
    public void run() {
        DataBox dataBox = null;
        while (!finished){
            try {
                dataBox = (DataBox) sendBox.get();
                if(dataBox != null){
                    objectOutputStream.writeObject(dataBox);
                    objectOutputStream.flush();
//                    Logger.log("Sending data finished!", LogLevel.Report, "Send");
                }
            }catch (Exception e){
                Logger.error("Failed while transferring data: " + e.getMessage(),
                        LogLevel.ThreadWarning,
                        "communication.Send");
            }
        }
        done = true;
    }

}
