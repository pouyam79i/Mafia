package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.action.Action;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Doctor Lecter
 */
public class DoctorLecter extends Mafia{

    /**
     * Number of left self revive!
     * Doctor can revive himself only once!
     */
    private int selfRevive;

    /**
     * Constructor of DoctorLecter
     */
    public DoctorLecter(){
        characterName = CharacterName.Doctor_Lecter;
        action = Action.REVIVE_MAFIA;
        group = Group.Mafia;
        isHeadOfMafia = false;
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
