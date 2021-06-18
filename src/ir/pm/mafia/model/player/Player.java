package ir.pm.mafia.model.player;

import ir.pm.mafia.model.utils.memory.SharedMemory;
import ir.pm.mafia.view.console.Color;
import ir.pm.mafia.view.console.Console;

/**
 * Contains to hold player details.
 * Used in player-side only!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1
 */
public class Player {

    /**
     * Each player has unique token
     */
    private String token;
    /**
     * Nickname of player!
     */
    private String nickname;
    /**
     * Share memory used as send box.
     * Client gets these and send it to server.
     */
    private final SharedMemory sendBox;
    /**
     * Share memory used as receive box.
     * Client puts these and game loop of player consume them.
     */
    private final SharedMemory receiveBox;
    /**
     * Character is set by server loop!
     * server sends a special data box to set it.
     */
    private Character character;

    /**
     * Sets main and most important thing for player,
     * to be able to interact in game.
     * Which are player shared memory boxes.
     */
    public Player() {
        sendBox = new SharedMemory(true);
        receiveBox = new SharedMemory(true);
        token = null;
        nickname = null;
        character = null;
    }

    // Setters
    public boolean setToken(String token) {
        if(this.token == null && token != null){
            this.token = token;
            return true;
        }
        return false;
    }
    public void setNickname(String input) {
        input = null;
        // Setting nickname of player!
        Console.getConsole().println(Color.YELLOW + "Please enter your nick name:");
        Console.getConsole().println(Color.PURPLE + "  -This name will be displayed for other players");
        while (input == null){
            input = Console.getConsole().readConsole();
            try {
                if(input.split(" ")[0].equals("") || input.equals("")){
                    Console.getConsole().println(Color.RED + "Invalid name");
                    input = null;
                }
            }catch (Exception e){
                Console.getConsole().println(Color.RED + "Invalid name");
                input = null;
            }
        }
        nickname = input;

    }
    public boolean setCharacter(Character character) {
        if(this.character == null && character != null){
            this.character = character;
            return true;
        }
        return false;
    }

    // Getters
    public String getToken() {
        return token;
    }
    public String getNickname() {
        return nickname;
    }
    public SharedMemory getSendBox() {
        return sendBox;
    }
    public SharedMemory getReceiveBox() {
        return receiveBox;
    }
    public Character getCharacter() {
        return character;
    }

}
