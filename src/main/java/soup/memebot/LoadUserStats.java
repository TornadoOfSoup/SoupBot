package soup.memebot;

import com.google.gson.Gson;
import de.btobastian.javacord.entities.User;

import java.io.*;


/**
 * Created by RPGenius on 8/7/2017.
 */


public class LoadUserStats {
    static Reader reader;
    static Gson gson;

    public LoadUserStats() {

    }

    public static UserStats loadStats(User player) throws IOException {
        File file = new File(Utils.getPathOfResourcesFolder() + "/stats/" + player.getId() + ".json");
        if (file.exists()) {
            reader = new FileReader(file);
            UserStats userStats = null;

            gson = new Gson();
            /*gson = new GsonBuilder().registerTypeAdapter(Species.class, new InterfaceAdapter<Species>())
                    .registerTypeAdapter(Breed.class, new InterfaceAdapter<Breed>()).create();*/

            while (userStats == null) {
                if (reader.ready()) {
                    userStats = gson.fromJson(reader, UserStats.class);
                }
            }
            return userStats;
        } else {
            UserStats userStats = new UserStats(player.getId());
            return userStats;
        }
    }

    public static UserStats loadStats(String playerID) throws IOException {
        File file = new File(Utils.getPathOfResourcesFolder() + "/stats/" + playerID + ".json");
        if (file.exists()) {
            reader = new FileReader(file);
            UserStats userStats = null;

            gson = new Gson();
            /*gson = new GsonBuilder().registerTypeAdapter(Species.class, new InterfaceAdapter<Species>())
                    .registerTypeAdapter(Breed.class, new InterfaceAdapter<Breed>()).create();*/

            while (userStats == null) {
                if (reader.ready()) {
                    userStats = gson.fromJson(reader, UserStats.class);
                }
            }
            return userStats;
        } else {
            UserStats userStats = new UserStats(playerID);
            return userStats;
        }
    }

}
