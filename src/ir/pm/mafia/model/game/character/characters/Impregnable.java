package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Impregnable (Hard Life)
 */
public class Impregnable extends Character {

    public Impregnable(){
        super();
        characterName = CharacterName.Impregnable;
        action = Action.ENQUIRY_DEAD;
        group = Group.Citizen;
        lives = 2;
    }

}
