package ir.pm.mafia.view.menu.menus;

import ir.pm.mafia.view.menu.Menu;

/**
 * This Class contains the structure of main menu
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 */
public class Main extends Menu {

    /**
     * Main Menu Constructor
     * Setup requirements
     * @param username will the username of user
     */
    public Main(String username){
        // Calling super class constructor
        super();
        // Building main menu
        updateMessage(username);
    }

    /**
     * Displays the main menu
     */
    @Override
    public void display() {
        console.println(message);
    }

    /**
     * waits for user input
     * @return String [it contains one of the number from 1 to 4]
     */
    @Override
    public String listen() {
        Integer input = null;
        while (input == null){
            console.print("Enter: ");
            try {
                input = Integer.parseInt(console.readConsole());
                if(input > 4 || input < 1){
                    console.println(RED + "Wrong input!" + RESET);
                    input = null;
                }
            }catch (Exception ignored){
                input = null;
                console.println(RED + "Invalid input!" + RESET);
                // It needs editing ----> 'ADD_LOGGER'     **********************************************
            }
        }
        return (input + "");
    }

    /**
     * updates main menu text message (UI of main menu).
     * @param args it is set if any input required to updated message!
     */
    @Override
    public void updateMessage(String... args) {
        String username = "";
        message = "";
        if(args != null){
            username = args[0];
            if(username == null)
                username = "";
        }
        message += BLUE_BOLD + "Welcome" + YELLOW_BOLD + " to " + RED_BOLD + "Mafia" +RESET + "\n\n";
        message += YELLOW_BOLD + "Main Menu" + RESET + "\n";
        message += BLUE + "   1" + RED + " - " + BLUE + "Create Room" + RESET + "\n";
        message += BLUE + "   2" + RED + " - " + BLUE + "Join Room" + RESET + "\n";
        message += BLUE + "   3" + RED + " - " + BLUE + "Setting" + RESET + "\n";
        message += BLUE + "   4" + RED + " - " + BLUE + "Exit" + RESET + "\n";
        if(!username.equals("")){
            message += YELLOW + "-------------------" + RESET + "\n";
            message += PURPLE + "Username" + RED + ": " + YELLOW + username + RESET + "\n";
        }
    }

}
