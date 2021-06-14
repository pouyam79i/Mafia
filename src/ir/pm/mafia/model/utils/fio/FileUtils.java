package ir.pm.mafia.model.utils.fio;

import ir.pm.mafia.controller.data.DataBase;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.model.utils.multithreading.Runnable;
import ir.pm.mafia.view.console.Color;

import java.io.*;
import java.util.UUID;

/**
 * Class of FileUtils
 * This contains function which handles io of this application
 * @author Pouya Mohammadi - Uni ID: 9829039
 * @version 1.0
 */
public class FileUtils extends Runnable {

    /**
     * Only instance of file paters
     */
    private static FileUtils fileUtils = null;

    // File information
    private final String FILENAME;
    private final String FORMAT;
    private final String DIR = "./log/";
    // File Output tools
    private FileWriter fileWriter = null;
    private BufferedWriter bufferedWriter = null;
    private PrintWriter printWriter = null;

    /**
     * Shared memory to read received memory
     */
    private final DataBase shareInputSaver;
    /**
     * last read from data base
     */
    private int lastRead = 0;
    /**
     * Used to hold the thread and read all previous data
     */
    private volatile boolean onHold;

    /**
     * Constructor of FileUtils
     * Sets requirements
     * @param FILENAME name of file
     * @param FORMAT format of file
     * @throws Exception if code not set it right!
     */
    private FileUtils(String FILENAME, String FORMAT) throws Exception {
        if(FILENAME == null)
            throw new Exception("Null name");
        if(FORMAT == null)
            FORMAT = "txt";
        this.FILENAME = FILENAME;
        this.FORMAT = FORMAT;
        shareInputSaver = new DataBase();
        initial();
        onHold = false;
    }

    // Append text to save!
    public void addToReaderBox(Message message){
        if(message == null)
            return;
        if(message.getMessageText() == null || message.getSenderName() == null)
            return;
        String text = message.getSenderName() + ": " + message.getMessageText();
        shareInputSaver.add(text);
    }

    /**
     * Holds the writing process and the print all previous data,
     * When it ended
     */
    public void printPreviousMessages(){
        onHold = true;
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
        try {
            closeFileUtils();
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(
                        DIR + FILENAME + "." + FORMAT));
                String line = reader.readLine();
                while (line != null) {
                    System.out.println(Color.PURPLE_BOLD + line);
                    // read next line
                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                Logger.log("Failed while reading line by line!" + e.getMessage(),
                        LogLevel.IOException, "FileUtils");
            }
            try {
                initial();
            } catch (IOException e) {
                Logger.log("Failed to rebuild the file writer tools" + e.getMessage(),
                        LogLevel.IOException, "FileUtils");
            }
        }catch (Exception e){
            Logger.log("Failed to print previous messages" + e.getMessage(),
                    LogLevel.IOException, "FileUtils");
        }
        onHold = false;
    }

    /**
     * Runs the process of appending
     */
    @Override
    public void run() {
        try {
            finished = false;
            initial();
            lastRead = 0;
            String text = null;
            while (!finished){
                while (onHold) Thread.onSpinWait();
                try {
                    if(lastRead >= shareInputSaver.getSize())
                        continue;
                    text = (String) shareInputSaver.readData(lastRead);
                    if(text == null)
                        continue;
                    if(text.replace(" ", "").equals(""))
                        continue;
                    lastRead++;
                    printWriter.println(text);
                    printWriter.flush();
                }catch (Exception ignored){}
            }
        } catch (IOException ignored) {}
        this.shutdown();
    }

    /**
     * Shutdown call
     */
    @Override
    public void shutdown(){
        finished = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        this.close();
        closeFileUtils();
    }

    /**
     * Build required tools
     * @throws IOException if failed to build proper tools
     */
    private void initial() throws IOException {
        if(fileWriter == null && bufferedWriter == null && printWriter == null){
            fileWriter = new FileWriter(DIR + FILENAME + "." + FORMAT, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            printWriter = new PrintWriter(bufferedWriter);
        }
    }

    /**
     * Closes the file property
     */
    private void closeFileUtils(){
        try {
            if(printWriter != null)
                printWriter.close();
            if(bufferedWriter != null)
                bufferedWriter.close();
            if(fileWriter != null)
                fileWriter.close();
            fileWriter = null;
            bufferedWriter = null;
            printWriter = null;
        }catch (Exception e){
            Logger.error("Failed to shutdown the file utils" + e.getMessage(),
                    LogLevel.IOException, "FileUtils");
        }
    }

    //Getters
    /**
     * Get instance of this
     * @return FileUtils
     */
    public static FileUtils getFileUtils() {
        if(fileUtils == null){
            try {
                fileUtils = new FileUtils(UUID.randomUUID().toString(), "txt");
            } catch (Exception e) {
                fileUtils = null;
            }
        }
        return fileUtils;
    }

}
