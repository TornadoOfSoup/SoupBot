package soup.memebot;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;

import java.io.File;
import java.util.concurrent.Future;

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

    public static void levelUpDialog(Channel channel, User user, UserStats userStats) {
        userStats.addSkillPoints(5);
        Future<Message> messageFuture = channel.sendMessage("User " + user.getName() + " has levelled up!\n" +
                "```\n" +
                "lvl " + (userStats.getLevel() - 1) + " → lvl " + userStats.getLevel() + "\n" +
                "+ 5 skill points\n" +
                "```");
        while (!messageFuture.isDone()) {}
    }
}
