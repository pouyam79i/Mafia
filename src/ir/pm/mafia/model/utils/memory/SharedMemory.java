package ir.pm.mafia.model.utils.memory;

import java.util.LinkedList;

/**
 * This class contains the structure of shared memory!
 * Used between to transfer data between threads!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.1
 */
public class SharedMemory {

    /**
     * delete modes define the way of getting data from memory
     * if true, data will be removed after getting it!
     * if false, it reads last-in data
     */
    private final boolean deleteMode;
    /**
     * This is the shared memory structure!
     * Which is a LinkedList at this moment!
     */
    private final LinkedList<Object> boxes;
    private int lastReadIndex;

    /**
     * Constructor of SharedMemory.
     * Setup the box of memory.
     * @param deleteMode will be set as delete mode of shared memory
     */
    public SharedMemory(boolean deleteMode){
        // Constructing a linked list!
        boxes = new LinkedList<Object>();
        this.deleteMode = deleteMode;
        lastReadIndex = 0;
    }

    /**
     * This method helps to get data from shared memory,
     * in a synchronized way.
     * It reads from the first-in data!
     * If delete mode is true:
     * after reading DataBox, it will delete it from shared memory!
     * Else:
     * it just reads new data and returns new data to user!
     * @return DataBox
     */
    public synchronized Object get(){
        Object dataBox = null;
        if(boxes.size() > lastReadIndex)
            if(deleteMode)
                dataBox = boxes.remove(0);
            else{
                dataBox = boxes.get(lastReadIndex);
                lastReadIndex++;
            }
        return dataBox;
    }

    /**
     * This method helps to put (append) data into shared memory,
     * in a synchronized way.
     * It adds new data as last data.
     * @param newBox will be added.
     */
    public synchronized void put(Object newBox){
        if(newBox != null)
            boxes.add(newBox);
    }

}
