package ir.pm.mafia.model.game.character.action;

/**
 * Contains character actions
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1
 */
public enum Action {

    SHOOT_MAFIA,    // Head of mafia can kill citizen.
    SHOOT_SNIPER,   // Sniper can kill players.
    REVIVE_MAFIA,   // Doctor lecter can revive mafia.
    REVIVE,         // Citizen doctor can revive players.
    DISMISS,        // Mayer can dismiss the voting.
    SILENCE,        // Psychologist can quiet one player.
    ENQUIRY,        // Detective can enquiry some character.
    ENQUIRY_DEAD,   // Impregnable can enquiry dead character.
    EMPTY,          // No action.

}
