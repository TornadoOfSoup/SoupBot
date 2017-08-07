package soup.memebot;

import java.io.File;

/**
 * Created by RPGenius on 8/7/2017.
 */
public class Utils {

    public static String getPathOfResourcesFolder() {
        String path = ClassLoader.getSystemResource("donotdelete").getPath().substring(0, ClassLoader.getSystemResource("donotdelete").getPath().lastIndexOf("donotdelete"));
        return path;
    }

    public static boolean statsFileExists(String playerID) {
        String path = ClassLoader.getSystemResource("donotdelete").getPath().substring(0, ClassLoader.getSystemResource("donotdelete").getPath().lastIndexOf("donotdelete"));
        File file = new File(path + "/stats/" + playerID + ".json");
        System.out.println("File path: " + file.getAbsolutePath());

        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
