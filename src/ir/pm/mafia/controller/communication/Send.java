package ir.pm.mafia.controller.communication;

import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * This class handles the process of sending data to the network!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.4.1
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
     * If the process has interrupted! it is true!
     */
    private boolean interrupted;
    /**
     * If locked, it means cannot receive data from user!
     */
    private volatile boolean locked;

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
        interrupted = false;
        locked = false;
        threadName = "Send";
    }

    /**
     * Running sending process
     */
    @Override
    public void run() {
        interrupted =false;
        Object sendObj = null;
        while ((!finished) && (!interrupted)){
            try {
                sendObj = sendBox.get();
                if(sendObj != null && (!locked)){
                    objectOutputStream.writeObject(sendObj);
                    objectOutputStream.flush();
                }
            }catch (Exception e){
                Logger.error("Failed while transferring data: " + e.getMessage(),
                        LogLevel.ThreadWarning,
                        "communication.Send");
                interrupted = true;
            }
        }
        this.shutdown();
    }

    /**
     * Shutdown thread
     */
    @Override
    public void shutdown(){
        try {
            objectOutputStream.close();
        } catch (IOException ignored) {}
        this.close();
    }

    // Setter
    public void setLocked(boolean locked) {
        if(locked == this.locked)
            return;
        this.locked = locked;
    }

    // Getters!
    public boolean isInterrupted() {
        return interrupted;
    }

}
