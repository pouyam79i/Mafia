package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Mayer of city
 */
public class Mayer extends Character {

    public Mayer(){
        characterName = CharacterName.Mayer;
        action = Action.DISMISS;
        group = Group.Citizen;
    }

}
