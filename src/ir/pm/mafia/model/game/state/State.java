package ir.pm.mafia.model.game.state;

import java.io.Serializable;

/**
 * State Contains state of game!
 * State tells that where are we!
 * For Example if we are still in lobby,
 * the state is 'Lobby'.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.0
 */
public enum State implements Serializable {
    Day,            // Day is when we display day chat room.
    Vote,           // Vote happens after day. time to vote!
    Night,          // Night is when you can do your action!
    NightChat,      // (Only for mafia), display a chat room for mafia
    Lobby           // Lobby is when the game is not started!
}
