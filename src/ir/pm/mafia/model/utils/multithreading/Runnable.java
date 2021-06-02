package ir.pm.mafia.model.utils.multithreading;

/**
 * This class contains the structure of Runnable classes!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1
 */
public abstract class Runnable implements java.lang.Runnable{

    /**
     * contains the state of thread loop.
     * if false, thread is still running.
     * if true, the process of finishing begin!
     */
    protected volatile boolean finished;
    /**
     * contains the state of thread job.
     * if false, it means the job is not done.
     * if true, it means the job and thread is completely done!
     *
     */
    protected boolean done;

    /**
     * Constructor of Runnable.
     * Setup required fields!
     */
    protected Runnable(){
        finished = false;
        done = false;
    }

    /**
     * tells the thread to begin the process of finishing!
     */
    public synchronized void close(){
        finished = true;
    }

    /**
     * Tells if the thread is finished of not
     * if true it means the thread is finished!
     * @return finished state!
     */
    public synchronized boolean isDone() {
        return done;
    }

}
