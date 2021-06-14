package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.action.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Psychologist
 */
public class Psychologist extends Character {

    /**
     * Constructor of Psychologist
     */
    public Psychologist(){
        characterName = CharacterName.Psychologist;
        action = Action.SILENCE;
        group = Group.Citizen;
    }

}
