package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.action.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Citizen's Doctor
 */
public class DoctorCitizen extends Character{

    /**
     * Number of left self revive!
     * Doctor can revive himself only once!
     */
    private int selfRevive;

    /**
     * Constructor of DoctorCitizen
     */
    public DoctorCitizen(){
        characterName = CharacterName.Doctor_Citizen;
        action = Action.REVIVE;
        group = Group.Citizen;
        selfRevive = 1;
    }

    /**
     * Revive my self if i have been shot!
     * @return true if could revive!
     */
    public boolean reviveMyself(){
        if(selfRevive > 0){
            selfRevive--;
            return true;
        }
        return false;
    }

}
