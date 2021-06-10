package ir.pm.mafia.model.game.state;

import ir.pm.mafia.model.utils.multithreading.Runnable;

/**
 * This class sets game state according to the timer they got!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.0.1
 */
public class StateUpdater extends Runnable {

    /**
     * Contains current state of game
     */
    private State currentState;
    /**
     * Tells if game has started!
     */
    private boolean gameStarted;
    /**
     * Tells if game has ended ---> show winner!
     */
    private boolean gameFinished;
    /**
     * Day time amount
     */
    private int dayTimer;
    /**
     * Vote time amount
     */
    private int voteTimer;
    /**
     * Night time amount
     */
    private int nightTimer;

    /**
     * Constructor of StateUpdater.
     * Setups requirements!
     */
    public StateUpdater(){
        currentState =  State.Lobby;
        dayTimer = 120;              // 2 mints
        voteTimer = 60;              // 1 mints
        nightTimer = 120;            // 2 mints
        gameStarted = false;
        gameFinished = false;
    }

    /**
     * runs state updater
     * updates state of game!
     */
    @Override
    public void run() {
        currentState = State.Lobby;
        while (!gameStarted) Thread.onSpinWait();
        try {
            // Waiting for other threads to prepare!
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        while (!finished){
            currentState = State.Day;
            for(int i = 0; i < dayTimer; i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            currentState = State.Vote;
            for(int i = 0; i < voteTimer; i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            currentState = State.Night;
            for(int i = 0; i < nightTimer; i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            if(gameFinished){
                currentState = State.FINISHED;
                this.shutdown();
                break;              // Just in case :)
            }
        }
    }

    // Setters
    public void setGameStarted(boolean gameStarted) {
        if(this.gameStarted)
            return;
        this.gameStarted = gameStarted;
    }
    public void setGameFinished(boolean gameFinished) {
        if(!gameStarted)
            return;
        this.gameFinished = gameFinished;
    }
    public boolean setDayTimer(int dayTimer) {
        if(gameStarted)
            return false;
        if(dayTimer >= 60 && dayTimer <= 300){
            this.dayTimer = dayTimer;
            return true;
        }
        else
            return false;
    }
    public boolean setVoteTimer(int voteTimer) {
        if(gameStarted)
            return false;
        if(voteTimer >= 30 && voteTimer <= 120){
            this.voteTimer = voteTimer;
            return true;
        }
        else
            return false;
    }
    public boolean setNightTimer(int nightTimer) {
        if(gameStarted)
            return false;
        if(dayTimer >= 60 && dayTimer <= 180){
            this.nightTimer = nightTimer;
            return true;
        }
        else
            return false;
    }

    // Getters
    public State getCurrentState() {
        return currentState;
    }

}
