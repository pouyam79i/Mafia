package ir.pm.mafia.model.game.character;

/**
 * Contains character actions
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public enum Action {

    SHOOT,          // Head of mafia and sniper.
    REVIVE,         // Doctors can revive.
    DISMISS,        // Mayer can dismiss voting.
    SILENCE,        // Psychologist can quiet one player.
    ENQUIRY,        // Detective can enquiry some character.
    ENQUIRY_DEAD,   // Impregnable can enquiry dead character.
    EMPTY,          // No action.

}
