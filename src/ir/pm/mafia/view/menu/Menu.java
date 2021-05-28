package ir.pm.mafia.view.menu;

import ir.pm.mafia.view.console.Console;

/**
 * This is abstract class for all designed menus.
 * contains the structure and required tools to show and handle a menu.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 */
public abstract class Menu {

    /**
     * Console is used to interact with users
     */
    protected Console console;
    /**
     * message contains menu interface!
     */
    protected String message;

    /**
     * Constructor of Menu
     * Setup required tools
     */
    public Menu(){
        // getting a console
        console = Console.getConsole();
    }

    /**
     * deploys the menu interface and run it.
     */
    public abstract void deploy();

    /**
     * This method updates the message of menu.
     * @param args it is set if any input required to updated message!
     */
    protected abstract void updateMessage(String... args);

}
