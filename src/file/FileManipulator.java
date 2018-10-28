package file;

import java.io.*;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows retrieval and storage of data in the standard hierarchy of data files.
 * Any interaction with files should be done through this class's static
 * methods.
 *
 * @author Japhez
 */
public enum FileManipulator {
    ;

    /**
     * Writes the passed object to the passed filename on the passed path.
     *
     * @param objectToWrite Object to be written
     * @param path File pathname
     * @param fileName Name of file
     */
    public static void writeObject(Object objectToWrite, String path, String fileName) {
        //Create the path if it doesn't exist
        new File(path).mkdirs();
        File file = new File(path + fileName);

        try (ObjectOutputStream oStream =
                     new ObjectOutputStream(new FileOutputStream(file))) {

            oStream.writeObject(objectToWrite);

            file.createNewFile();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManipulator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            Logger.getLogger(FileManipulator.class.getName()).log(Level.SEVERE,"Issue with IO", e);
        }
    }

    public static void main(String[] args) {
        writeObject("bob", "", "bob.txt");
    }

    /**
     * Attempts to read in the object from the passed path and file name, and
     * then returns that object.
     *
     * @param path Path to file
     * @param fileName Name of file
     * @return the read in object
     */
    public static Object readObject(String path, String fileName) {

            Object result;
            try (ObjectInputStream iStream = new ObjectInputStream(new FileInputStream(MessageFormat.format("{0}{1}", path, fileName)))) {
                result = iStream.readObject();
                return result;


        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileManipulator.class.getName()).log(Level.SEVERE, "Class not found", ex);
        } catch (FileNotFoundException e) {
                Logger.getLogger(FileManipulator.class.getName()).log(Level.SEVERE, "File not found", e);
            } catch (IOException e) {
                Logger.getLogger(FileManipulator.class.getName()).log(Level.SEVERE, "Problem with IO", e);
            }
        return null;
    }

    public static boolean isFileExist(String path, String fileName) {
        File file = new File(path + fileName);
        return file.exists();
    }

    /**
     * Retrieves an array of all files in the passed directory, or null if none
     * are found.
     *
     * @param path Path to file
     * @return a array of files if any exist, else null
     */
    // removed method getFiles() as reason for it being here is not clear

}