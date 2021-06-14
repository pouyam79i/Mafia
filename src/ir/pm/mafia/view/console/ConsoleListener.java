package ir.pm.mafia.view.console;

import ir.pm.mafia.controller.data.SharedMemory;
import ir.pm.mafia.model.utils.multithreading.Runnable;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Console listener is used to read inputs from console,
 * but in multithreading mode!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1.1
 */
public class ConsoleListener extends Runnable {

//    /**
//     * building console listener in singleton mode
//     */
//    private static ConsoleListener instanceOfListener = null;

    /**
     * Using console to read inputs
     */
    private final Scanner scanner;
    /**
     * Shared memory is used to share input of console between threads
     */
    private final SharedMemory inputBox;

    /**
     * Constructor of ConsoleListener
     * Setups requirements
     */
    public ConsoleListener(){
        inputBox = new SharedMemory(true);
        scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        threadName = "ConsoleListener";
    }

    /**
     * Running the listener
     */
    @Override
    public void run() {
        String input;
        while (!finished){
            try{
                input = scanner.nextLine();
                if(input != null)
                    inputBox.put(input);
            }catch (Exception ignored){}
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

//    /**
//     * Builds (or just returns) a listener!
//     * @return Console listener
//     */
//    public static ConsoleListener getListener() {
//        if(instanceOfListener == null)
//            instanceOfListener = new ConsoleListener();
//        return instanceOfListener;
//    }

}
