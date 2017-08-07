package soup.memebot;

import com.google.gson.Gson;
import de.btobastian.javacord.entities.User;

import javax.jws.soap.SOAPBinding;
import java.io.*;


/**
 * Created by RPGenius on 8/7/2017.
 */


public class LoadUserStats {
    static Reader reader;
    static Gson gson;

    public static UserStats loadStats(User player) throws IOException {
        reader = new FileReader(new File(Utils.getPathOfResourcesFolder() + "/stats/" + player.getId() + ".json"));
        gson = new Gson();
        UserStats userStats = null;

        while (userStats == null) {
            if (reader.ready()) {
                userStats = gson.fromJson(reader, UserStats.class);
            }
        }
        return userStats;
    }

    public static UserStats loadStats(String playerID) throws IOException {
        reader = new FileReader(new File(Utils.getPathOfResourcesFolder() + "/stats/" + playerID + ".json"));
        gson = new Gson();
        UserStats userStats = null;

        while (userStats == null) {
            if (reader.ready()) {
                userStats = gson.fromJson(reader, UserStats.class);
            }
        }
        return userStats;
    }

}
