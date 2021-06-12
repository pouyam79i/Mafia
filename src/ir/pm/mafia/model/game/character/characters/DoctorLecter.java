package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Action;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Doctor Lecter
 */
public class DoctorLecter extends Mafia{

    public DoctorLecter(){
        characterName = CharacterName.Doctor_Lecter;
        action = Action.REVIVE;
        group = Group.Mafia;
        isHeadOfMafia = false;
    }

}
