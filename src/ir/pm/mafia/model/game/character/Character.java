package ir.pm.mafia.model.game.character;

/**
 * This class contains main structure of
 * existing characters.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
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
    public void gottenShoot(){
        if(!alive)
            return;
        lives--;
        if(lives <= 0)
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
    public void saveMyLife(){
        if(!passedAway){
            if(!alive){
                alive = true;
                lives = 1;
            }
        }
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

}
