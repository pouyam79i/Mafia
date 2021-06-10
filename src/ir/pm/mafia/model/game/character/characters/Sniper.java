package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Sniper
 */
public class Sniper extends Character {

    public Sniper(){
        characterName = CharacterName.Sniper;
        action = Action.SHOOT;
        group = Group.Citizen;
    }

}
