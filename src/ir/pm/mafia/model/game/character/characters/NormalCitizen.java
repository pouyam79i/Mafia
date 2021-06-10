package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Normal citizen
 */
public class NormalCitizen extends Character {

    public NormalCitizen(){
        characterName = CharacterName.NormalCitizen;
        action = Action.EMPTY;
        group = Group.Citizen;
    }

}
