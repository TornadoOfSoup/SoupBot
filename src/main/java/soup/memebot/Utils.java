package soup.memebot;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;

import java.io.File;
import java.util.Random;
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
        Random rand = new Random();
        double randomFactor = (((double) rand.nextInt(350)) / 1000) - 0.1; //random number between -0.1 and 0.3

        double level = userStats.getLevel() - 1;

        double potentiorbs = 200 * (1 + (level / 10)) * (1 + randomFactor);
        int intPotentiorbs = (int) Math.round(potentiorbs);

        System.out.println("Potentiorbs: 200 * " + (1 + (level / 10)) + " * " + (1 + randomFactor) + " = " + potentiorbs);

        userStats.addPotentiorbs(intPotentiorbs);
        Future<Message> messageFuture = channel.sendMessage("User " + user.getName() + " has levelled up!\n" +
                "```\n" +
                "lvl " + (userStats.getLevel() - 1) + " → lvl " + userStats.getLevel() + "\n" +
                "+" + intPotentiorbs + " potentiorbs\n" +
                "```");
        while (!messageFuture.isDone()) {}
    }

    public static int plusMinus(int factor) {
        Random rand = new Random();
        int varyingFactor = rand.nextInt((factor*2) + 1) - factor;
        return varyingFactor;
    }
}
