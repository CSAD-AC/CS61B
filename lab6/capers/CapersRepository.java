package capers;

import java.io.File;
import java.io.IOException;

import static capers.Utils.*;

/** A repository for Capers 
 * @author 逐辰
 * The structure of a Capers Repository is as follows:
 *
 * .capers/ -- top level folder for all persistent data in your lab12 folder
 *    - dogsFile/ -- folder containing all of the persistent data for dogsFile
 *    - story -- file containing the current story
 *
 * TODO: change the above structure if you do something different.
 */
public class CapersRepository {
    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    static final File CAPERS_FOLDER = join(".capers"); // TODO Hint: look at the `join`
                                            //      function in Utils

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     * Remember: recommended structure (you do not have to follow):
     *
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     *    - dogsFile/ -- folder containing all of the persistent data for dogsFile
     *    - story -- file containing the current story
     */
    public static void setupPersistence() {

    }

    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text) {
        if(!CAPERS_FOLDER.exists()) CAPERS_FOLDER.mkdir();
        File story = join(CAPERS_FOLDER,"story");
        if(!story.exists()) {
            try {
                story.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String storyText = readContentsAsString(story);
        storyText += text + "\n";
        writeContents(story, storyText);
        System.out.println(storyText);

    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) {
        if(!CAPERS_FOLDER.exists()) CAPERS_FOLDER.mkdir();
        File dogsFile = join(CAPERS_FOLDER,"dogs");
        if(!dogsFile.isDirectory()) dogsFile.mkdir();
        Dog dog = new Dog(name, breed, age);
        dog.saveDog();
        System.out.println(dog.toString());


    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) {
        File dogs = join(CAPERS_FOLDER,"dogs");
        Dog dog = Dog.fromFile(name);
        dog.haveBirthday();
        writeObject(join(dogs, name), dog);
    }
}
