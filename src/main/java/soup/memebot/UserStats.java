package soup.memebot;

import de.btobastian.javacord.entities.User;

import java.io.IOException;

/**
 * Created by RPGenius on 8/7/2017.
 */
public class UserStats {
    String id;
    int exp;

    int playedGames1, wonGames1, wonTeamGames1;

    public UserStats(User player) {
        id = player.getId();
        exp = 0;

        playedGames1 = 0;
        wonGames1 = 0;
        wonTeamGames1 = 0;

        System.out.println("Created new UserStats object");

        try {
            SaveUserStats.saveStats(this);
            System.out.println("UserStats object creation for user " + player.getName() + " was successful.");
        } catch (IOException e) {
            System.out.println("ERROR: UserStats object creation for user " + player.getName() + " was unsuccessful.");
            e.printStackTrace();
        }
    }

    public UserStats(String userID) {
        id = userID;
        exp = 0;

        playedGames1 = 0;
        wonGames1 = 0;
        wonTeamGames1 = 0;

        try {
            SaveUserStats.saveStats(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addExp(int number) {
        this.exp += number;
        System.out.println("Added " + number + " exp to " + this.id);
        try {
            SaveUserStats.saveStats(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPlayedGames1(int number) {
        this.playedGames1 += number;
        try {
            SaveUserStats.saveStats(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addWonGames1(int number) {
        this.wonGames1 += number;
        try {
            SaveUserStats.saveStats(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addWonTeamGames1(int number) {
        this.wonTeamGames1 += number;
        try {
            SaveUserStats.saveStats(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setExp(int exp) {
        this.exp = exp;
        try {
            SaveUserStats.saveStats(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public int getExp() {
        return exp;
    }

    public int getPlayedGames1() {
        return playedGames1;
    }

    public int getWonGames1() {
        return wonGames1;
    }

    public int getWonTeamGames1() {
        return wonTeamGames1;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

