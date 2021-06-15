package ir.pm.mafia.model.game.character.action;

import ir.pm.mafia.view.console.Color;

/**
 * Contains notes for action help message!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public interface ActionNotes {

    // Head of mafia and sniper.
    String SHOOT = Color.PURPLE_BOLD + "Action" + Color.RED_BOLD + ": " + Color.RED_BOLD + "Shoot\n" +
        Color.PURPLE_BOLD + "Usage" + Color.RED_BOLD + ":" +
        Color.YELLOW_BOLD + " You can kill someone,\n" +
        "if you are in mafia team,\n" +
        "you cannot kill your teammate!.";

    // Doctors can revive.
    String REVIVE = Color.PURPLE_BOLD + "Action" + Color.RED_BOLD + ": " + Color.GREEN_BOLD + "Revive\n" +
            Color.PURPLE_BOLD + "Usage" + Color.RED_BOLD + ":" +
            Color.YELLOW_BOLD + " You can save someones life,\n" +
            "if you are in mafia team,\n" +
            "you cannot save citizens.\n" +
            "Plus doctor lecter can kill if he is head of mafia.";

    // Mayer can dismiss voting.
    String DISMISS = Color.PURPLE_BOLD + "Action" + Color.RED_BOLD + ": " + Color.BLUE_BOLD + "Dismiss\n" +
            Color.PURPLE_BOLD + "Usage" + Color.RED_BOLD + ":" +
            Color.YELLOW_BOLD + " You can dismiss a voting";

    // Psychologist can quiet one player.
    String SILENCE = Color.PURPLE_BOLD + "Action" + Color.RED_BOLD + ": " + Color.BLUE_BOLD + "Silence\n" +
            Color.PURPLE_BOLD + "Usage" + Color.RED_BOLD + ":" +
            Color.YELLOW_BOLD + " You can make someone quiet for a match.";

    // Detective can enquiry some character.
    String ENQUIRY = Color.PURPLE_BOLD + "Action" + Color.RED_BOLD + ": " + Color.BLUE_BOLD + "Enquiry\n" +
            Color.PURPLE_BOLD + "Usage" + Color.RED_BOLD + ":" +
            Color.YELLOW_BOLD + " Detectives can find someones role!\n" +
            "except for godfather and normal citizens";

    // Impregnable can enquiry dead character.
    String ENQUIRY_DEAD = Color.PURPLE_BOLD + "Action" + Color.RED_BOLD + ": " +
            Color.BLUE_BOLD + "Enquiry dead roles\n" +
            Color.PURPLE_BOLD + "Usage" + Color.RED_BOLD + ":" +
            Color.YELLOW_BOLD + " Impregnable can find dead roles!\n" +
            "except for godfather and normal citizens";     // Edit this part *******************

    // Normal mafia
    String NORMAL_MAFIA = Color.RED_BOLD + "You have no special role!\n" +
            "Except that you are current head of mafia!\n" +
            "Then you can kill someone!";

    // No special action
    String EMPTY = Color.YELLOW_BOLD + "You have no special role!";

}
