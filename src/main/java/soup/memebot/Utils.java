package soup.memebot;

import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

        return file.exists();
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

    public static String wordToProperCase(String word) {
        word = word.toLowerCase();
        char[] chars = word.toCharArray();

        chars[0] = Character.toUpperCase(chars[0]);
        return String.valueOf(chars);
    }

    public static String toProperCase(String string) {
        String[] words = string.split(" ");
        String returnString = "";
        for (String word : words) {
            returnString += wordToProperCase(word) + " ";
        }
        returnString = returnString.trim();
        return returnString;
    }

    public static String getTimestampFull() {
        Date date = new Date();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd | HH:mm:ss").format(date);
        timeStamp = "[" + timeStamp + "]";
        return timeStamp;
    }

    public static String getTimestampForFileName() {
        Date date = new Date();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(date);
        return timeStamp;
    }

    public static String getTimestampTime() {
        Date date = new Date();
        String timeStamp = new SimpleDateFormat("HH:mm").format(date);
        return timeStamp;
    }

    public static String formatMillisAsString(long l)
    {
        long day = TimeUnit.MILLISECONDS.toDays(l);
        long hr = TimeUnit.MILLISECONDS.toHours(l - TimeUnit.DAYS.toMillis(day));
        long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.DAYS.toMillis(day) - TimeUnit.HOURS.toMillis(hr));
        long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.DAYS.toMillis(day) - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.DAYS.toMillis(day) - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));

        StringBuilder builder = new StringBuilder();
        int nonsecondUnits = 0; //for incremental calculations
        int nonzeroUnits = 0;

        if (day > 0) nonzeroUnits++;
        if (hr > 0) nonzeroUnits++;
        if (min > 0) nonzeroUnits++;
        if (sec > 0) nonzeroUnits++;

        if (day > 0) {
            builder.append(day + " days");
            if (nonzeroUnits > 3) {
                builder.append(", ");
            }
            nonsecondUnits++;
        }
        if (hr > 0) {
            builder.append(hr + " hours");
            if (nonzeroUnits > 1) builder.append(", "); else {
                builder.append(" ");
            }
            nonsecondUnits++;
        }
        if (min > 0) {
            builder.append(min + " minutes");
            if (nonsecondUnits > 0) builder.append(", "); else {
                builder.append(" ");
            }
            nonsecondUnits++;
        }
        if (nonsecondUnits > 1) builder.append("and ");
        builder.append(sec + "." + ms + " seconds");


        return builder.toString();
    }

}
