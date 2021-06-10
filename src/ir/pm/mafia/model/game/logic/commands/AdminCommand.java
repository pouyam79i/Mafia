package ir.pm.mafia.model.game.logic.commands;

/**
 * This enum contains admin commands!
 * only before game is started!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1.1
 */
public enum AdminCommand {

    START,          // Start the game. ends lobby and game begins.
    SET,            // Set new changers. like max number of connections!
    CLOSE,          // Closes the game loop and server.
    LIVES,          // Returns number of current connections.
    REMOVE,         // Remove a member in lobby.
    CONFIRM,        // Confirmation to do changes.
    TIME,           // Refers to time loops! like day time.
    DAY,            // Refers to day property.
    VOTE,           // Refers to vote property.
    NIGHT,          // Refers to night property.
    MAX,            // Refers to the max value of a variable.
    PLAYER,         // Refers to player
    END,            // Ends a process, like process of confirmation

}
