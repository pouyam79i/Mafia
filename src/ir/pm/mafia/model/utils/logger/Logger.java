package ir.pm.mafia.model.utils.logger;

import ir.pm.mafia.view.console.Console;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This is the logger class.
 * write logs of program into the './log/'. ( This is my default :) )
 * Instantiating is not allowed.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.2
 */
public class Logger {

    /**
     * Directory of log files
     */
    private static final String ROOT_DIR = "./log/";
    /**
     * This file is used to gather logs
     * Problems that does not make major problem!
     */
    private static final String LOG_FILE = "log.txt";
    /**
     * This file is used to gather errors
     * Problems that are important!
     */
    private static final String ERROR_FILE = "error.txt";
    /**
     * If a log or error is not saved in the related files,
     * it will be added to this list.
     * At the end of program, this list will be printed!
     */
    private static final ArrayList<String> unsavedLogs = new ArrayList<String>();
    /**
     * When debug is true mode we print logs and errors!
     */
    private static boolean debugMode = true;

    /**
     * Constructor of logger
     * Instantiating is not allowed
     */
    private Logger(){}

    /**
     * Logging problems(not major ones) information into log file.
     * @param data is data of log
     * @param level is level of log (or problem)
     * @param className is where it happened
     */
    public static synchronized void log(String data, LogLevel level, String className){
        if(!debugMode)
            return;
        data = buildMessage(data, level.toString(), className, false);
        addToEndFoFile(data, ROOT_DIR + LOG_FILE);
    }

    /**
     * Logging errors information into error file.
     * @param data is data of error
     * @param level is level of error
     * @param className is where it happened
     */
    public static synchronized void error(String data, LogLevel level, String className){
        if(!debugMode)
            return;
        data = buildMessage(data, level.toString(), className, true);
        addToEndFoFile(data, ROOT_DIR + ERROR_FILE);
    }

    /**
     * Prints all unsaved logs or errors!
     * In case that appending logs or errors to their related file fails!
     */
    public static synchronized void printAllLostLogs(){
        if(!debugMode)
            return;
        Console console = Console.getConsole();
        for (String message : unsavedLogs){
            console.println(message);
        }
    }

    /**
     * Build A well formed message of log or error
     * @param data of log/error
     * @param level of log/error
     * @param className of log/error
     * @param isError if true it builds an error message, else build a message for log
     * @return well formed message of log/error
     */
    private static synchronized String buildMessage(String data, String level, String className, boolean isError){
        try{
            if(data == null)
                data = "Empty";
            if(level == null)
                level = "Unspecific";
            if(className == null)
                className = "Undefined";
            String message = "";
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            message += ",{\n";
            message += "    \"Location\" : \"" + className + "\",\n";
            if(isError)
                message += "    \"ErrorLevel\" : \"" + level + "\",\n";
            else
                message += "    \"LogLevel\" : \"" + level + "\",\n";
            message += "    \"Date\" : \"" + formatter.format(date) + "\",\n";
            message += "    \"Info\" : \"" + data + "\"\n";
            message += "}";
            return message;
        }catch (Exception e){
            return "#### Failed to build log/error message!\n* Message of Error: " + e.getMessage();
        }
    }

    /**
     * Append the data to a file.
     * @param data will be appended to file.
     * @param address of file.
     */
    private static synchronized void addToEndFoFile(String data, String address){
        try (FileWriter fileWriter = new FileWriter(address, true)){
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            printWriter.println(data);
            printWriter.flush();
            printWriter.close();
            bufferedWriter.close();
            fileWriter.close();
        }catch (IOException ignored){
            unsavedLogs.add("#### Failed to append log/error message!\n" +
                    "File Address: " + address + ",\n" + "Data: " + data);
        }
    }

}
