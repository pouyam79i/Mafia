package ir.pm.mafia.model.loops.godloop;

/**
 * This enum contains admin commands!
 * only before game is started!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public enum AdminCommand {

    START,          // Start the game. ends lobby and game begins.
    SET,            // Set new changers. like max number of connections!
    CLOSE,          // Closes the game loop and server.
    LIVES,          // Returns number of current connections.
    REMOVE,         // Remove a member in lobby.
    CONFIRM,        // Confirmation to do changes.

}
