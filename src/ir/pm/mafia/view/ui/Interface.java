package ir.pm.mafia.view.ui;

import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.utils.multithreading.Runnable;
import ir.pm.mafia.view.console.Console;
import ir.pm.mafia.view.console.ConsoleListener;

/**
 * This class contains the structure for all user interfaces.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.2.1
 */
public abstract class Interface extends Runnable {

    /**
     * Contains running state,
     * and as long as it is false, the chat room runs!
     */
    protected boolean finished = false;
    /**
     * console is used to print messages
     */
    protected final Console console;
    /**
     * Used to send DataBox!
     */
    protected final SharedMemory sendBox;
    /**
     * This memory is used to read new data box!
     */
    protected final SharedMemory receivedBox;
    /**
     * listener. is used to read console input!
     */
    protected final ConsoleListener listener;
    /**
     * my token is used in data sending
     */
    protected final String myToken;
    /**
     * my name is used in data sending
     */
    protected final String myName;
    /**
     * listener state
     */
    protected boolean listeningState;

    /**
     * Constructor of Interface!
     * Setups requirements
     * @param sendBox of will be used in interface to send data box
     * @param receivedBox is the input data which will be used to update display!
     * @param myToken will be used to send data box
     * @param myName will be used to send data box
     * @throws Exception if failed to build UI
     */
    public Interface(SharedMemory sendBox,
                     SharedMemory receivedBox,
                     String myToken,
                     String myName) throws Exception {
        if(sendBox == null || receivedBox == null)
            throw new Exception("Null input");
        if(myToken == null)
            myToken = "empty";
        console = Console.getConsole();
        listener = ConsoleListener.getListener();
        // set shared location to be able to update new messages!
        this.sendBox = sendBox;
        this.receivedBox = receivedBox;
        this.myToken = myToken;
        this.myName = myName;
        listeningState = false;
    }

    /**
     * Displays the interface in console!
     */
    protected abstract void display();

    /**
     * Updates contents
     * @param args will be used to updated contents!
     */
    public abstract void update(String... args);

    /**
     * runs console input lister!
     * and also helps to send new data if called!
     * must be override
     */
    public abstract void runListening();

}
