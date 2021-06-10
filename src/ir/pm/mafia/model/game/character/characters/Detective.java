package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Detective
 */
public class Detective extends Character {

    public Detective(){
        characterName = CharacterName.Detective;
        action = Action.ENQUIRY;
        group = Group.Citizen;
    }

}
