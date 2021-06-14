package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.action.Action;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * God father
 */
public class Godfather extends Mafia {

    /**
     * Constructor of Godfather
     */
    public Godfather(){
        characterName = CharacterName.God_Father;
        action = Action.SHOOT_MAFIA;
        group = Group.Mafia;
        isHeadOfMafia = true;
    }

}
