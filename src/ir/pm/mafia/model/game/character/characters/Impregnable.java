package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.action.Action;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;

/**
 * Impregnable (Hard Life)
 */
public class Impregnable extends Character {

    /**
     * Number of dead enquiry you can use!
     */
    private int leftDeadEnquiry;

    /**
     * Constructor of Impregnable
     */
    public Impregnable(){
        super();
        characterName = CharacterName.Impregnable;
        action = Action.ENQUIRY_DEAD;
        group = Group.Citizen;
        lives = 2;
        leftDeadEnquiry = 2;
    }

    /**
     * Use dead enquiry to know dead characters!
     * @return true if you can use it!
     */
    public boolean useDeadEnquiry(){
        if(leftDeadEnquiry > 0){
            leftDeadEnquiry--;
            return true;
        }
        return false;
    }

}
