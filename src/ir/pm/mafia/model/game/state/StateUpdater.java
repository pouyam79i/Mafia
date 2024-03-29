package ir.pm.mafia.model.game.state;

import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;

/**
 * This class sets game state according to the timer they got!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.0.4
 */
public class StateUpdater extends Runnable {

    /**
     * Contains current state of game
     */
    private volatile State currentState;
    /**
     * Tells if game has started!
     */
    private volatile boolean gameStarted;
    /**
     * Tells if game has ended ---> show winner!
     */
    private volatile boolean gameFinished;
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
     * go forward!
     */
    private volatile boolean advance;

    /**
     * Constructor of StateUpdater.
     * Setups requirements!
     */
    public StateUpdater(){
        currentState =  State.Lobby;
        dayTimer = 300;              // 5 mints
        voteTimer = 60;              // 1 mints
        nightTimer = 60;            // 1 mints
        gameStarted = false;
        gameFinished = false;
        advance = false;
        threadName = "StateUpdater";
    }

    public void advance(){
        advance = true;
    }

    /**
     * runs state updater
     * updates state of game!
     */
    @Override
    public void run() {
        // waiting in lobby
        currentState = State.Lobby;
        while (!gameStarted) Thread.onSpinWait();
        Logger.log("Time of lobby finished!", LogLevel.Report, "StateUpdater");
        // calling to start
        currentState = State.STARTED;
        // Waiting for other threads to prepare!
        while (!advance) Thread.onSpinWait();
        advance = false;
        Logger.log("Starting finished!", LogLevel.Report, "StateUpdater");
        // Starting the game!
        while (!finished){
            currentState = State.Night;
            for(int i = 0; i < nightTimer; i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            // Hold before next state
            currentState = State.CHECK;
            Logger.log("Time of night finished!", LogLevel.Report, "StateUpdater");
            while (!advance) Thread.onSpinWait();
            advance = false;
            if(gameFinished){
                currentState = State.FINISHED;
                this.shutdown();
                break;              // Just in case :)
            }
            Logger.log("Time of check finished!", LogLevel.Report, "StateUpdater");
            // Hold before next state
            currentState = State.Day;
            for(int i = 0; i < dayTimer; i++){
                try {
                    if(advance){
                        advance = false;
                        break;
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            Logger.log("Time of day finished!", LogLevel.Report, "StateUpdater");
            // Hold before next state
            currentState = State.Vote;
            while (!advance) Thread.onSpinWait();
            advance = false;
            for(int i = 0; i < voteTimer; i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
            Logger.log("Time of vote finished!", LogLevel.Report, "StateUpdater");
            currentState = State.VoteEnded;
            while (!advance) Thread.onSpinWait();
            advance = false;
            Logger.log("end of loop!", LogLevel.Report, "StateUpdater");
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
        if(gameFinished)
            this.gameFinished = true;
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
