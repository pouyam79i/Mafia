package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.action.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Mayer of city
 */
public class Mayer extends Character {

    /**
     * Constructor of Mayer
     */
    public Mayer(){
        characterName = CharacterName.Mayer;
        action = Action.DISMISS;
        group = Group.Citizen;
    }

}
