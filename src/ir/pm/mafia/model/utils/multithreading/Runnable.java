package ir.pm.mafia.model.utils.multithreading;

/**
 * This class contains the structure of Runnable classes!
 * It can run a class as a new thread!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.3
 */
public abstract class Runnable implements java.lang.Runnable{

    /**
     * contains the state of thread loop.
     * if false, thread is still running.
     * if true, the process of finishing begin!
     */
    protected volatile boolean finished;
    /**
     * contains this thread
     */
    private Thread thisThread;
    /**
     * Running state of thread
     */
    private boolean runningState;
    /**
     * Name of thread!
     */
    protected String threadName = "EMPTY";

    /**
     * Constructor of Runnable.
     * Setup required fields!
     */
    protected Runnable(){
        finished = false;
        runningState = false;
        thisThread = null;
    }

    /**
     * Starts a thread to run this object run method!
     */
    public synchronized void start(){
        finished = false;
        if(!runningState){
            thisThread = new Thread(this);
            runningState = true;
            thisThread.start();
        }
    }

    /**
     * Tells the thread to begin the process of killing it self!
     */
    protected synchronized void close(){
        if (runningState){
            thisThread.interrupt();
            thisThread.stop();
            try {
                thisThread.join(50);
            } catch (InterruptedException ignored) {}
        }
        thisThread = null;
        finished = true;
        runningState = false;
    }

    /**
     * Call for shutdown!
     * override this method to have safer shutdown!
     */
    public synchronized void shutdown(){
        finished = true;
        this.close();
    }

    /**
     * Tells if the thread is still running or not
     * @return running state!
     */
    public synchronized boolean isRunning() {
        return runningState;
    }

}
