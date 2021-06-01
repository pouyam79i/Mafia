package ir.pm.mafia.view.ui;

import ir.pm.mafia.view.console.Console;

/**
 * This class contains the structure for all user interfaces.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public abstract class Interface implements Runnable{

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
     * Constructor of Interface!
     * Setups requirements
     */
    public Interface(){
        console = Console.getConsole();
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
     * Closes the interface
     */
    public synchronized void close(){
        finished = true;
    }

}
