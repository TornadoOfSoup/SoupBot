package soup.memebot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by RPGenius on 8/7/2017.
 */


public class SaveUserStats {
    static Writer writer;
    static Gson gson;

    public SaveUserStats() throws IOException {
    }

    static void saveStats(UserStats player) throws IOException {
        writer = new FileWriter(new File(Utils.getPathOfResourcesFolder() + "/stats/" + player.id + ".json"));
        gson = new GsonBuilder().create();

        gson.toJson(player, writer);
        writer.flush();
        writer.close();
    }

}
