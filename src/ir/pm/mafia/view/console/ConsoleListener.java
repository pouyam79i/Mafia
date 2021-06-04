package ir.pm.mafia.view.console;

import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.utils.multithreading.Runnable;

/**
 * Console listener is used to read inputs from console,
 * but in multithreading mode!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class ConsoleListener extends Runnable {

    /**
     * building console listener in singleton mode
     */
    private static ConsoleListener instanceOfListener = null;

    /**
     * Using console to read inputs
     */
    private final Console console;
    /**
     * Shared memory is used to share input of console between threads
     */
    private final SharedMemory inputBox;

    /**
     * Constructor of ConsoleListener
     * Setups requirements
     */
    private ConsoleListener(){
        inputBox = new SharedMemory(true);
        console = Console.getConsole();
    }

    /**
     * Running the listener
     */
    @Override
    public void run() {
        String input;
        while (!finished){
            input = console.readConsole();
            inputBox.put(input);
        }
    }

    /**
     * input box is where we put what ever user inputs!
     * so other thread can get the input.
     * @return input box shared memory
     */
    public SharedMemory getInputBox() {
        return inputBox;
    }

    /**
     * Builds (or just returns) a listener!
     * @return Console listener
     */
    public static ConsoleListener getListener() {
        if(instanceOfListener == null)
            instanceOfListener = new ConsoleListener();
        return instanceOfListener;
    }

}
