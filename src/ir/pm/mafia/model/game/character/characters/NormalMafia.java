package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.action.Action;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Normal mafia
 */
public class NormalMafia extends Mafia {

    /**
     * Constructor of NormalMafia
     */
    public NormalMafia(){
        characterName = CharacterName.Normal_Mafia;
        action = Action.EMPTY;
        group = Group.Mafia;
        isHeadOfMafia = false;
    }

}
