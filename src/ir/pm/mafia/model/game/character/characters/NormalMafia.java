package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Action;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Normal mafia
 */
public class NormalMafia extends Mafia {

    public NormalMafia(){
        characterName = CharacterName.Normal_Mafia;
        action = Action.EMPTY;
        group = Group.Mafia;
        isHeadOfMafia = false;
    }

}
