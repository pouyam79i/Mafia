package ir.pm.mafia.model.game.character;

import ir.pm.mafia.model.game.character.action.Action;

/**
 * This class contains main structure of
 * existing characters.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1.1
 */
public abstract class Character {

    /**
     * Character name
     */
    protected CharacterName characterName;
    /**
     * Character actions
     */
    protected Action action;
    /**
     * Group of Character
     */
    protected Group group;
    /**
     * Number if lives
     */
    protected int lives;
    /**
     * Tells if a character is still alive,
     * but can be revived!
     */
    protected boolean alive;
    /**
     * It means completely dead, cant be revived!
     */
    protected boolean passedAway;

    public Character(){
        lives = 1;
        alive = true;
        passedAway = false;
    }

    /**
     * When a player has been shot by other players.
     */
    public void gottenShot(){
        if(!alive)
            return;
        lives--;
        if(lives < 1)
            alive = false;
    }

    /**
     * When a player is going to be executed.
     */
    public void gottenExecuted(){
        lives = 0;
        alive = false;
        passedAway = true;
    }

    /**
     * When a player is revived by a doctor.
     */
    public boolean revive(){
        if(!passedAway){
            if(!alive){
                alive = true;
                lives = 1;
                return true;
            }
        }
        return false;
    }

    /**
     * Call this at the end of applying all actions!
     * @return boolean
     */
    public boolean checkPassedAway(){
        if(!alive)
            passedAway = true;
        return passedAway;
    }

    @Override
    public String toString(){
        if(characterName == null)
            return "No character is set!";
        String name = characterName.toString();
        if(name.contains("_")){
            name = name.replace("_", " ");
        }
        return name;
    }

    // Getters
    public Action getAction() {
        return action;
    }
    public Group getGroup() {
        return group;
    }
    public boolean isAlive() {
        return alive;
    }
    public CharacterName getCharacterName() {
        return characterName;
    }

}
