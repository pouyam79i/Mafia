package ir.pm.mafia.model.game.character.characters;

import ir.pm.mafia.model.game.character.Character;

/**
 * Mafia characters have some more field and methods
 */
public abstract class Mafia extends Character {

    /**
     * Head of mafia is the one,
     * who decide to kill other player.
     */
    protected boolean isHeadOfMafia;

    // Setters
    public void setHeadOfMafia(boolean headOfMafia) {
        isHeadOfMafia = headOfMafia;
    }
    // Getters
    public boolean isHeadOfMafia(){
        return isHeadOfMafia;
    }

}
