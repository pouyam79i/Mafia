package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.action.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Normal citizen
 */
public class NormalCitizen extends Character {

    /**
     * Constructor of NormalCitizen
     */
    public NormalCitizen(){
        characterName = CharacterName.Normal_Citizen;
        action = Action.EMPTY;
        group = Group.Citizen;
    }

}
