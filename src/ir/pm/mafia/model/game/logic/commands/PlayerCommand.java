package ir.pm.mafia.model.game.logic.commands;

/**
 * This enum contains normal player commands!
 * can be used during the game
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1.2
 */
public enum PlayerCommand {

    CLOSE,          // Closes the game loop.
    LIVES,          // Returns number of current connections.
    CONFIRM,        // Confirmation to do changes
    VOTE,           // Vote some body.
    ACTION,         // Preform your action.
    CLEAR,          // Clear decided vote or action.
    DISMISS,        // Works only for mayer to dismiss a voting.
    SKIP,           // Used to skip a part like day chat room.
    HISTORY         // Used to print previous chatroom information.

}
