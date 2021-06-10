package ir.pm.mafia.model.game.state;

import java.io.Serializable;

/**
 * State Contains state of game!
 * State tells that where are we!
 * For Example if we are still in lobby,
 * the state is 'Lobby'.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.1.2
 */
public enum State implements Serializable {

    Day,            // Day is when we display day chat room.
    Vote,           // Vote happens after day. time to vote.
    Night,          // Night is when you can do your action.
    Lobby,          // Lobby is when the game is not started.
    Initial,        // When no state is set and good loop is just created.
    FINISHED,       // When game finished, show the winner.
    STARTED,        // When game has just started. Runs the introduction night!

}
