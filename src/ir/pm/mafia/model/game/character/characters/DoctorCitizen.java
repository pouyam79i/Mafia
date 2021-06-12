package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Citizen's Doctor
 */
public class DoctorCitizen extends Character{

    public DoctorCitizen(){
        characterName = CharacterName.Doctor_Citizen;
        action = Action.REVIVE;
        group = Group.Citizen;
    }

}
