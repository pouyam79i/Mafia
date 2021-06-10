package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Psychologist
 */
public class Psychologist extends Character {

    public Psychologist(){
        characterName = CharacterName.Psychologist;
        action = Action.SILENCE;
        group = Group.Citizen;
    }

}
