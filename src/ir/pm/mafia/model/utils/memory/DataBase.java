package ir.pm.mafia.model.utils.memory;

import java.util.LinkedList;

/**
 * This is a data base!
 * Storing data in data base will allow other threads to read new data.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class DataBase {

    /**
     * Data base -  memory structure is linked list
     */
    private final LinkedList<Object> dataBase;
    /**
     * size of data base
     */
    private int size;

    /**
     * Constructor of DataBase
     * Setups requirements
     */
    public DataBase(){
        size = 0;
        dataBase = new LinkedList<Object>();
    }

    /**
     * This method adds a new object to data base
     * @param newData will be appended to data base
     */
    public synchronized void add(Object newData){
        if(newData == null)
            return;
        dataBase.add(newData);
        size++;
    }

    /**
     * reads a data from data base
     * @param index of data
     * @return object
     */
    public synchronized Object readData(int index){
        Object data = null;
        if(index >= 0 && index < size){
            data = dataBase.get(index);
        }
        return data;
    }

    /**
     * gets the current size of data
     * @return size
     */
    public synchronized int getSize() {
        return size;
    }

}
