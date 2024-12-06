
/**
 * @author Caden Finley
 */
public abstract class Engine {

    public static boolean TESTING = false;
    private static String OpenAI_API_KEY = "sk-z3q9L-Lh39YYmooGmbPNAFlsaDywlFdRB-O1vFB4mYT3BlbkFJSSn6Um-zBw4r7fUB2H6dX3fhiOisNo8PFzy-fdKXwA";

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOpenAI_API_KEY() {
        return OpenAI_API_KEY;
    }
}
