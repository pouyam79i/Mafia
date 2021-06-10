package ir.pm.mafia.model.game.character;

import ir.pm.mafia.model.game.character.characters.*;

/**
 * This class builds character for us! (Contains Factory Method)
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.0
 */
public class CharacterFactory {

    /**
     * Number of mafia that has been build!
     */
    private int mafia;
    /**
     * Number of citizen that has been build!
     */
    private int citizen;

    /**
     * Constructor of CharacterFactory
     * Sets built character to zero!
     */
    public CharacterFactory(){
        mafia = 0;
        citizen = 0;
    }

    /**
     * Factory method to build character
     * @param group of wanted characters!
     * @return Character
     */
    public Character buildCharacter(Group group){
        Character character = null;
        // building for mafia group
        if(group == Group.Citizen){
            character = switch (mafia) {
                case 0 -> new Godfather();
                case 1 -> new DoctorLecter();
                default -> new NormalMafia();
            };
            mafia++;
        }
        // building for citizen group
        else if(group == Group.Mafia){
            character = switch (citizen) {
                case 0 -> new Mayer();
                case 1 -> new DoctorCitizen();
                case 2 -> new Sniper();
                case 3 -> new Detective();
                case 4 -> new Psychologist();
                case 5 -> new Impregnable();
                default -> new NormalCitizen();
            };
            citizen++;
        }
        return character;
    }

    /**
     * Resets built character number!
     */
    public void reset(){
        mafia = 0;
        citizen = 0;
    }

    // Getters!
    public int getMafia() {
        return mafia;
    }
    public int getCitizen() {
        return citizen;
    }

}
