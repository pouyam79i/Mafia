package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Action;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * God father
 */
public class Godfather extends Mafia {

    public Godfather(){
        characterName = CharacterName.GodFather;
        action = Action.SHOOT;
        group = Group.Mafia;
        isHeadOfMafia = true;
    }

}
