package soup.memebot;

import com.google.common.util.concurrent.FutureCallback;
import com.samuelmaddock.strawpollwrapper.StrawPoll;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAttachment;
import de.btobastian.javacord.entities.message.MessageHistory;
import de.btobastian.javacord.entities.message.MessageReceiver;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import de.btobastian.javacord.*;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.Random;
import java.awt.TrayIcon.MessageType;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;

import de.btobastian.javacord.listener.user.UserChangeStatusListener;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static soup.memebot.LoadUserStats.loadStats;
import static soup.memebot.Utils.*;

public class memebot {

    static final ArrayList<String> commandList = new ArrayList<>(Arrays.asList("$help", "$memelist", "$meme", "$info", "$shutdown^", "$getavatar",
            "$silvertime", "$vote", "$upcoming", "$hypixel", "$tos", "$censor^", "$mode^", "$whitelist^^", "$prune^", "$convert", "$choose", "$bubblesort",
            "$math", "$mute^", "$unmute^", "$supermute^", "$quadratic", "$primeFactors", "$string", "$simplify", "$ascii", "$rng", "$factors", "$leetspeak",
            "$game", "$google", "$cat", "$onlineusers", "$addxp", "$setxp", "$addlevel", "$setlevel", "$getstats", "$setpotentiorbs", "$newpet", "$getpets",
            "$clearpets", "$addlogo", "$identify", "$tobinary", "$frombinary", "$makestorychannel^", "$printstory", "$viewstorylist", "$finishstory^",
            "$deleteword", "$viewrecentwords", "$clnew", "$cltoggle", "$roll", "$strawpoll", "$checkstrawpoll"));

    static TimedEventRunnable checkOnline = new TimedEventRunnable("CheckOnline", 60);

    static final ArrayList<String> whitelist = new ArrayList<>(Arrays.asList("TornadoOfSoup", "Kotamonn", "SoupBot"));
    static final ArrayList<String> promotedList = new ArrayList<>(Arrays.asList("Butterwhales", "Almurray155", "Meme"));
    static final ArrayList<String> unitTypeList = new ArrayList<>(Arrays.asList("temp", "weight", "length", "angle"));
    static final ArrayList<String> unitTempList = new ArrayList<>(Arrays.asList("C", "F", "K"));
    static final ArrayList<String> unitAngleList = new ArrayList<>(Arrays.asList("D", "R", "deg", "rad"));

    static boolean[] games = new boolean[] {false, false}; //0 = guessnumber, 1 = hangman
    static boolean failed = false;

    static int game0Num = 0;
    static int game0Turns = 0;
    static String game0UserID = "";

    static String game1Word = "";
    static int game1lives = 0;
    static int game1TurnNumber = 0;
    static int game1MaxLives = 0;
    static ArrayList<Character> game1GuessedWord = new ArrayList<>();
    static ArrayList<Character> game1ActualWord = new ArrayList<>();
    static ArrayList<String> game1HelpfulUsers = new ArrayList<>();
    static ArrayList<Character> game1UnusedChars = new ArrayList<>();

    static ArrayList<String> storyGameChannelIDs = new ArrayList<>();
    static HashMap<String, StringBuilder> storyGameStories = new HashMap<>();
    static HashMap<String, String> strawpolls = new HashMap<>();

    public static void main(String[] args) {
        String token = "";
        String password = "";

        try {
            ArrayList<String> admin = getArrayListOfLines("admin.pass");
            token = admin.get(0);
            password = admin.get(1);
            while(true) {
                String pass = JOptionPane.showInputDialog("Password:"); //checks for a password
                if (pass.equals(password)) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("admin.pass not found, an admin.pass file should have two lines. " +
                    "The first should contain a token, and the second should contain the bot password. It should be located in the resources folder.");
                token = JOptionPane.showInputDialog("Token:"); //takes token

        }

        final DiscordAPI api = Javacord.getApi(token, true);
        final int numOfCommands = 61;
        final int numOfSubCommands = 20;
        final String version = "1.8.4";
        final String complieDate = "3/31/18 14:44 EST";
        final String chatFilterVersion = "1.7";
        final boolean[] censor = {false};
        final long[] cooldown = {0, 0};
        final boolean[] modes = {false, false}; //0 = fastPictures, 1 = restricted


        new File(getPathOfResourcesFolder() + "/images").mkdir(); //create necessary directories
        new File(getPathOfResourcesFolder() + "/stats").mkdir();

        System.out.println("Logging in...");

            api.connect(new FutureCallback<DiscordAPI>() {
                @Override
                public void onSuccess(final DiscordAPI api) {
                    System.out.println("Successful login");
                    final MessageReceiver receiver = api.getChannelById("189359733377990656"); //general in ddc
                    receiver.sendMessage("Hello everyone! SoupBot v" + version + " here!");   //that thing travis and nick dislike
                    //receiver.sendMessage("***H a i l   t h e   c o m i n g   o f   y o u r   d o o m,   T r a v i s.***");
                    api.setAutoReconnect(true);

                    final Collection users = api.getUsers();
                    System.out.print(users.size() + " users: ");
                    for (Object username : users) {
                        System.out.print(username.toString());
                    }
                    System.out.println();

                    api.setGame("$help");

//                    TimedEventHandlerRunnable timedEventHandlerRunnable = new TimedEventHandlerRunnable(api, api.getChannelById("189359733377990656")); //general in ddc;
//                    timedEventHandlerRunnable.start();


                    //user status changes
                    api.registerListener((UserChangeStatusListener) (discordAPI, user, oldStatus) -> {
                        System.out.println(new Timestamp(System.currentTimeMillis()) + ": " + user.getName() + "'s status has changed from " + oldStatus.toString() + " to " + user.getStatus().toString());

                        ArrayList<String> notificationPeople = new ArrayList<>(Arrays.asList("Silver", "Almurray155", "Meme", "Rickl", "Cyrinthia", "Zomyster"));
                        ArrayList<String> runningProcesses = getProcessList();
                        String[] uninterruptibleProcesses = new String[]{"MinecraftLauncher.exe", "Terraria.exe"}; //add more to here

                        if (arrayListContainsIgnoreCase(notificationPeople, user.getName())) {
                            final Runnable runnable =
                                    (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.asterisk");
                            if (!arrayListContainsIgnoreCase(runningProcesses, uninterruptibleProcesses, false) && true == false) { //remove the "&& true = false" to make do JOptionPane things
                                if (runnable != null) runnable.run();
                                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), user.getName() + "'s status has changed from " + oldStatus.toString() + " to " + user.getStatus().toString());
                            } else {
                                System.out.println("Uninterruptible programs are open, displaying notification in system tray");
                                if (SystemTray.isSupported()) {
                                    memebot td = new memebot();
                                    try {
                                        td.displayTray(user.getName(), oldStatus.toString() + " → " + user.getStatus().toString());
                                    } catch (AWTException e) {
                                        e.printStackTrace();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    System.err.println("System tray not supported!");
                                }
                            }
                        }
                    });

                    api.registerListener((MessageCreateListener) (api1, message) -> {
                        //System.out.println("Message received: \"" + message.getContent() + "\"");
                        // check the content of the message

                        // censorship below

                        String editedMessage = message.getContent();

                        if (censor[0]) {
                            editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "fuck", "tarnation"); //replace all of this with a StringUtils.replaceEach();
                            editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "shit", "flummery");
                            editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "pussy", "baguette");
                            editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "cunt", "baguette");
                            editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "bitch", "<:timeforcrab:292796338645630978>");
                            editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "nigger", "African American human being");
                            editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "cock", "doodle");
                            editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "dick", "doodle");
                            editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "bastard", "baguette");
                            editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "or does it", "I'm a frog");

                            if (message.getAuthor().getName().equals("Rickl")) {
                                editedMessage = StringUtils.replaceIgnoreCase(editedMessage, "oof", "I should quit overwatch");
                            }
                            if (!message.getContent().equals(editedMessage)) {
                                message.delete();
                                message.reply(String.valueOf("**" + message.getAuthor()) + "**: \n" + editedMessage);
                                System.out.println(message.getContent() + " → " + editedMessage);
                            }
                        }
                        // end of censorship

                        if (modes[1]) { //restricted mode
                            if ((memebot.isOnList(message.getAuthor().getName(), whitelist)) && message.getContent().startsWith("$")) {

                            } else {
                                if (message.getContent().startsWith("$")) {
                                    message.reply("Restricted mode is active and you, **" + message.getAuthor().getName() + "**, are not on the whitelist.");
                                    return;
                                }
                            }
                        }

                        if (games[0]) { //guessnumber game

                            if (message.getAuthor().getId().equals(game0UserID)) {
                                if (message.getContent().startsWith("$guess ")) {
                                    game0Turns++;
                                    String msg = message.getContent().replace("$guess ", "");
                                    int guess = Integer.parseInt(msg);
                                    if (guess < game0Num) message.reply("Higher.");
                                    else if (guess > game0Num) message.reply("Lower.");
                                    else if (guess == game0Num) {
                                        message.reply("That's it!");
                                        message.reply(message.getAuthor().getName() + " is victorious in " + game0Turns + " turns!");
                                        message.reply(message.getAuthor().getName() + " receives **" + guessNumberVictory(game0UserID, game0Turns, api1, message.getChannelReceiver()) + "** exp.");
                                        games[0] = false;
                                        return;
                                    }
                                } else if (message.getContent().equalsIgnoreCase("$quit")) {
                                    message.reply("Exiting game.");
                                    games[0] = false;
                                    return;
                                }
                            }
                        }

                        if (games[1]) { //hitlerman game
                            if (message.getContent().startsWith("$guess") && !message.getContent().startsWith("$guessword")) { //$guessword also starts with $guess so we need to check for that
                                String strChar = message.getContent().replace("$guess ", "");
                                if (strChar.length() == 1) {
                                    char c = strChar.charAt(0);
                                    c = Character.toLowerCase(c);
                                    if (game1UnusedChars.contains(c)) {
                                        game1TurnNumber++;
                                        game1UnusedChars.remove(game1UnusedChars.indexOf(c));
                                        ArrayList<Integer> indexes = indexesOfCharInArrayList(c, game1ActualWord);
                                        if (indexes.size() > 0) {
                                            for (int i : indexes) {
                                                game1GuessedWord.set(i, game1ActualWord.get(i));
                                            }
                                            if (indexes.size() == 1) {
                                                message.reply("There is 1 " + c + " in the word.");

                                                if (!arrayListContainsIgnoreCase(game1HelpfulUsers, message.getAuthor().getId())) {
                                                    game1HelpfulUsers.add(message.getAuthor().getId());
                                                    try {
                                                        LoadUserStats.loadStats(message.getAuthor()).addPlayedGames1(1);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            } else {
                                                message.reply("There are " + indexes.size() + " " + c + "'s in the word.");
                                                if (!arrayListContainsIgnoreCase(game1HelpfulUsers, message.getAuthor().getId())) {
                                                    game1HelpfulUsers.add(message.getAuthor().getId());
                                                    try {
                                                        LoadUserStats.loadStats(message.getAuthor()).addPlayedGames1(1);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        } else {
                                            game1lives--;
                                            message.reply("There are no " + c + "'s in the word.");
                                        }
                                        if (indexesOfCharInArrayList('_', game1GuessedWord).size() == 0) {
                                            message.reply("Congratulations to " + arrayListOfIdsAsStringListOfNames(game1HelpfulUsers, api1) + "! You've stopped Hitler from being born and saved millions of lives!\n" +
                                                    "The word was `" + game1Word + "`. You were victorious with " + game1lives + " lives remaining in " + game1TurnNumber + " turns.\n");

                                            message.reply("All players involved earn **" + hitlermanVictory(game1HelpfulUsers, game1TurnNumber, game1lives, game1MaxLives, game1Word, api1, message.getChannelReceiver()) + "** exp.");
                                            games[1] = false;
                                        } else {
                                            if (game1lives == 0) {
                                                if (game1HelpfulUsers.size() > 1) {
                                                    message.reply("Oh no! " + arrayListOfIdsAsStringListOfNames(game1HelpfulUsers, api1) + " have failed and millions of lives have been doomed to fall to Hitler.");
                                                } else {
                                                    message.reply("Oh no! " + arrayListOfIdsAsStringListOfNames(game1HelpfulUsers, api1) + " has failed and millions of lives have been doomed to fall to Hitler.");
                                                }
                                                message.reply("The word was `" + game1Word + "`");
                                                games[1] = false;
                                            } else {
                                                message.reply(game1lives + " lives remaining.");
                                                message.reply(makeAsciiSpermEgg(game1lives) + "\n" +
                                                        "```\n" +
                                                        arrayListAsStringForHitlermanGuess(game1GuessedWord) + "\n" +
                                                        "```");
                                            }
                                        }
                                    } else {
                                        message.reply("You've already guessed that character.");
                                    }
                                } else {
                                    message.reply("Error: Make sure you guess exactly one character at a time.");
                                    return;
                                }
                            } else if (message.getContent().startsWith("$guessword")) {
                                String word = message.getContent().replace("$guessword ", "");
                                if (message.getContent().equalsIgnoreCase("$guessword")) { //user didn't give a word
                                    message.reply("Use \"$guessword [word]\" to guess a word.");
                                } else if (word.equalsIgnoreCase(game1Word)) {
                                    game1TurnNumber++;
                                    if (!arrayListContainsIgnoreCase(game1HelpfulUsers, message.getAuthor().getId())) {
                                        game1HelpfulUsers.add(message.getAuthor().getId());
                                        try {
                                            LoadUserStats.loadStats(message.getAuthor()).addPlayedGames1(1);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    message.reply("Congratulations to " + arrayListOfIdsAsStringListOfNames(game1HelpfulUsers, api1) + "! You've stopped Hitler from being born and saved millions of lives!\n" +
                                            "The word was `" + game1Word + "`. You were victorious with " + game1lives + " lives remaining in " + game1TurnNumber + " turns.\n");

                                    message.reply("All players involved earn **" + hitlermanVictory(game1HelpfulUsers, game1TurnNumber, game1lives, game1MaxLives, game1Word, api1, message.getChannelReceiver()) + "** exp.");
                                    games[1] = false;
                                } else {
                                    game1lives--;
                                    game1TurnNumber++;
                                    if (game1lives > 0) {

                                        message.reply("That's not it! Try again.\n" +
                                                game1lives + " lives remaining.");
                                        message.reply(makeAsciiSpermEgg(game1lives) + "\n" +
                                                "```\n" +
                                                arrayListAsStringForHitlermanGuess(game1GuessedWord) + "\n" +
                                                "```");
                                    } else {
                                        if (game1HelpfulUsers.size() > 1) {
                                            message.reply("Oh no! " + arrayListOfIdsAsStringListOfNames(game1HelpfulUsers, api1) + " have failed and millions of lives have been doomed to fall to Hitler.");
                                        } else {
                                            message.reply("Oh no! " + arrayListOfIdsAsStringListOfNames(game1HelpfulUsers, api1) + " has failed and millions of lives have been doomed to fall to Hitler.");
                                        }
                                        message.reply("The word was `" + game1Word + "`");
                                        games[1] = false;
                                    }
                                }
                            } else if (message.getContent().equalsIgnoreCase("$quit")) {
                                System.out.println("hitlerman quit");
                                message.reply("Exiting game.");
                                games[1] = false;
                                return;
                            } else if (message.getContent().equalsIgnoreCase("$resend")) { //resends sperm+egg and word
                                message.reply(game1lives + " lives remaining.");
                                message.reply(makeAsciiSpermEgg(game1lives) + "\n" +
                                        "```\n" +
                                        arrayListAsStringForHitlermanGuess(game1GuessedWord) + "\n" +
                                        "```");
                            } else if (message.getContent().equalsIgnoreCase("$addlife")) {
                                if (memebot.isOnList(message.getAuthor().getName(), whitelist)) {
                                    game1lives++;
                                    message.reply("Number of lives increased to " + game1lives + ".");
                                } else {
                                    message.reply("Error: You are not a whitelisted user.");
                                }
                            }
                        }


                        //story game handling is after all the commands

                        //end of games

                        if (message.getContent().equalsIgnoreCase("$help")) {

                            int longestLength = 0;

                            for (String command : commandList) {
                                if (command.length() > longestLength) {
                                    longestLength = command.length();
                                }
                            }

                            int columnLength = longestLength + 8;

                            message.reply("Hello! I am primarily a meme bot, but I might do other things too. \n" +
                                    "Currently, I have **" + commandList.size() + "** command(s), **" + numOfSubCommands + "** subcommand(s), and **2** hidden command(s).\n" +
                                    "Commands only usable by whitelisted members are marked with an ^.\n" +
                                    "Commands partially usable by non-whitelisted members are marked with a ^^.\n" +
                                    "The command list is as follows:\n" +
                                    "```\n" +
                                    formatListAsColumns(commandList, 3, columnLength) +
                                    "```");

                        } else if (message.getContent().equalsIgnoreCase("$info")) {
                            message.reply("```\n" +
                                    "Author: TornadoOfSoup\n" +
                                    "Version: " + version + "\n" +
                                    "Date compiled: " + complieDate + "\n" +
                                    "Chat filter version: " + chatFilterVersion + "\n" +
                                    "```");
                        } else if (message.getContent().equalsIgnoreCase("$memelist")) {

                            File folder = new File("C:\\Users\\RPGenius\\Desktop\\memes\\pics");
                            File[] listOfFiles = folder.listFiles();
                            ArrayList<String> arrayListOfFiles = new ArrayList<>();
                            String displayList = "";

                            for (int i = 0; i < listOfFiles.length; i++) {
                                if (listOfFiles[i].getName().endsWith(".png") || listOfFiles[i].getName().endsWith(".jpg")) { //will only look for .png and .jpg
                                    arrayListOfFiles.add(listOfFiles[i].getName());
                                    displayList = displayList.concat(listOfFiles[i].getName() + "\n");
                                }
                            }
                            message.reply("```\n" +
                                    "There are currently [" + arrayListOfFiles.size() + "] available memes.\n" +
                                    "Current list of available memes:\n" +
                                    displayList +
                                    "The \".png\" at the end is not required.\n" +
                                    "```");
                        } else if (message.getContent().startsWith("$meme")) {
                            if (message.getContent().equalsIgnoreCase("$meme")) {
                                message.reply("```\n" +
                                        "$meme has subcommands. Use one of the following:\n" +
                                        "\"$meme random\"\n" +
                                        "\"$meme [meme]\" where [meme] is the name of the meme\n" +
                                        "Use \"$memelist\" to get a list of available memes.\n" +
                                        "```");
                            }
                            if (message.getContent().equalsIgnoreCase("$meme random")) {
                                File folder = new File("C:\\Users\\RPGenius\\Desktop\\memes\\pics\\");
                                File[] listOfFiles = folder.listFiles();
                                ArrayList<String> arrayListOfFiles = new ArrayList<>();

                                for (int i = 0; i < listOfFiles.length; i++) {
                                    if (listOfFiles[i].getName().endsWith(".png") || listOfFiles[i].getName().endsWith(".jpg")) { //will only look for .png or .jpg
                                        arrayListOfFiles.add(listOfFiles[i].getName());
                                    }
                                }
                                message.reply("Getting file...");
                                Random rand = new Random();
                                int max = arrayListOfFiles.size() - 1;
                                int index = rand.nextInt(max + 1);

                                File file = new File("C:\\Users\\RPGenius\\Desktop\\memes\\pics\\" + arrayListOfFiles.get(index));
                                message.getChannelReceiver().sendFile(file);
                            }
                            if ((!message.getContent().equalsIgnoreCase("$meme")) && (!message.getContent().equalsIgnoreCase("$meme random"))) { //$meme + something else
                                String[] parts = message.getContent().split(" ");


                                if (parts.length > 2) {
                                    message.reply("Error: The given command contains **" + parts.length + "** parts instead of the necessary 2.");
                                } else {

                                    if (modes[0] == true) {
                                        message.reply("Getting file...");
                                        String url = "";
                                        url = getUrl("C:\\Users\\RPGenius\\Desktop\\memes\\pics\\piclinks\\" + parts[1]);
                                        message.reply(url);
                                    } else {

                                        message.reply("Getting file...");
                                        if (parts[1].endsWith(".png") || parts[1].endsWith(".jpg")) {
                                            File file = new File("C:\\Users\\RPGenius\\Desktop\\memes\\pics\\" + parts[1]);
                                            message.getChannelReceiver().sendFile(file);
                                            if (!file.exists()) {
                                                message.reply("Error: The meme specified is misspelled, not in our library, or does not exist.");
                                            }
                                        } else {
                                            File file = new File("C:\\Users\\RPGenius\\Desktop\\memes\\pics\\" + parts[1] + ".png");
                                            message.getChannelReceiver().sendFile(file);
                                            if (!file.exists()) {
                                                message.reply("Error: The meme specified is misspelled, not in our library, or does not exist.");
                                            }
                                        }
                                    }

                                }
                            }
                        } else if (message.getContent().equalsIgnoreCase("$shutdown")) {
                            if (memebot.isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("Farewell. <:timeforcrab:292796338645630978>");
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } //a try catch in one line because it should be more compact
                                api1.disconnect();
                                System.exit(0);
                            } else {
                                message.reply("Only whitelisted members have the ability to shut me down.");
                            }
                        } else if (message.getContent().startsWith("$getavatar")) {
                            if (message.getContent().equalsIgnoreCase("$getavatar")) {
                                message.reply("```\n" +
                                        "Returns the profile picture of the given user.\n" +
                                        "Syntax: \"$getavatar [user / id]\"\n" +
                                        "Example: \"$getavatar @iamadog$1234\"\n" +
                                        "```");
                            } else {
                                List<User> mentions = message.getMentions();
                                String[] parts = message.getContent().split(" ");
                                if (mentions.size() != 1 && parts.length < 2) {
                                    message.reply("Error: The given command does not fit the requirements.\n" +
                                            "Syntax: \"$getavatar [user / id]\"\n" +
                                            "Example: \"$getavatar @iamadog#1234\"");
                                } else if (mentions.size() == 1) {
                                    message.reply("Profile picture of **" + mentions.get(0) + "**: " + mentions.get(0).getAvatarUrl().toString());
                                } else {
                                        /*try {
                                            User user = api.getUserById(parts[1]).get();
                                            System.out.println(user.getName());
                                            System.out.println(user.getAvatarId());
                                            System.out.println(user.getAvatarUrl().toString());
                                            message.reply("Profile picture of **" + user.getName() + "**: " + user.getAvatarUrl().toString());
                                        } catch (Exception e) {
                                            message.reply(e.getMessage());
                                            e.printStackTrace();
                                        }
                                        */
                                    message.reply("This functionality is currently not supported.");
                                }
                            }
                        } else if (message.getContent().equalsIgnoreCase("$silverTime")) {
                            SimpleDateFormat dateFormatCST = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
                            dateFormatCST.setTimeZone(TimeZone.getTimeZone("Canada/East-Saskatchewan"));

                            message.reply("Current time for Silver: " + dateFormatCST.format(new Date()) + " CST");
                        } else if (message.getContent().startsWith(("$vote"))) {
                            if (message.getContent().equalsIgnoreCase("$vote")) {
                                message.reply("```\n" +
                                        "Conducts a vote based on reactions.\n" +
                                        "Syntax: \"$vote [question] | [time in minutes] | [reaction 1] [reaction 2] ... | [optional tag everyone]\"\n" +
                                        "Example: \"$vote Happy, sad, or mad? | 60 | :smile: :frowning: :angry:\"\n" +
                                        "Example: \"$vote Yes or no? | 90 | :thumbsup: :thumbsdown: | 1\"\n" +
                                        "Note that you can have as many reactions as you want.\n" +
                                        "For the last part, use 0 to tag everyone and 1 to tag here.\n" +
                                        "Also note that custom emotes are currently not supported.\n" +
                                        "```");
                            } else if (!memebot.isOnList(message.getAuthor().getName(), whitelist) && !memebot.isOnList(message.getAuthor().getName(), promotedList)) {
                                message.reply("This command is only usable by whitelisted or promoted members.");
                                return;
                            } else if (message.getContent().startsWith("$vote readID ")) {
                                String id = message.getContent().replace("$vote readID ", "");
                                new ScheduledVoteReadRunnable("Vote ReadID", 0.01, "PLACEHOLDER QUESTION", api1.getMessageById(id), api1).start();
                            } else {
                                String msg = message.getContent().replace("$vote ", "");
                                String[] parts = msg.split(" \\| ");
                                if (parts.length != 3 && parts.length != 4) {
                                    message.reply("Error: The command must have exactly 3 or 4 parts separated by vertical bars. (\" | \")");
                                    //System.out.println(parts.length);
                                } else {
                                    try {
                                        Future<Message> voteFutureMessage;
                                        StringBuilder builder = new StringBuilder();
                                        if (parts.length < 4) {
                                        } else if (parts[3].equals("0")) {
                                            builder.append("@everyone\n");
                                        } else if (parts[3].equals("1")) {
                                            builder.append("@here\n");
                                        }
                                        if (Double.parseDouble(parts[1]) > 60) {
                                            int hours = (int) Math.floor(Double.parseDouble(parts[1]) / 60);
                                            if (hours > 24) {
                                                int days = (int) Math.floor(hours / 24);
                                                builder.append("```\n\"" + parts[0] + "\"\n" +
                                                        "\n" +
                                                        "The voting period will end in " + days + " days, " + hours % 24 + " hours, and " + Double.parseDouble(parts[1]) % 60 + " minutes.\n" +
                                                        "Vote!\n" +
                                                        "```");
                                            } else {
                                                builder.append("```\n\"" + parts[0] + "\"\n" +
                                                        "\n" +
                                                        "The voting period will end in " + hours + " hours and " + Double.parseDouble(parts[1]) % 60 + " minutes.\n" +
                                                        "Vote!\n" +
                                                        "```");
                                            }
                                        } else {
                                            builder.append("```\n\"" + parts[0] + "\"\n" +
                                                    "\n" +
                                                    "The voting period will end in " + parts[1] + " minutes.\n" +
                                                    "Vote!\n" +
                                                    "```");
                                        }
                                        voteFutureMessage = message.reply(builder.toString());
                                        while (!voteFutureMessage.isDone()) {
                                        }

                                        Message voteMessage = voteFutureMessage.get();

                                        String[] reactions = parts[2].split(" ");

                                        new ReactionAddingWithSimulatedRateLimitRunnable(voteMessage, reactions).start();
                                        ScheduledVoteReadRunnable vote = new ScheduledVoteReadRunnable("Vote", Double.parseDouble(parts[1]), parts[0], voteMessage, api1);
                                        vote.start();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else if (message.getContent().equalsIgnoreCase("$upcoming")) {
                            message.reply("```\n" +
                                    "Here's a non-exhaustive list of what is currently planned for updates in the near future.\n" +
                                    "   A way to add memes to the library and maybe rename them from the discord.\n" +
                                    "   Implement coterminal and other math things.\n" +
                                    "   Make the $mode command work more.\n" +
                                    "   Add levels of restriction to restricted mode.\n" +
                                    "   Implement picture reformatter.\n" +
                                    "   ```");
                        } else if (message.getContent().equalsIgnoreCase("$hypixel")) {
                            long cooldownLengthInMinutes = 15;  //15 minutes
                            long cooldownLength = cooldownLengthInMinutes * 1000 * 60;
                            System.out.println("hypixel: " + message.getAuthor().getName() + " | " + message.getAuthor().getId());
                            if (message.getAuthor().getId().equals("190519404780322818")) { //if the message author is nick | 190519404780322818
                                System.out.println("it's a nick");
                                if (System.currentTimeMillis() < (cooldown[0] + (cooldownLength * 12)) && System.currentTimeMillis() != 0) { //multiply cooldown by 12;
                                    message.reply("Because you're Nick, this command is on cooldown for another " + Math.abs(((System.currentTimeMillis() - cooldown[0]) / 1000) - 3600) + " seconds.");
                                }
                            } else {
                                if (System.currentTimeMillis() > (cooldown[0] + cooldownLength)) {

                                    cooldown[0] = System.currentTimeMillis();

                                    try {
                                        message.reply("GoldfishClancy: @everyone hypixel?\n");
                                        Thread.sleep(2000);
                                        message.reply("we should all play later");
                                        Thread.sleep(1000);
                                        message.reply("@here we should all go on hypixel");
                                        Thread.sleep(500);
                                        message.reply("https://cdn.discordapp.com/attachments/189359733377990656/311631065112641537/unknown.png");
                                        message.reply("anyjuan?");
                                        Thread.sleep(2000);
                                        message.reply("https://cdn.discordapp.com/attachments/189359733377990656/311631154904301569/unknown.png");
                                        Thread.sleep(1000);
                                        message.reply("hypixel?");
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    message.reply("This command is on cooldown for another " + Math.abs(((System.currentTimeMillis() - cooldown[0]) / 1000) - 600) + " seconds.");
                                }
                            }
                        } else if (message.getContent().equalsIgnoreCase("$censor")) {
                            if (memebot.isOnList(message.getAuthor().getName(), whitelist)) {
                                censor[0] = !censor[0];
                                message.reply("Censorship mode toggled to *" + censor[0] + "*");
                            } else {
                                message.reply("Only TornadoOfSoup has the ability to toggle censorship. You, **" + message.getAuthor().getName() + "**, do not possess that ability.");
                            }
                        } else if (message.getContent().startsWith("$mode")) {
                            if (!memebot.isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("Only whitelisted members can use the $mode command.");
                                return;
                            }
                            if (message.getContent().equalsIgnoreCase("$mode")) {
                                message.reply("```\n" +
                                        "Toggles certain modes on/off." +
                                        "   \"$mode fastPictures\" -- replaces sending pictures with sending links, resulting in overall speed improvements\n" +
                                        "   \"$mode restricted\" -- toggles \"restricted mode\" on, allowing only certain roles to send commands\n" +
                                        "```");
                            } else if (message.getContent().equalsIgnoreCase("$mode fastPictures")) {
                                modes[0] = !modes[0];
                                message.reply("\"fastPictures\" mode toggled to **" + modes[0] + "**.");
                            } else if (message.getContent().equalsIgnoreCase("$mode restricted")) {
                                modes[1] = !modes[1];
                                message.reply("\"restricted\" mode toggled to **" + modes[1] + "**.");
                            } else {
                                message.reply("The specified mode either does not exist or has been misspelled.");
                            }

                        } else if (message.getContent().startsWith("$whitelist")) {
                            if (message.getContent().equalsIgnoreCase("$whitelist")) {
                                message.reply("```\n" +
                                        "Shows/changes some aspects of the whitelist.\n" +
                                        "   \"$whitelist list\" -- lists the current members on the whitelist\n" +
                                        "   \"$whitelist add\" -- adds a name to the whitelist\n" +
                                        "   \"$whitelist remove\" -- removes a name from the whitelist\n" +
                                        "```");
                            } else if (message.getContent().equalsIgnoreCase("$whitelist list")) {
                                String whitelistList = "";

                                for (String name : whitelist) {
                                    whitelistList = whitelistList.concat((name + "\n"));
                                }

                                message.reply("```\n" +
                                        "The current members of the whitelist are as follows:\n" +
                                        whitelistList +
                                        "```");
                            } else if (message.getContent().equalsIgnoreCase("$whitelist add")) {
                                message.reply("This command is currently under maintenance.");
                            } else if (message.getContent().equalsIgnoreCase("$whitelist remove")) {
                                message.reply("This command is currently under maintenance.");
                            }
                        } else if (message.getContent().startsWith("$prune")) {
                            if (!memebot.isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("This command is only usable by whitelisted members.");
                                return;
                            }
                            if (message.getContent().equalsIgnoreCase("$prune")) {
                                message.reply("```\n" +
                                        "Mass-deletes messages.\n" +
                                        "   \"$prune [x] [person]\" -- deletes x amount of messages by a given user\n" +
                                        "Can only delete a maximum of 100 messages at a time due to safety reasons.\n" +
                                        "Any number higher than 10 will require a y/n confirmation.\n" +
                                        "If a user is not specified, messages by all users will be deleted.\n" +
                                        "Only usable by whitelisted users.\n" +
                                        "Example: \"$prune 15 @rem\"\n" +
                                        "         \"$prune 15\"\n" +
                                        "```");

                            } else {
                                String[] parts = message.getContent().split(" ");
                                if (parts.length != 2 && parts.length != 3) {
                                    message.reply("Error: The given command contains **" + parts.length + "** parts of the necessary 2 or 3.");
                                } else {
                                    try {
                                        int numOfMessages = Integer.parseInt(parts[1]); //TODO maybe replaces this with an instanceof operator
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                        message.reply("The given number must be an integer.");
                                        return;
                                    }
                                    Channel channel = message.getChannelReceiver();
                                    List<User> mentions = message.getMentions();
                                    if (mentions.size() > 1) {
                                        message.reply("Command must contain 1 or less mentions.");
                                        return;
                                    }
                                    if (mentions.size() == 0) {
                                        int numOfMessages = Integer.parseInt(parts[1]);
                                        Future<MessageHistory> futureMessageHistory = channel.getMessageHistory(numOfMessages);
                                        try {
                                            MessageHistory messageHistory = futureMessageHistory.get();
                                            Collection<Message> messages = messageHistory.getMessages();
                                            for (Message msg : messages) {
                                                msg.delete();
                                            }
                                            message.reply("`Deleted " + messages.toArray().length + " messages.`");
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                            message.reply("help i died");
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                            message.reply("help i died");
                                        }

                                    } else {
                                        int numOfMessages = Integer.parseInt(parts[1]);
                                        Future<MessageHistory> futureMessages = mentions.get(0).getMessageHistory(numOfMessages);
                                        try {
                                            MessageHistory messages = futureMessages.get();
                                            for (Message msg : messages.getMessages()) {
                                                System.out.println("Deleting: " + msg.getContent());

                                                msg.delete();
                                            }
                                            message.reply("`Deleted " + messages.getMessages().toArray().length + " messages by " + mentions.get(0).getName() + ".`");
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                            message.reply("help i died");
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                            message.reply("help i died");
                                        }

                                    }

                                }
                            }
                        } else if (message.getContent().startsWith("$convert")) {
                            if (message.getContent().equalsIgnoreCase("$convert")) {
                                message.reply("```\n" +
                                        "Converts units to other units.\\n" +
                                        "Syntax: \"$convert [UNIT TYPE] [NUM OF UNIT A] [UNIT A] [UNIT B]\"\\n" +
                                        "Unit types: temp, length, weight, angle\n" +
                                        "```");
                                return;
                            } else {
                                String[] parts = message.getContent().split(" ");
                                if (parts.length != 5) {
                                    message.reply("Error: The given command contains **" + parts.length + "** parts instead of the required 5.");
                                    return;
                                }
                                if ((!isOnList(parts[1], unitTypeList))) {
                                    message.reply("Error: The given unit type is unsupported or misspelled.");
                                    return;
                                }
                                if (parts[1].equalsIgnoreCase("temp")) {
                                    if ((!isOnList(parts[3], unitTempList)) || (!isOnList(parts[4], unitTempList))) {
                                        message.reply("```\n" +
                                                "Error: One or more of the given units are not compatible with the given unit type.\n" +
                                                "Valid temperature units are: C, F, K\n" +
                                                "Example: \"$convert temp 15 C F\" converts 15° celsius to fahrenheit.\n" +
                                                "```");
                                        return;
                                    }
                                    System.out.println(message.getContent());
                                    if (parts[3].equalsIgnoreCase("F") && parts[4].equalsIgnoreCase("F")) { //first fahrenheit
                                        message.reply(parts[2] + "°F = " + parts[2] + "°F");
                                        return;
                                    } else if (parts[3].equalsIgnoreCase("F") && parts[4].equalsIgnoreCase("C")) {
                                        double degree1 = Double.valueOf(parts[2]);
                                        double degree2 = (degree1 - 32) / 1.8;
                                        message.reply(parts[2] + "°F = " + degree2 + "°C");
                                        return;
                                    } else if (parts[3].equalsIgnoreCase("F") && parts[4].equalsIgnoreCase("K")) {
                                        double degree1 = Double.valueOf(parts[2]);
                                        double degree2 = ((degree1 - 32) * 1.8) + 273.15;
                                        message.reply(parts[2] + "°F = " + degree2 + "°K");
                                        return;
                                    } else if (parts[3].equalsIgnoreCase("C") && parts[4].equalsIgnoreCase("C")) { //first celsius
                                        message.reply(parts[2] + "°C = " + parts[2] + "°C");
                                        return;
                                    } else if (parts[3].equalsIgnoreCase("C") && parts[4].equalsIgnoreCase("F")) {
                                        double degree1 = Double.valueOf(parts[2]);
                                        double degree2 = (degree1 * 1.8) + 32;
                                        message.reply(parts[2] + "°C = " + degree2 + "°F");
                                        return;
                                    } else if (parts[3].equalsIgnoreCase("C") && parts[4].equalsIgnoreCase("K")) {
                                        double degree1 = Double.valueOf(parts[2]);
                                        double degree2 = degree1 + 273.15;
                                        message.reply(parts[2] + "°C = " + degree2 + "°K");
                                        return;
                                    } else if (parts[3].equalsIgnoreCase("K") && parts[4].equalsIgnoreCase("K")) { //first kelvin
                                        message.reply(parts[2] + "°K = " + parts[2] + "°K");
                                        return;
                                    } else if (parts[3].equalsIgnoreCase("K") && parts[4].equalsIgnoreCase("C")) {
                                        double degree1 = Double.valueOf(parts[2]);
                                        double degree2 = degree1 - 273.15;
                                        message.reply(parts[2] + "°K = " + degree2 + "°C");
                                        return;
                                    } else if (parts[3].equalsIgnoreCase("K") && parts[4].equalsIgnoreCase("F")) {
                                        double degree1 = Double.valueOf(parts[2]);
                                        double degree2 = (degree1 - 273.15) * 1.8 + 32;
                                        message.reply(parts[2] + "°K = " + degree2 + "°F");
                                        return;
                                    }

                                } else if (parts[1].equalsIgnoreCase("angle")) {
                                    if ((!isOnList(parts[3], unitAngleList)) || (!isOnList(parts[4], unitAngleList))) {
                                        message.reply("```\n" +
                                                "Error: One or more of the given units are not compatible with the given unit type.\n" +
                                                "Valid angle units are D, R, deg, rad\n" +
                                                "To format radians, remove pi from the numberator.\n" +
                                                "Example: \"$convert angle 1/6 rad deg\"\n" +
                                                "```");
                                        return;
                                    }
                                    if (parts[4].equalsIgnoreCase("D") || parts[4].equalsIgnoreCase("deg")) {
                                        String[] fractionParts = parts[2].split("/");
                                        double numerator = Double.parseDouble(fractionParts[0]) * Math.PI;
                                        double degrees = Math.toDegrees(numerator / Double.parseDouble(fractionParts[1]));
                                        degrees = Math.round(degrees * 10000) / 10000; //rounds to 4 decimal places
                                        message.reply(fractionParts[0] + "π/" + fractionParts[1] + " radians = " + degrees + "°.");
                                    } else if (parts[4].equalsIgnoreCase("R") || parts[4].equalsIgnoreCase("rad")) {
                                        int[] fraction = simplifyFraction(Integer.parseInt(parts[2]), 180);
                                        if (fraction[0] != 1) {
                                            message.reply(parts[2] + "° = " + fraction[0] + "π/" + fraction[1] + " radians.");
                                        } else {
                                            message.reply(parts[2] + "° = π/" + fraction[1] + " radians.");
                                        }
                                    }
                                }
                            }

                        } else if (message.getContent().startsWith("$puppet @say@")) {
                            if (memebot.isOnList(message.getAuthor().getName(), whitelist)) {
                                String[] parts = message.getContent().split("@say@");
                                message.reply(parts[1]);
                                for (String part : parts) {
                                    System.out.println(part);
                                }
                                System.out.println(message.getAuthor().getName() + " is secretly sending message: " + parts[1]);
                                message.delete();
                            } else {
                                String[] parts = message.getContent().split("@say@");
                                System.out.println(message.getAuthor().getName() + " is not secretly sending message: " + parts[1]);
                                //message.delete();
                            }
                        } else if (message.getContent().startsWith("$choose")) {
                            if (message.getContent().equalsIgnoreCase("$choose")) {
                                message.reply("```\n" +
                                        "Randomly chooses between given choices.\n" +
                                        "Syntax: \"$choose [choice1];[choice2];[choice3];.....[choicex]\"\n" +
                                        "Example: \"$choose Monday;Tuesday;Wednesday;Thursday;Friday\"\n" +
                                        "```");
                                return;
                            }
                            String msg = message.getContent().replace("$choose ", "");
                            String[] parts = msg.split(";");
                            if (parts.length < 2) {
                                message.reply("Error: The given command contains **" + parts.length + "** choices instead of the necessary 2 or more.");
                                return;
                            }
                            ArrayList<String> choices = new ArrayList<>();
                            for (String part : parts) {
                                choices.add(part);
                            }
                            //choices.remove(0);

                            Random rand = new Random();
                            int rng = rand.nextInt(choices.size());
                            message.reply("**" + message.getAuthor().getName() + "**, I've carefully considered your inquiry and (randomly) selected the choice: *" + choices.get(rng) + "*.");
                        } else if (message.getContent().startsWith("$bubblesort")) {
                            if (message.getContent().equalsIgnoreCase("$bubblesort")) {
                                message.reply("```\n" +
                                        "Sorts an array of numbers from least to greatest using a bubble sort and gives the mean, median, mode, and range.\n" +
                                        "Syntax: \"$bubblesort [num 1] [num 2] [num 3] . . . [num x]\"\n" +
                                        "Example: \"$bubblesort 13 6 10 19 31 22 42\"\n" +
                                        "```");
                                return;
                            }
                            long millis = System.currentTimeMillis();
                            //message.reply("This command is currently under maintenance.");
                            String msg = message.getContent().replace("$bubblesort ", "");
                            String[] array = msg.split(" ");
                            ArrayList<Integer> elements = new ArrayList<>();
                            for (String element : array) {
                                try {
                                    if ((element.equals(null) || element.equals("")) == false) {
                                        elements.add(Integer.parseInt(element));
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    message.reply("```\n" +
                                            "java.lang.NumberFormatException: for input string: \"" + element + "\"\n" +
                                            "The given string is not of an acceptable format or is too large.\n" +
                                            "```");
                                }
                            }

                            Arrays.sort(array);
                            ArrayList<Integer> sortedList = bubbleSort(elements);

                            StringBuilder sb = new StringBuilder();
                            for (int element : sortedList) {
                                sb.append(element + ", ");
                            }
                            sb.deleteCharAt(sb.length() - 1); //removes the final comma and space at the end of the string
                            sb.deleteCharAt(sb.length() - 1);

                            double mean = 0;
                            for (int x : sortedList) {
                                mean += x;
                            }
                            mean = mean / sortedList.size();

                            double median = 0;
                            if (sortedList.size() % 2 == 0) {
                                double m1 = (sortedList.size() / 2) + 0.5;
                                double m2 = (sortedList.size() / 2) - 0.5;

                                median = ((double) sortedList.get((int) m1) + (double) sortedList.get((int) m2)) / 2;
                            } else {
                                median = sortedList.get(sortedList.size() / 2);
                            }

                            double range = sortedList.get(sortedList.size() - 1) - sortedList.get(0);

                            if (sb.toString().length() > 1650) {
                                message.reply("The given list is too long, so sending it here is likely to break Discord's character limit. If it doesn't send below, the final message was over 2000 characters.");
                            }

                            message.reply("The given list has **" + sortedList.size() + "** sortable elements. It is sorted below:\n" +
                                    "```\n" + sb.toString() + "\n" +
                                    "Mean: " + mean + "\n" +
                                    "Median: " + median + "\n" +
                                    "Mode: Mode is weird.\n" +
                                    "Range: " + range + "\n" +
                                    "Completed in " + ((System.currentTimeMillis() - millis)) + " milliseconds.\n" +
                                    "```");
                        } else if (message.getContent().startsWith("$math")) {
                            if (message.getContent().equalsIgnoreCase("$math")) {
                                message.reply("```\n" +
                                        "Evaluates a given expression.\n" +
                                        "This might be a little buggy, we'll see.\n" +
                                        "Syntax: \"$math [expression]\"\n" +
                                        "Example: \"$math 8 * 12\"\n" +
                                        "For valid JavaScript math info, go here: https://www.w3schools.com/js/js_math.asp\n" +
                                        "```");
                                return;
                            }
                            String expression = message.getContent().replace("$math ", "");

                            // Get JavaScript engine
                            ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");

                            try {
                                Object result = engine.eval(expression);
                                message.reply(expression + " = " + result);
                            } catch (ScriptException e) {
                                e.printStackTrace();
                                message.reply("Something there didn't work, make sure the given expression is considered valid JavaScript.\n" +
                                        "For syntax of JavaScript mathematical functions, go here: https://www.w3schools.com/js/js_math.asp");
                            }

                        } else if (message.getContent().startsWith("$mute")) {
                            if (message.getContent().equalsIgnoreCase("$mute")) {
                                message.reply("```\n" +
                                        "Mutes a user. The user must be mentioned." +
                                        "Syntax: \"$mute [name]\"\n" +
                                        "Example: \"$mute @rem\"\n" +
                                        "```");
                                return;
                            }
                            if (!memebot.isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("This command is only usable by whitelisted members.");
                                return;
                            }
                            boolean roleExists = false;
                            Collection<Role> roles = message.getChannelReceiver().getServer().getRoles();
                            for (Role role : roles) {
                                if (role.getName().equalsIgnoreCase("SoupMuted")) {
                                    Role mutedRole = role;
                                    List<User> users1 = message.getMentions();
                                    if (users1.size() == 0) {
                                        message.reply("No mentions found.");
                                    }
                                    for (User user : users1) {
                                        mutedRole.addUser(user);
                                        message.reply("Muted " + user.getName() + ".");
                                    }
                                    return;
                                }
                            }
                            //if the role doesn't exist

                            try {
                                Future<Role> roleFuture = message.getChannelReceiver().getServer().createRole();
                                Role mutedRole = roleFuture.get();
                                mutedRole.updateName("SoupMuted");
                                List<User> users1 = message.getMentions();
                                if (users1.size() == 0) {
                                    message.reply("No mentions found.");
                                }
                                for (User user : users1) {
                                    mutedRole.addUser(user);
                                    message.reply("Muted " + user.getName() + ".");
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                        } else if (message.getContent().startsWith("$supermute")) {
                            if (message.getContent().equalsIgnoreCase("$supermute")) {
                                message.reply("```\n" +
                                        "Super mutes a user, preventing them from speaking in channels that don't silence muted people. The user must be mentioned." +
                                        "Syntax: \"$supermute [name]\"\n" +
                                        "Example: \"$supermute @rem\"\n" +
                                        "```");
                                return;
                            }
                            if (!memebot.isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("This command is only usable by whitelisted members.");
                                return;
                            }
                            Collection<Role> roles = message.getChannelReceiver().getServer().getRoles();
                            for (Role role : roles) {
                                if (role.getName().equalsIgnoreCase("SoupSuperMuted")) {
                                    Role mutedRole = role;
                                    List<User> users1 = message.getMentions();
                                    if (users1.size() == 0) {
                                        message.reply("No mentions found.");
                                    }
                                    for (User user : users1) {
                                        mutedRole.addUser(user);
                                        message.reply("**Super muted** " + user.getName() + ".");
                                    }
                                    return;
                                }
                            }
                            //if the role doesn't exist

                            try {
                                Future<Role> roleFuture = message.getChannelReceiver().getServer().createRole();
                                Role mutedRole = roleFuture.get();
                                mutedRole.updateName("SoupSuperMuted");
                                List<User> users1 = message.getMentions();
                                if (users1.size() == 0) {
                                    message.reply("No mentions found.");
                                }
                                for (User user : users1) {
                                    mutedRole.addUser(user);
                                    message.reply("**Super muted** " + user.getName() + ".");
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                        } else if (message.getContent().startsWith("$unmute")) {
                            if (message.getContent().equalsIgnoreCase("$unmute")) {
                                message.reply("```\n" +
                                        "Unmutes a user. The user must be mentioned and must have been muted by SoupBot." +
                                        "Syntax: \"$unmute [name]\"\n" +
                                        "Example: \"$unmute @rem\"\n" +
                                        "```");
                                return;
                            }
                            if (!memebot.isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("This command is only usable by whitelisted members.");
                                return;
                            }
                            if (message.getMentions().size() == 0) {
                                message.reply("No mentions found.");
                            }
                            Collection<Role> roles = message.getChannelReceiver().getServer().getRoles();
                            for (Role role : roles) {
                                if (role.getName().equalsIgnoreCase("SoupMuted")) {
                                    Role mutedRole = role;
                                    for (User user : message.getMentions()) {
                                        mutedRole.removeUser(user);
                                        message.reply("Unmuted " + user.getName() + ".");
                                    }
                                    return;
                                } else if (role.getName().equalsIgnoreCase("SoupSuperMuted")) {
                                    Role mutedRole = role;
                                    for (User user : message.getMentions()) {
                                        mutedRole.removeUser(user);
                                        message.reply("Unmuted " + user.getName() + ".");
                                    }
                                    return;
                                }
                            }
                            message.reply("Error: The muted roles either do not exist or have been improperly named.");
                        } else if (message.getContent().startsWith("$quadratic")) {
                            if (message.getContent().equalsIgnoreCase("$quadratic")) {
                                message.reply("```\n" +
                                        "Uses the quadratic formula to find the roots of a quadratic equation in the form ax^2 + bx + c = 0, given a, b, and c.\n" +
                                        "Syntax: \"$quadratic [a] [b] [c]\"\n" +
                                        "Example: \"$quadratic 2 4 1\"\n" +
                                        "```");
                                return;
                            }
                            String msg = message.getContent().replace("$quadratic ", "");
                            String parts[] = msg.split(" ");

                            if (parts.length < 3) {
                                message.reply("Error: The given command contains **" + parts.length + "** instead of the necessary 3.");
                                return;
                            }

                            double discriminant = Math.pow(Double.parseDouble(parts[1]), 2) - (4 * Double.parseDouble(parts[0]) * Double.parseDouble(parts[2]));
                            if (discriminant < 0) {
                                message.reply("```\n" +
                                        "The roots of quadratic equation " + parts[0] + "x² + " + parts[1] + "x + " + parts[2] + " are complex.\n" +
                                        "The discriminant of the equation is " + discriminant + ".\n" +
                                        "```");
                                return;
                            }

                            double[] roots = quadratic(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2])); //runs formula with a, b, and c parsed as doubles
                            message.reply("```\n" +
                                    "The roots of quadratic equation " + parts[0] + "x² + " + parts[1] + "x + " + parts[2] + " are " + roots[0] + " and " + roots[1] + ".\n" +
                                    "The discriminant of the equation is " + discriminant + ".\n" +
                                    "```");
                        } else if (message.getContent().startsWith("$primeFactors")) {
                            if (message.getContent().equalsIgnoreCase("$primeFactors")) {
                                message.reply("```\n" +
                                        "");
                                return;
                            }
                            String msg = message.getContent().replace("$primeFactors ", "");
                            String[] parts = msg.split(" ");

                            if (parts.length != 1) {
                                message.reply("Error: This command requires exactly one argument.");
                                return;
                            }

                            try {
                                ArrayList<Integer> factors = primeFactors(Integer.parseInt(parts[0]));
                                StringBuilder sb = new StringBuilder();

                                for (int factor : factors) {
                                    sb.append(factor + " * ");
                                }
                                String factorString = sb.substring(0, sb.length() - 3);
                                message.reply("The prime factors of " + msg + " are: \n" +
                                        "`" + factorString + "`");
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                message.reply("Error: java.lang.NumberFormatException: For input string: \"" + parts[0] + "\"\n" +
                                        "The given input string is not an integer or is out of bounds.");
                            } catch (StringIndexOutOfBoundsException e) {
                                e.printStackTrace();
                                message.reply("**" + msg + "** is prime.");
                            }

                        } else if (message.getContent().startsWith("$string")) {
                            if (message.getContent().equalsIgnoreCase("$string")) {
                                message.reply("```\n" +
                                        "A whole bunch of string utilities.\n" +
                                        "Subcommands: reverse, scramble, tumblr, find, replace, compare\n" +
                                        "Syntax: \"$string [subcommand] [string]\"\n" +
                                        "Example: \"$string #reverse olleh\"\n" +
                                        "```");
                                return;
                            }
                            String msg = message.getContent().replace("$string ", "");
                            String[] parts = msg.split(" ");

                            if (parts[0].equalsIgnoreCase("#reverse")) {
                                if (parts.length < 2) {
                                    message.reply("```\n" +
                                            "Reverses the given string.\n" +
                                            "Syntax: \"$string #reverse [string]\"\n" +
                                            "Example: \"$string #reverse olleh\"\n" +
                                            "```");
                                    return;
                                }
                                msg = msg.replaceFirst("#reverse ", "");
                                String reversedString = "";
                                for (int i = 0; i < msg.length(); i++) {
                                    reversedString += msg.charAt(msg.length() - 1 - i);
                                }
                                if (msg.length() > 980) {
                                    message.reply(reversedString);
                                    return;
                                } else {
                                    message.reply("\"" + msg + "\" reversed is: \n\"" + reversedString + "\".");
                                    return;
                                }
                            } else if (parts[0].equalsIgnoreCase("#scramble")) {
                                msg = msg.replaceFirst("#scramble ", "");

                                if (parts.length < 2) {
                                    message.reply("```\n" +
                                            "Scrambles the given string.\n" +
                                            "Syntax: \"$string #scramble [string]\"\n" +
                                            "Example: \"$string #scramble The quick brown fox jumped over the lazy dog.\"\n" +
                                            "```");
                                    return;
                                }
                                Random rand = new Random();
                                String scrambledString = scramble(rand, msg);
                                message.reply("Original string: \"" + msg + "\"\n" +
                                        "Scrambled string: \"" + scrambledString + "\"");
                            } else if (parts[0].equalsIgnoreCase("#tumblr") || parts[0].equalsIgnoreCase("#silver")) {
                                msg = msg.replaceFirst("#tumblr ", "");
                                msg = msg.replaceFirst("#silver ", "");

                                if (parts.length < 2) {
                                    message.reply("```\n" +
                                            "Randomly capitalizes letters of the given string.\n" +
                                            "Syntax: \"$string #tumblr [string]\"\n" +
                                            "Example: \"$string #tumblr I am from tumblr\"\n" +
                                            "```");
                                    return;
                                }
                                char[] chars = msg.toCharArray();
                                ArrayList<Character> charList = new ArrayList<Character>();
                                Random rand = new Random();

                                for (char c : chars) {
                                    int rng = rand.nextInt(2);
                                    if (rng == 0) {
                                        charList.add(Character.toUpperCase(c));
                                    } else {
                                        charList.add(Character.toLowerCase(c));
                                    }
                                }
                                StringBuilder sb = new StringBuilder();
                                for (char c : charList) {
                                    sb.append(c);
                                }
                                message.reply(sb.toString());
                            } else if (parts[0].equalsIgnoreCase("#find")) {
                                msg = msg.replaceFirst("#find ", "");
                                if (parts.length < 3) {
                                    message.reply("```\n" +
                                            "Finds the number of occurrances of the given substring in the given string.\n" +
                                            "Syntax: \"$string #find [substring] [string]\"\n" +
                                            "Example: \"$string #find hello The man said hello, and I said hello back. \"\n" +
                                            "```");
                                    return;
                                }
                                msg = msg.replaceFirst(parts[1], "");
                                int matches = StringUtils.countMatches(msg, parts[1]);
                                message.reply("The string \"" + parts[1] + "\" occurs **" + matches + "** times in the given string.");
                            } else if (parts[0].equalsIgnoreCase("#replace")) {
                                msg = msg.replaceFirst("#replace ", "");
                                if (parts.length < 4) {
                                    message.reply("```\n" +
                                            "Replaces all occurrances of the given substring in the given string with a different string.\n" +
                                            "Syntax: \"$string #replace [substring1] [substring2] [string]\"\n" +
                                            "Example: \"$string #replace o e grootings\"\n" +
                                            "```");
                                    return;
                                }
                                msg = msg.replaceFirst(parts[1], "");
                                msg = msg.replaceFirst(parts[2], "");

                                int matches = StringUtils.countMatches(msg, parts[1]);
                                message.reply("**" + matches + "** matches.");
                                msg = msg.replaceAll(parts[1], parts[2]);
                                message.reply("```\n" +
                                        msg + "\n" +
                                        "```");
                                if (msg.length() > 1994) {
                                    message.reply("The resulting string is too long and cannot be sent back through discord.");
                                }
                            } else if (parts[0].equalsIgnoreCase("#compare")) {
                                msg = msg.replaceFirst("#compare ", "");
                                if (parts.length < 3) {
                                    message.reply("```\n" +
                                            "Compares two strings, finding the Levenshtein distance and the difference between the two.\n" +
                                            "For information: https://en.wikipedia.org/wiki/Levenshtein_distance \n" +
                                            "Syntax: \"$string #compare [string1] ; [string2]\"\n" +
                                            "Example: \"$string #compare I said goodbye ; I said hello\"\n" +
                                            "```");
                                    return;
                                }
                                String[] strings = msg.split(";");

                                if (strings.length < 2) {
                                    message.reply("Error: This command requires exactly two strings to compare. They must be separated by a semicolon.\n" +
                                            "Example: \"$string #compare hello ; jello");
                                }

                                strings[0] = strings[0].trim(); //should remove leading and trailing spaces
                                strings[1] = strings[1].trim();

                                int distance = StringUtils.getLevenshteinDistance(strings[0], strings[1]);
                                String difference = StringUtils.difference(strings[0], strings[1]);
                                message.reply("```\n" +
                                        "The Levenshtein distance between the two strings is " + distance + ".\n" +
                                        "The difference between the two strings is \"" + difference + "\".\n" +
                                        "```");
                            }
                        } else if (message.getContent().startsWith("$simplify")) {
                            if (message.getContent().equalsIgnoreCase("$simplify")) {
                                message.reply("```\n" +
                                        "Simplifies the given fraction. Takes two integers.\n" +
                                        "Syntax: \"$simplify [numerator] [denominator]\"\n" +
                                        "Example: \"$simplify 12 14\"\n" +
                                        "```");
                                return;
                            }
                            String msg = message.getContent().replace("$simplify ", "");
                            String[] parts = msg.split(" ");
                            if (parts.length != 2) {
                                message.reply("Error: This command requires exactly **2** parts.");
                                return;
                            }
                            int num = Integer.parseInt(parts[0]);
                            int denom = Integer.parseInt(parts[1]);
                            int[] fraction = simplifyFraction(num, denom);

                            if (fraction.length == 0) {
                                message.reply("The given fraction is already simplified.");
                                return;
                            }

                            num = fraction[0];
                            denom = fraction[1];

                            message.reply("The given fraction simplifies to **" + num + "/" + denom + "**.");
                        } else if (message.getContent().startsWith("$ascii")) {
                            if (message.getContent().equalsIgnoreCase("$ascii")) {
                                message.reply("```\n" +
                                        "Returns ascii text of a message. Separate the text and the font with a semicolon.\n" +
                                        "Syntax: \"$ascii [text]; [font]\"\n" +
                                        "Example: \"$ascii hello; gothic\"\n" +
                                        "```");
                                return;
                            }
                            String msg = message.getContent().replace("$ascii ", "");
                            String[] parts = msg.split(";");
                            if (parts.length != 2) {
                                message.reply("Error: This command requires exactly two parts.");
                                return;
                            }
                            String text = parts[0].trim().replace(" ", "+");
                            String font = parts[1].trim();
                            System.out.println(text + " || " + font);
                            try {
                                message.reply("```\n" +
                                        asciiGet(text, font) + "\n" +
                                        "```");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (message.getContent().startsWith("$factorial")) {
                            if (message.getContent().equalsIgnoreCase("$factorial")) {
                                message.reply("```\n" +
                                        "Returns the factorial of an integer. Works for all positive values up to 170.\n" +
                                        "Syntax: \"$factorial [integer]\"\n" +
                                        "Example: \"$factorial 12\"\n" +
                                        "```");
                                return;
                            }
                            String msg = message.getContent().replace("$factorial ", "");
                            double result = factorial(Integer.parseInt(msg));

                            message.reply(msg + "! = *" + String.format("%.0f", result) + "*");
                        } else if (message.getContent().startsWith("$rng")) {
                            if (message.getContent().equalsIgnoreCase("$rng")) {
                                message.reply("```\n" +
                                        "Computes a number of random numbers between 1 and a given bound.\n" +
                                        "Syntax: \"$rng [amount] [max]\"\n" +
                                        "Example: \"$rng 3 10\"\n" +
                                        "If only one argument is given, it will be considered the upper bound and the amount will be assumed to be 1.\n" +
                                        "If no arguments are given, it will be considered as 1 random number between 1 and 10. (In theory)\n" +
                                        "```");
                                return;
                            }
                            String msg = message.getContent().replace("$rng ", "");
                            String[] parts = msg.split(" ");
                            if (parts.length == 0) {
                                message.reply("```\n" +
                                        "Creating 1 random number between 1 and 10.\n" +
                                        rng(10) + "\n" +
                                        "```");
                            } else if (parts.length == 1) {
                                int rng = rng(Integer.parseInt(parts[0]));
                                message.reply("```\n" +
                                        "Creating 1 random number between 1 and " + parts[0] + ".\n" +
                                        rng + "\n" +
                                        "```");
                            } else {
                                ArrayList<Integer> numbers = rng(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
                                StringBuilder sb = new StringBuilder();
                                for (Integer number : numbers) {
                                    sb.append(number + ", ");
                                }
                                sb.delete(sb.length() - 2, sb.length()); //deletes last comma and space
                                message.reply("```\n" +
                                        "Creating " + parts[0] + " random numbers between 1 and " + parts[1] + ".\n" +
                                        sb.toString() + "\n" +
                                        "```");
                            }
                        } else if (message.getContent().startsWith("$factors")) {
                            if (message.getContent().equalsIgnoreCase("$factors")) {
                                message.reply("```\n" +
                                        "");
                                return;
                            }
                            String msg = message.getContent().replace("$factors ", "");
                            String[] parts = msg.split(" ");

                            if (parts.length != 1) {
                                message.reply("Error: This command requires exactly one argument.");
                                return;
                            }

                            try {
                                ArrayList<Integer> factors = factors(Integer.parseInt(parts[0]));
                                StringBuilder sb = new StringBuilder();

                                for (int factor : factors) {
                                    sb.append(factor + " * ");
                                }
                                String factorString = sb.substring(0, sb.length() - 3);
                                message.reply("The factors of " + msg + " are: \n" +
                                        "`" + factorString + "`");
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                message.reply("Error: java.lang.NumberFormatException: For input string: \"" + parts[0] + "\"\n" +
                                        "The given input string is not an integer or is out of bounds.");
                            } catch (StringIndexOutOfBoundsException e) {
                                e.printStackTrace();
                                message.reply("**" + msg + "** is prime.");
                            }

                        } else if (message.getContent().startsWith("$leetspeak")) {
                            if (message.getContent().equalsIgnoreCase("$leetspeak")) {
                                message.reply("```\n" +
                                        "Translates a given string into leetspeak.\n" +
                                        "Syntax: \"$leetspeak [string]\"\n" +
                                        "Example: \"$leetspeak silver is the frenchiest fry\"\n" +
                                        "```");
                                return;
                            }
                            String msg = message.getContent().replace("$leetspeak ", "");
                            String leetspeak = "";
                            if (msg.contains("il") || msg.contains("li")) {
                                String[] letters = new String[]{"a", "b", "e", "g", "i", "l", "o", "s", "t", "z"};
                                String[] numbers = new String[]{"4", "8", "3", "6", "1", "l", "0", "5", "7", "2"}; //only translates 'i' to help avoid possible confusion
                                leetspeak = StringUtils.replaceEach(msg, letters, numbers);
                            } else {
                                String[] letters = new String[]{"a", "b", "e", "g", "i", "l", "o", "s", "t", "z"};
                                String[] numbers = new String[]{"4", "8", "3", "6", "1", "l", "0", "5", "7", "2"};
                                leetspeak = StringUtils.replaceEach(msg, letters, numbers);
                            }

                            message.reply("`" + leetspeak + "`");
                            return;
                        } else if (message.getContent().startsWith("$game")) {
                            if (message.getContent().equalsIgnoreCase("$game")) {
                                message.reply("```\n" +
                                        "Allows users to interface with SoupBot's gaming module.\n" +
                                        "Current list of games: guessnumber, hitlerman\n" +
                                        "Syntax: \"$game [game]\"\n" +
                                        "Example \"$game guessnumber\"\n" +
                                        "```");
                                return;
                            }
                            String[] parts = message.getContent().split(" ");
                        /*if (parts.length > 2) {                                          //this is probably unnecessary
                            message.reply("Error: Command must have less than 3 parts.");
                            return;
                        }*/
                            String game = message.getContent().replace("$game ", "");

                            String username = message.getAuthor().getName();
                            if (game.equalsIgnoreCase("guessnumber")) {
                                if (games[0] == false) {
                                    game0UserID = message.getAuthor().getId();
                                    message.reply(("User " + username + " has challenged SoupBot to a number guessing game!\n\n" +
                                            "```\n" +
                                            "I've generated a random number between 0 and 100.\n" +
                                            "The challenger will guess what the number is with \"$guess [x]\"\n" +
                                            "I will then tell the challenger if the number is higher or lower than their guess.\n" +
                                            "Use \"$quit\" to exit the game." +
                                            "```\n" +
                                            "The game begins now!"));
                                    Random rand = new Random();
                                    game0Num = rand.nextInt(100) + 1;
                                    game0Turns = 0;
                                    System.out.println(game0Num);
                                    games[0] = true;
                                } else {
                                    message.reply("User " + username + " has already started a number guessing game with Soupbot!\n" +
                                            "Please wait until this game is finished before trying to start another one.");
                                }
                            } else if (game.startsWith("hitlerman")) {
                                if (!games[1]) {
                                    String wordList = "";
                                    if (parts.length == 2) {
                                        wordList = "default";
                                    } else if (parts.length == 3) {
                                        wordList = parts[2];
                                    }
                                    try {
                                        ArrayList<String> words = getArrayListOfLines("hangman/hangman_" + wordList + ".txt");
                                        Random rand = new Random();
                                        int index = rand.nextInt(words.size());
                                        game1Word = words.get(index).toLowerCase();
                                        game1UnusedChars.clear();
                                        for (char c = 'a'; c != '{'; c++) { //fills it with 26 letters of alphabet
                                            game1UnusedChars.add(c);
                                        }
                                        game1lives = 8;
                                        game1MaxLives = 8;
                                        System.out.println(game1Word);
                                        game1GuessedWord.clear();
                                        game1ActualWord.clear();
                                        game1HelpfulUsers.clear();
                                        game1TurnNumber = 0;

                                        String blanks = multiplyString("_", game1Word.length());

                                        game1GuessedWord = arrayListFromArray(blanks.toCharArray());
                                        game1ActualWord = arrayListFromArray(game1Word.toCharArray());
                                        ArrayList<Integer> spaces = indexesOfCharInArrayList(' ', game1ActualWord);

                                        for (int spaceIndex : spaces) {
                                            game1GuessedWord.set(spaceIndex, ' '); //makes the spaces not underscores
                                        }

                                        message.reply("Stop Hitler from being born by guessing the word in this hangman-style game!\n" +
                                                "Guess letters with \"$guess [letter]\"\n" +
                                                "Try to guess the word with \"$guessword [word]\"\n" +
                                                "You can quit the game with \"$quit\"\n" +
                                                "Use \"$resend\" to resend information if it doesn't send.\n" +
                                                "The game begins now!\n" +
                                                makeAsciiSpermEgg(game1lives) + "\n" +
                                                "```\n" +
                                                arrayListAsStringForHitlermanGuess(game1GuessedWord) + "\n" +
                                                "```");
                                        games[1] = true;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        message.reply("Something went wrong. The most likely problem is in the name of the word list. " +
                                                "Currently, supported word lists include default, videogames, and pokemon.\n" +
                                                "If only two parts have been supplied, the problem is something else.");
                                    }

                                } else {
                                    message.reply("There is already an active game of Hitlerman. Please wait until it is finished.");
                                }
                            } else {
                                message.reply("Error: The given game either doesn't exist or is spelled incorrectly.");
                                return;
                            }

                        } else if (message.getContent().startsWith("$google")) { //featuring the worst way ever to implement this
                            if (message.getContent().equalsIgnoreCase("$google")) {
                                message.reply("```\n" +
                                        "Do a google search or something.\n" +
                                        "Syntax: \"$google [query]\"\n" +
                                        "Example: \"$google cats\"" +
                                        "```");
                                return;
                            }
                            String query = message.getContent().replace("$google ", "");
                            query = query.replace("+", "%2B");
                            query = query.replace(' ', '+'); //formatting for google search
                            try {
                                Document doc = Jsoup.connect("https://www.google.com/search?q=" + query + "&num=5").userAgent("SoupBot").get();
                                Elements links = doc.select("a[href]");
                                Elements meaningfulLinks = new Elements();

                                for (Element link : links) {
                                    if (link.toString().startsWith("<a href=\"/url?")) {
                                        meaningfulLinks.add(link);
                                    }
                                }
                                int elementNo = 0;
                                for (Element link : meaningfulLinks) {
                                    boolean foundALink = false;
                                    if (link.text().contains("wikipedia")) {
                                        elementNo = links.indexOf(link);
                                        foundALink = true;
                                    }
                                    if (foundALink) break;
                                }
                                String href = meaningfulLinks.get(elementNo).attr("abs:href");
                                System.out.println(href);
                                message.reply(href);
                                //okitworksfornow
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (message.getContent().startsWith("$cat")) {
                            String url = "";
                            int number = 0;
                            if (message.getContent().equalsIgnoreCase("$cat")) {
                                Random rand = new Random();
                                number = rand.nextInt(365);
                                url = "http://www.iscalio.com/cats/" + number + ".jpg";
                                message.reply(url);
                            } else {
                                String[] parts = message.getContent().split(" ");
                                number = Integer.parseInt(parts[1]);
                                url = "http://www.iscalio.com/cats/" + number + ".jpg";
                                message.reply(url);
                            /*try {
                                Document img = Jsoup.connect(url).get();
                                message.reply(img.html());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/
                            }
                        /*try {
                            BufferedImage img = ImageIO.read(new URL(url));
                            System.out.println(ClassLoader.getSystemResource("images/donotdelete").getPath());
                            String path = ClassLoader.getSystemResource("images/donotdelete").getPath().substring(0, ClassLoader.getSystemResource("images/donotdelete").getPath().lastIndexOf("donotdelete"));
                            File file = new File(path + number + ".jpg");
                            if (file.exists()) {
                                message.getChannelReceiver().sendFile(file);
                            } else {
                                ImageIO.write(img, "jpg", file);
                                Thread.sleep(500);
                                message.getChannelReceiver().sendFile(file);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        } else if (message.getContent().equalsIgnoreCase("$onlineusers")) {
                            ArrayList<ArrayList<String>> arrayLists = getListsOfOnlineUsers(api1, message.getChannelReceiver());

                            ArrayList<String> online = arrayLists.get(0);
                            ArrayList<String> idle = arrayLists.get(1);
                            ArrayList<String> dnd = arrayLists.get(2);
                            ArrayList<String> bots = arrayLists.get(3);

                            int longestLength = 0;

                            for (ArrayList<String> arrayList : arrayLists) {
                                for (String s : arrayList) {
                                    if (s.length() > longestLength) {
                                        longestLength = s.length();
                                    }
                                }
                            }

                            ArrayList<String>[] userLists = new ArrayList[]{online, idle, dnd, bots};
                            int columnWidth = longestLength + 8; //leaves some space between columns
                            if (columnWidth > 36) {
                                columnWidth = 36; //caps column width at 36
                            }
                            String outputString = "```\n" +
                                    formatListsAsColumns(userLists, columnWidth) +
                                    "```";
                            Future<Message> isSent = message.reply("```\n" +
                                    "ONLINE" + multiplyString(" ", columnWidth - "ONLINE".length()) +
                                    "IDLE" + multiplyString(" ", columnWidth - "IDLE".length()) +
                                    "DO NOT DISTURB" + multiplyString(" ", columnWidth - "DO NOT DISTURB".length()) +
                                    "BOTS" + multiplyString(" ", columnWidth - "BOTS".length()) +
                                    "```");
                            while (!isSent.isDone()) {
                            } //should only continue once message sends
                            message.reply(outputString);
                        } else if (message.getContent().startsWith("$addxp")) {
                            if (message.getContent().equals("$addxp")) {
                                message.reply("```\n" +
                                        "Adds a given number of xp.\n" +
                                        "Syntax: \"$addxp [num] [user]\"\n" +
                                        "Example: \"$addxp 5 @TornadoOfSoup\"\n" +
                                        "If a username is not provided, xp will be given to the command sender.\n" +
                                        "```");
                                return;
                            }
                            if (memebot.isOnList(message.getAuthor().getName(), whitelist)) {
                                String id = message.getAuthor().getId();
                                String[] parts = message.getContent().split(" ");

                                if (parts.length == 2) {
                                    if (statsFileExists(id)) {
                                        try {
                                            UserStats userStats = loadStats(id);
                                            userStats.addExp(Integer.parseInt(parts[1]));
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        UserStats userStats = new UserStats(id);
                                        userStats.addExp(Integer.parseInt(parts[1]));
                                    }
                                    message.reply("Added " + parts[1] + " exp to user " + message.getAuthor().getName() + ".");
                                } else if (parts.length == 3) {
                                    List<User> mentions = message.getMentions();
                                    if (!mentions.isEmpty()) {
                                        if (statsFileExists(mentions.get(0).getId())) {
                                            try {
                                                UserStats userStats = loadStats(id);
                                                userStats.addExp(Integer.parseInt(parts[1]));
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            UserStats userStats = new UserStats(mentions.get(0).getId());
                                            userStats.addExp(Integer.parseInt(parts[1]));
                                        }
                                        message.reply("Added " + parts[1] + " exp to user " + mentions.get(0).getName() + ".");
                                    } else {
                                        message.reply("Error: No mentions found.");
                                    }

                                } else {
                                    message.reply("Error: Command must have three or fewer parts.");
                                }
                            }
                        } else if (message.getContent().startsWith("$getstats")) {
                            if (message.getContent().equalsIgnoreCase("$getstats")) {
                                if (statsFileExists(message.getAuthor().getId())) {
                                    try {
                                        message.reply(formatStats(api1, LoadUserStats.loadStats(message.getAuthor().getId())));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    UserStats userStats = new UserStats(message.getAuthor().getId());
                                    message.reply(formatStats(api1, userStats));
                                }
                            } else {
                                List<User> mentions = message.getMentions();
                                if (mentions.isEmpty()) {
                                    message.reply("Error: Command must include one mention.");
                                } else {
                                    if (statsFileExists(mentions.get(0).getId())) {
                                        try {
                                            message.reply(formatStats(api1, loadStats(mentions.get(0))));
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        UserStats userStats = new UserStats(mentions.get(0).getId());
                                        message.reply(formatStats(api1, userStats));
                                    }
                                }
                            }
                        } else if (message.getContent().startsWith("$setxp")) {
                            if (message.getContent().equalsIgnoreCase("$setxp")) {
                                message.reply("```\n" +
                                        "Sets player's xp to a given amount.\n" +
                                        "Syntax: \"$setxp [player] [x]\"\n" +
                                        "Example: \"$setxp @TornadoOfSoup 50\"\n" +
                                        "If a player is not provided, xp will be given to the command sender.");
                                return;
                            } else {
                                List<User> mentions = message.getMentions();
                                String[] parts = message.getContent().split(" ");
                                if (mentions.size() > 0) {
                                    try {
                                        UserStats userStats = loadStats(mentions.get(0));
                                        userStats.setExp(Integer.parseInt(parts[1]));
                                        message.reply("Set user " + mentions.get(0).getName() + "'s xp to " + parts[2] + ".");
                                        while (userStats.canLevelUp()) {
                                            userStats.levelUpIfPossible();
                                            levelUpDialog(message.getChannelReceiver(), mentions.get(0), userStats);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                        message.reply("Error: Please make sure an integer is provided.\n" +
                                                "Syntax: \"$setxp [player] [x]\"\n");
                                    }
                                } else {
                                    try {
                                        UserStats userStats = LoadUserStats.loadStats(message.getAuthor());
                                        userStats.setExp(Integer.parseInt(parts[1]));
                                        message.reply("Set user " + message.getAuthor().getName() + "'s xp to " + parts[1] + ".");
                                        while (userStats.canLevelUp()) {
                                            userStats.levelUpIfPossible();
                                            levelUpDialog(message.getChannelReceiver(), message.getAuthor(), userStats);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                        message.reply("Error: Please make sure an integer is provided.\n" +
                                                "Syntax: \"$setxp [player] [x]\"\n");
                                    }
                                }
                            }
                        } else if (message.getContent().startsWith("$setlevel")) {
                            if (message.getContent().equalsIgnoreCase("$setlevel")) {
                                message.reply("```\n" +
                                        "Sets the level of a player to the given value." +
                                        "Syntax: \"$setlevel [player] [x]\"\n" +
                                        "Example: \"$setlevel @TornadoOfSoup 5\"\n" +
                                        "If a player is not given, the level of the command sender will be set.\n" +
                                        "```");
                                return;
                            }
                            List<User> mentions = message.getMentions();
                            String[] parts = message.getContent().split(" ");
                            if (!mentions.isEmpty()) {
                                try {
                                    UserStats userStats = loadStats(mentions.get(0));
                                    userStats.setLevel(Integer.parseInt(parts[2]));
                                    message.reply("Set level of user " + mentions.get(0).getName() + " to " + parts[2] + ".");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    UserStats userStats = LoadUserStats.loadStats(message.getAuthor());
                                    userStats.setLevel(Integer.parseInt(parts[1]));
                                    message.reply("Set level of user " + message.getAuthor().getName() + " to " + parts[1] + ".");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        } else if (message.getContent().startsWith("$setpotentiorbs")) {
                            if (message.getContent().equalsIgnoreCase("$setpotentiorbs")) {
                                message.reply("```\n" +
                                        "Sets player's potentiorb count to a given amount.\n" +
                                        "Syntax: \"$setpotentiorbs [player] [x]\"\n" +
                                        "Example: \"$setpotentiorbs @TornadoOfSoup 15\"\n" +
                                        "If a player is not provided, potentiorbs will be given to the command sender.");
                                return;
                            } else {
                                List<User> mentions = message.getMentions();
                                String[] parts = message.getContent().split(" ");
                                if (mentions.size() > 0) {
                                    try {
                                        UserStats userStats = loadStats(mentions.get(0));
                                        userStats.setExp(Integer.parseInt(parts[1]));
                                        message.reply("Set user " + mentions.get(0).getName() + "'s potentiorb count to " + parts[2] + ".");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                        message.reply("Error: Please make sure an integer is provided.\n" +
                                                "Syntax: \"$setpotentiorbs [player] [x]\"\n");
                                    }
                                } else {
                                    try {
                                        UserStats userStats = LoadUserStats.loadStats(message.getAuthor());
                                        userStats.setExp(Integer.parseInt(parts[1]));
                                        message.reply("Set user " + message.getAuthor().getName() + "'s potentiorb count to " + parts[1] + ".");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                        message.reply("Error: Please make sure an integer is provided.\n" +
                                                "Syntax: \"$setpotentiorbs [player] [x]\"\n");
                                    }
                                }
                            }
                        } else if (message.getContent().startsWith("$newpet")) {
                            try {
                                UserStats userStats = LoadUserStats.loadStats(message.getAuthor());
                                if (userStats.getPets().get(0).equals(null)) {

                                    if (message.getContent().equalsIgnoreCase("$newpet")) {
                                        Random rand = new Random();
                                        int petSpecies = rand.nextInt(2);
                                        Pet pet;
                                        switch (petSpecies) {
                                            case 0:
                                                pet = new Pet(Species.DOG);
                                                break;
                                            case 1:
                                                pet = new Pet(Species.CAT);
                                                break;
                                            default:
                                                pet = new Pet(Species.DOG);
                                                break;
                                        }

                                        userStats.addPet(pet);
                                        String outputString = formatPetStats(api1, pet);
                                        message.reply(outputString);
                                    } else {
                                        String name = message.getContent().replace("$newpet ", "");
                                        Random rand = new Random();
                                        int petSpecies = rand.nextInt(2);
                                        Pet pet;
                                        switch (petSpecies) {
                                            case 0:
                                                pet = new Pet(Species.DOG, name);
                                                break;
                                            case 1:
                                                pet = new Pet(Species.CAT, name);
                                                break;
                                            default:
                                                pet = new Pet(Species.DOG, name);
                                                break;
                                        }
                                        userStats.addPet(pet);
                                        String outputString = formatPetStats(api1, pet);
                                        message.reply(outputString);
                                    }
                                } else {
                                    message.reply("You already have a pet.");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                message.reply("Something went wrong. \n" + e.toString());
                            }
                        } else if (message.getContent().startsWith("$getpets")) {
                            if (message.getContent().equalsIgnoreCase("$getpets")) {
                                try {
                                    UserStats userStats = LoadUserStats.loadStats(message.getAuthor());
                                    ArrayList<Pet> pets = userStats.getPets();

                                    for (Pet pet : pets) {
                                        formatPetStats(api1, pet);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    message.reply("Something went wrong. \n" + e.toString());
                                }
                            } else {
                                List<User> mentions = message.getMentions();
                                if (mentions.isEmpty()) {
                                    message.reply("Error: Command must include one mention.");
                                } else {

                                    try {
                                        UserStats userStats = loadStats(mentions.get(0));
                                        ArrayList<Pet> pets = userStats.getPets();

                                        for (Pet pet : pets) {
                                            formatPetStats(api1, pet);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        message.reply("Something went wrong. \n" + e.toString());
                                    }

                                }
                            }
                        } else if (message.getContent().startsWith("$clearpets")) {
                            if (message.getContent().equalsIgnoreCase("$clearpets")) {
                                message.reply("```\n" +
                                        "Clears all pets. Do not use unless you are sure.\n" +
                                        "Syntax: \"$clearpets [your name]\"\n" +
                                        "Example: \"$clearpets TornadoOfSoup\"\n" +
                                        "Note: Must be your Discord name, not your server nickname.\n" +
                                        "```");
                                return;
                            } else if (message.getContent().replace("$clearpets ", "").equals(message.getAuthor().getName())) {
                                try {
                                    UserStats userStats = LoadUserStats.loadStats(message.getAuthor());
                                    userStats.clearPets();
                                    message.reply("Cleared all pets of user " + message.getAuthor().getName());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    message.reply("Something went wrong. \n" + e.toString());
                                }
                            } else {
                                message.reply("```\n" +
                                        "Clears all pets. Do not use unless you are sure.\n" +
                                        "Syntax: \"$clearpets [your name]\"\n" +
                                        "Example: \"$clearpets TornadoOfSoup\"\n" +
                                        "Note: Must be your Discord name, not your server nickname.\n" +
                                        "```");
                            }
                        } else if (message.getContent().equalsIgnoreCase("$tos")) {
                            long cooldownLengthInMinutes = 15;  //15 minutes
                            long cooldownLength = cooldownLengthInMinutes * 1000 * 60;
                            System.out.println("tos: " + message.getAuthor().getName() + " | " + message.getAuthor().getId());
                            if (message.getAuthor().getId().equals("190519404780322818")) { //if the message author is nick | 190519404780322818
                                System.out.println("it's a nick");
                                if (System.currentTimeMillis() < (cooldown[1] + (cooldownLength * 12)) && System.currentTimeMillis() != 0) { //multiply cooldown by 12
                                    message.reply("Because you're Nick, this command is on cooldown for another " + Math.abs(((System.currentTimeMillis() - cooldown[1]) / 1000) - 3600) + " seconds.");
                                    return;
                                }
                            }
                            ArrayList<String> links = new ArrayList<String>(Arrays.asList(
                                    "https://cdn.discordapp.com/attachments/189359733377990656/350089070455816192/unknown.png",
                                    "https://cdn.discordapp.com/attachments/189359733377990656/350088340806172684/unknown.png",
                                    "https://cdn.discordapp.com/attachments/189359733377990656/350088483135815700/unknown.png",
                                    "https://cdn.discordapp.com/attachments/189359733377990656/350088669400399882/unknown.png",
                                    "https://cdn.discordapp.com/attachments/189359733377990656/350088852389625856/unknown.png"
                            ));

                            if (System.currentTimeMillis() > (cooldown[1] + cooldownLength)) {
                                cooldown[1] = System.currentTimeMillis();

                                try {
                                    message.reply("Rickl: @here ToSOsotOSToSOToSToSOtoS");
                                    Thread.sleep(1000);
                                    message.reply(getRandomObjectFromArrayList(links).toString());
                                    Thread.sleep(1000);
                                    message.reply("\uD835\uDD1A\uD835\uDD22\uD835\uDD31 \uD835\uDD10\uD835\uDD22\uD835\uDD2A\uD835\uDD22\uD835\uDD30: tos or die");
                                    Thread.sleep(500);
                                    message.reply("FraggerX123: TOS @everyoon");
                                    message.reply(getRandomObjectFromArrayList(links).toString());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                message.reply("This command is on cooldown for another " + Math.abs(((System.currentTimeMillis() - cooldown[1]) / 1000) - 600) + " seconds.");
                            }

                        } else if (message.getContent().startsWith("$addlogo")) {
                            boolean hasAcceptableSuffix = false;
                            String logoName = "soupbot.png";
                            String[] parts = message.getContent().split(" ");
                            if (message.getAttachments().size() == 0) {
                                message.reply("```\n" +
                                        "Adds SoupBot logo to given image." +
                                        "Requires one of the following suffixes: .jpg, .png, .gif, .bmp\n" +
                                        "Syntax: \"$addlogo [picture]\"\n" +
                                        "Make sure an image is sent with the command.\n" +
                                        "```");
                                return;
                            }
                            for (MessageAttachment attachment : message.getAttachments()) {
                                if (stringEndsWithFromArray(attachment.getFileName(), new String[]{".jpg", ".png", ".gif", ".bmp"})) {
                                    System.out.println("String has correct suffix");
                                    hasAcceptableSuffix = true;
                                    try {
                                        String path = ClassLoader.getSystemResource("images/donotdelete").getPath().substring(0, ClassLoader.getSystemResource("images/donotdelete").getPath().lastIndexOf("donotdelete"));
                                        URLConnection urlConnection = attachment.getUrl().openConnection();
                                        urlConnection.setRequestProperty("User-Agent",
                                                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
                                        BufferedImage img = ImageIO.read(urlConnection.getInputStream());
                                        if (parts.length > 1) {
                                            if (parts[1].equalsIgnoreCase("soupbot"))
                                                logoName = "soupbot.png";
                                            else if (parts[1].equalsIgnoreCase("rem"))
                                                logoName = "rem.png";
                                            else
                                                logoName = "soupbot.png";
                                        }
                                        BufferedImage logo = ImageIO.read(new File(path + logoName));
                                        if (logo.getWidth() > img.getWidth() / 6) {
                                            double scaleFactor = ((double) img.getWidth() / 6) / logo.getWidth();
                                            logo = resizeBufferedImage(logo, scaleFactor);
                                        }
                                        BufferedImage result = addLogoToImage(img, logo);
                                        File file = new File(System.currentTimeMillis() + attachment.getFileName() + ".png");
                                        ImageIO.write(result, "PNG", file);

                                        message.getChannelReceiver().sendFile(file);
                                    } catch (Exception e) {
                                        message.reply(e.getMessage());
                                        e.printStackTrace();
                                    } finally {
                                        if (!hasAcceptableSuffix) {
                                            message.reply("Make sure your image is of a valid file format.\n" +
                                                    "Valid file formats include .jpg, .png, .gif, .bmp");
                                        }
                                    }
                                }
                            }
                        } else if (message.getContent().startsWith("$identify")) {
                            if (message.getContent().equalsIgnoreCase("$identify") && message.getAttachments().size() < 1) {
                                message.reply("```\n" +
                                        "Passes the provided image to the Clarifai image recognition API.\n" +
                                        "Will take a link or an image.\n" +
                                        "Syntax: \"$identify [link/image]\"\n" +
                                        "Example: \"$identify http://iscalio.com/cats/0.jpg\"\n" +
                                        "If sending an image, make sure it is sent with the command.\n" +
                                        "```");
                                return;
                            }

                            final ClarifaiClient client = new ClarifaiBuilder("a12e63a5994d4261a02d42c8640b20ad")
                                    .client(new OkHttpClient()) // OPTIONAL. Allows customization of OkHttp by the user
                                    .buildSync();

                            String link = "link";
                            if (message.getContent().equalsIgnoreCase("$identify") && message.getAttachments().size() > 0) {
                                for (MessageAttachment attachment : message.getAttachments()) {
                                    if (stringEndsWithFromArray(attachment.getFileName(), new String[]{".jpg", ".png", ".gif", ".bmp", ".tiff"})) {
                                        System.out.println("Uploaded picture " + attachment.getFileName() + " is of a valid file format.");
                                        link = attachment.getUrl().toString();
                                    }
                                }
                            }
                            String[] parts = message.getContent().split(" ");
                            if (parts.length > 1) {
                                UrlValidator urlValidator = new UrlValidator();
                                if (urlValidator.isValid(parts[1])) {
                                    link = parts[1];
                                } else {
                                    message.reply("Error: Please input a valid URL.");
                                }

                            }
                            if (!link.equals("link")) {
                                List<ClarifaiOutput<Concept>> predictionResults =
                                        client.getDefaultModels().generalModel() // You can also do Clarifai.getModelByID("id") to get custom models
                                                .predict()
                                                .withInputs(
                                                        ClarifaiInput.forImage(ClarifaiImage.of(link))
                                                )
                                                .executeSync() // optionally, pass a ClarifaiClient parameter to override the default client instance with another one
                                                .get();
                                for (ClarifaiOutput clarifaiOutput : predictionResults) {
                                    ArrayList<String> names = new ArrayList<String>();
                                    ArrayList<String> percentages = new ArrayList<String>();
                                    for (Object o : clarifaiOutput.data()) {
                                        System.out.println(o);
                                        names.add(parseClarifaiConcept(o.toString())[0]);
                                        percentages.add(parseClarifaiConcept(o.toString())[1]);
                                    }
                                    names.add(0, "NAMES");
                                    names.add(1, "");

                                    percentages.add(0, "PERCENTAGES");
                                    percentages.add(1, "");

                                    names = truncateArrayList(names, 11);
                                    percentages = truncateArrayList(percentages, 11);

                                    message.reply("```\n" +
                                            formatListsAsColumns(new ArrayList[]{names, percentages}, 24) + "\n" +
                                            "```");
                                }

                            } else {
                                message.reply("An error occurred.");
                            }
                        } else if (message.getContent().startsWith("$whatlist")) {
                            String user = message.getContent().replace("$whatlist ", "");
                            if (isOnList(user, whitelist)) {
                                message.reply(user + " is on the whitelist.");
                            } else if (isOnList(user, promotedList)) {
                                message.reply(user + " is on the promoted list.");
                            } else {
                                message.reply(user + " is not on any lists.");
                            }
                        } else if (message.getContent().startsWith("$tobinary")) {
                            if (message.getContent().equalsIgnoreCase("$tobinary")) {
                                message.reply("```\n" +
                                        "Converts a number or string to binary.\n" +
                                        "Syntax: \"$tobinary [number or string]\"\n" +
                                        "Example: \"$tobinary 225\"\n" +
                                        "Note: Anything that isn't entirely a number will be handled as a string.\n" +
                                        "```");
                                return;
                            } else {
                                String msg = message.getContent().replace("$tobinary ", "");
                                String reply;
                                if (msg.matches("(?:\\d*)?\\d+")) {
                                    reply = toBinary(new BigInteger(msg));
                                } else {
                                    reply = asciiToBinary(msg);
                                }
                                if (msg.length() < 1990) {
                                    message.reply("```\n" +
                                            reply + "\n" +
                                            "```");
                                } else {
                                    message.reply("The resulting message was longer than 2000 characters and therefore cannot be sent. " +
                                            "Proper handling is planned for this, but has not yet been implemented."); //TODO implement it lol
                                }
                            }
                        } else if (message.getContent().startsWith("$frombinary")) {
                            if (message.getContent().equalsIgnoreCase("$frombinary")) {
                                message.reply("```\n" +
                                        "Converts a binary string to a number or string.\n" +
                                        "Syntax: \"$frombinary [\"num\" or \"string\"] [binary]\"\n" +
                                        "Example: \"$frombinary num 1000101\"\n" +
                                        "Note: If the first argument is left out, it is assumed to be a string.\n" +
                                        "```");
                                return;
                            } else {
                                String msg = message.getContent().replace("$frombinary ", "");
                                if (msg.startsWith("num ")) {
                                    msg = msg.replace("num ", "");
                                    if (msg.contains(" ")) {
                                        message.reply("Numbers cannot contain spaces.");
                                        return;
                                    }
                                    message.reply("```\n" +
                                            binaryToDecimal(msg).toString() +
                                            "\n" +
                                            "```");
                                } else {
                                    if (msg.startsWith("string ")) {
                                        msg.replace("string ", "");
                                    }
                                    message.reply("```\n" +
                                            binaryToAscii(msg) +
                                            "\n" +
                                            "```");
                                }
                            }
                        } else if (message.getContent().equalsIgnoreCase("$makeStoryChannel")) {
                            if (!memebot.isOnList(message.getAuthor().getName(), whitelist) && !memebot.isOnList(message.getAuthor().getName(), promotedList)) {
                                message.reply("Error: You don't have the necessary permissions to run this command.");
                                return;
                            }
                            if (!memebot.isOnList(message.getChannelReceiver().getId(), storyGameChannelIDs)) {
                                storyGameChannelIDs.add(message.getChannelReceiver().getId());
                                message.reply("Channel `" + message.getChannelReceiver().getName() + "` has been added to the list of story channels.");
                            } else {
                                message.reply("Error: Channel `" + message.getChannelReceiver().getName() + "` is already on the list of story channels.");
                            }
                        } else if (message.getContent().equalsIgnoreCase("$printStory")) {
                            if (memebot.isOnList(message.getChannelReceiver().getId(), storyGameChannelIDs)) {
                                if (storyGameStories.containsKey(message.getChannelReceiver().getId())) {
                                    StringBuilder story = storyGameStories.get(message.getChannelReceiver().getId());
                                    message.reply("Story from `" + message.getChannelReceiver().getName() + "` as of on " + LocalDate.now() + " at " + LocalTime.now() + ": \n" +
                                            "```\n" +
                                            story.toString() + "\n" +
                                            "```");
                                } else {
                                    message.reply("Error: The story in channel `" + message.getChannelReceiver().getName() + "` is empty!");
                                }
                            } else {
                                message.reply("Error: The channel `" + message.getChannelReceiver().getName() + "` isn't in the story channel list!");
                            }
                        } else if (message.getContent().equalsIgnoreCase("$viewStoryList")) {
                            if (!memebot.isOnList(message.getAuthor().getName(), whitelist) && !memebot.isOnList(message.getAuthor().getName(), promotedList)) {
                                message.reply("Error: You don't have the necessary permissions to run this command.");
                                return;
                            }
                            StringBuilder channels = new StringBuilder("");
                            for (String id : storyGameChannelIDs) {
                                channels.append(api1.getChannelById(id).getName() + " | " + id + "\n");
                            }
                            message.reply("Channels that are considered \"story channels:\"\n" +
                                    "```\n" +
                                    channels.toString() +
                                    "```");
                        } else if (message.getContent().startsWith("$finishstory")) {
                            String title = "untitled";
                            String[] parts = message.getContent().split(" ");
                            if (parts.length > 1) {
                                title = message.getContent().replace("$finishstory ", "");
                            }
                            if (memebot.isOnList(message.getChannelReceiver().getId(), storyGameChannelIDs)) {
                                if (storyGameStories.containsKey(message.getChannelReceiver().getId())) {
                                    StringBuilder story = storyGameStories.get(message.getChannelReceiver().getId());
                                    message.reply("Story from `" + message.getChannelReceiver().getName() + "`, completed on " + LocalDate.now() + " at " + LocalTime.now() + ": \n" +
                                            "```\n" +
                                            story.toString() + "\n" +
                                            "```");
                                    try {
                                        File file = new File(title + " " + getTimestampForFileName() + ".txt");
                                        file.createNewFile();

                                        FileWriter fileWriter = new FileWriter(file, true);
                                        fileWriter.append(title + "\n\n\t" + story.toString());
                                        fileWriter.close();

                                        message.getChannelReceiver().sendFile(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    storyGameChannelIDs.remove(message.getChannelReceiver().getId());
                                    storyGameStories.remove(message.getChannelReceiver().getId());
                                } else {
                                    message.reply("Error: The story in channel `" + message.getChannelReceiver().getName() + "` is empty!");
                                }
                            } else {
                                message.reply("Error: The channel `" + message.getChannelReceiver().getName() + "` isn't in the story channel list!");
                            }


                        } else if (message.getContent().equalsIgnoreCase("$deleteWord")) {
                            if (isOnList(message.getChannelReceiver().getId(), storyGameChannelIDs)) {
                                StringBuilder story = storyGameStories.get(message.getChannelReceiver().getId());
                                int indexOfLastSpace = story.lastIndexOf(" ");
                                if (indexOfLastSpace == -1) {
                                    message.reply("There are no words to delete in `" + message.getChannelReceiver().getName() + "`!");
                                } else {
                                    story.delete(indexOfLastSpace, story.length());
                                    message.reply("Deleted word!");
                                }
                            } else {
                                message.reply("`" + message.getChannelReceiver().getName() + "` is not currently a story channel!");
                            }
                        } else if (message.getContent().equalsIgnoreCase("$viewRecentWords")) {
                            try {
                                if (isOnList(message.getChannelReceiver().getId(), storyGameChannelIDs)) {
                                    StringBuilder story = storyGameStories.get(message.getChannelReceiver().getId());
                                    int numOfSpaces = StringUtils.countMatches(story.toString(), " ");

                                    if (numOfSpaces >= 10) {
                                        int indexOfTenthSpace = story.toString().lastIndexOf(" ", 9);
                                        message.reply("```\n" +
                                                ". . ." + story.toString().substring(indexOfTenthSpace, story.length()) + "\n" +
                                                "```");
                                    } else {
                                        message.reply("```\n" +
                                                story.toString() + "\n" +
                                                "```");
                                    }
                                } else {
                                    message.reply("`" + message.getChannelReceiver().getName() + "` is not currently a story channel!");
                                }
                            } catch (NullPointerException e) {
                                message.reply("Error: " + e.getMessage() + "\nIs the story empty?");
                            }
                        } else if (message.getContent().startsWith("$edit")) {
                            if (!isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("Error: command only usable by whitelisted members.");
                                return;
                            }

                            if (message.getContent().equalsIgnoreCase("$edit")) return;
                            String[] parts = message.getContent().replace("$edit ", "").split(" ");
                            if (parts.length < 2) {
                                message.reply("Error: command must contain a valid ID and edit message.");
                                return;
                            }

                            if (!parts[0].matches("[0-9]+")) { //check if the string contains only numbers
                                message.reply("Error: invalid message ID.");
                                return;
                            }

                            try {
                                Message messageToEdit = message.getChannelReceiver().getMessageById(parts[0]).get();
                                messageToEdit.edit(message.getContent().replace("$edit ", "").replace(parts[0], ""));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        } else if (message.getContent().startsWith("$clnew")) {
                            if (message.getContent().equalsIgnoreCase("$clNew")) {
                                message.reply("```\n" +
                                        "Creates new checklist item.\n" +
                                        "Syntax: \"$clnew [string]\"\n" +
                                        "Example: \"$clnew Create new GUI\"" +
                                        "```");
                                return;
                            }

                            String msg = message.getContent().replace("$clnew ", "");
                            message.reply(":x: " + msg);
                            message.delete();
                        } else if (message.getContent().startsWith("$cltoggle")) {
                            if (message.getContent().equalsIgnoreCase("$clToggle")) {
                                message.reply("```\n" +
                                        "Toggles existing checklist item.\n" +
                                        "Syntax: \"$cltoggle [id]\"\n" +
                                        "Example: \"$cltoggle 386297000041119754\"\n" +
                                        "Note: command must be run in the same channel as the checklist item being toggled.\n" +
                                        "```");
                                return;
                            }

                            String id = message.getContent().replace("$cltoggle ", "");
                            if (!id.matches("[0-9]+")) {
                                message.reply("Error: invalid message ID.");
                                return;
                            }
                            try {
                                Message messageToToggle = message.getChannelReceiver().getMessageById(id).get();
                                if (messageToToggle.getContent().startsWith(":x:")) {
                                    messageToToggle.edit(messageToToggle.getContent().replaceFirst(":x:", ":white_check_mark:"));
                                    message.delete();
                                } else if (messageToToggle.getContent().startsWith(":white_check_mark")) {
                                    messageToToggle.edit(messageToToggle.getContent().replaceFirst(":white_check_mark:", ":x:"));
                                    message.delete();
                                } else {
                                    message.reply("Error: invalid checklist item.");
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                        } else if (message.getContent().startsWith("$roll")) {
                            if (message.getContent().equalsIgnoreCase("$roll")) {
                                message.reply("```\n" +
                                        "Rolls a die.\n" +
                                        "Syntax: \"$roll [amount]d[type of die]\"\n" +
                                        "Example: \"$roll 2d10\"\n" +
                                        "```");
                                return;
                            }
                            String dice = message.getContent().replace("$roll ", "");
                            int amount = 1;
                            int numberOfSides = Integer.parseInt(dice.split("d")[1]);
                            boolean buff = false;

                            if (!dice.startsWith("d")) { //allows you to forgo the first number to roll one die
                                amount = Integer.parseInt(dice.split("d")[0]);
                            }

                            if (amount <= 0) {
                                message.reply("Error: make sure you're rolling at least 1 die.");
                                return;
                            }

                            if (numberOfSides <= 0) {
                                message.reply("Error: make sure you're rolling dice with at least 1 side.");
                                return;
                            }


                            if (message.getAuthor().getName().equalsIgnoreCase("Arsenol0105") || message.getAuthor().getName().equalsIgnoreCase("zarmo")) {
                                /*int riggingFactor = rng(10);
                                if (riggingFactor <= 3) { //30%
                                    amount = (int) ((double)amount * 0.8);
                                } else if (riggingFactor <= 6) { //30%
                                    amount = (int) ((double)amount * 0.6);
                                } else {} //don't do anything, 40%*/
                            } else {
                                buff = true;
                            }

                            ArrayList<Integer> numbers = rng(numberOfSides, amount);
                            String output = "`";
                            for (int number : numbers) {
                                if (buff) {
                                    if (number <  ((double)numberOfSides * 0.8)) { //20% ish buff to non dungeon masters
                                        System.out.print(number + " → ");
                                        number = (int)((double)number / 0.8);
                                        System.out.println(number);
                                    }
                                }
                                output += number + " ";
                            }
                            output += "`";
                            message.reply(output);
                        } else if (message.getContent().startsWith("$strawpoll")) {
                            if (message.getContent().equalsIgnoreCase("$strawpoll")) {
                                message.reply("```\n" +
                                        "Starts a strawpoll with the given title and options.\n" +
                                        "Syntax: \"$strawpoll [title] | [option 1] | [option 2] | ... [option x]\"\n" +
                                        "Example: \"$strawpoll Do you like the changes? | Yes, I do | No, I don't | I'm indifferent\"\n" +
                                        "Note: up to 100 polls per hour can be created and with up to 30 options each.\n" +
                                        "```");
                                return;
                            }

                            List<String> parts = Arrays.asList(message.getContent().replace("$strawpoll ", "").split("\\|"));
                            String title = parts.get(0);

                            List<String> options = parts.subList(1, parts.size());

                            StrawPoll poll = new StrawPoll(title, options);
                            poll.create();

                            String id = poll.getId();
                            String url = poll.getPollURL();

                            message.reply(url + "\n" +
                                    "```\n" +
                                    title + "\n\n" +
                                    listAsNumberedVerticalList(options) +
                                    "```");

                            strawpolls.put(title, id);
                        } else if (message.getContent().startsWith("$checkstrawpoll")) {
                            if (message.getContent().equalsIgnoreCase("$checkstrawpoll")) {
                                message.reply("```\n" +
                                        "Checks a strawpoll given the poll's id, or title if the strawpoll is cached in SoupBot.\n" +
                                        "Syntax: \"$checkstrawpoll [id/title]\"\n" +
                                        "Example: \"$checkstrawpoll 69\"\n" +
                                        "Note: Strawpolls are cached in SoupBot if they were created by SoupBot during its current session.\n" +
                                        "```");
                                return;
                            }
                            String id = message.getContent().replace("$checkstrawpoll ", "");
                            System.out.println(id);
                            StrawPoll poll = new StrawPoll();
                            poll = poll.retrieve(Integer.parseInt(id));

                            String title = poll.getTitle();
                            String url = poll.getPollURL();
                            List<String> options = poll.getOptions();
                            List<Integer> votes = poll.getVotes();

                            StringBuilder builder = new StringBuilder();

                            builder.append("ANSWER" + multiplyString(" ", 50 - "ANSWER".length()) + "VOTES\n");

                            for (int i = 0; i < options.size(); i++) {
                                builder.append("\"" + options.get(i) + "\"" +
                                        multiplyString(" ", 48 - options.get(i).length()) + votes.get(i) + "\n");
                            }

                            message.reply(url + "\n" +
                                    "```\n" +
                                    "\"" + title + "\"\n\n" +
                                    builder.toString() + "\n" +
                                    "```");
                        }

                        //ALL COMMANDS GO ABOVE HERE FOR CLARITY PURPOSES

                        //story game handling

                        else if (memebot.isOnList(message.getChannelReceiver().getId(), storyGameChannelIDs)) {
                            if (message.getContent().startsWith("$ignore") || message.getAuthor().isYourself()) {
                                System.out.println("Message \"" + message.getContent() + "\" has been ignored");
                            } else {
                                if (storyGameStories.containsKey(message.getChannelReceiver().getId())) {
                                    StringBuilder story = storyGameStories.get(message.getChannelReceiver().getId());

                                    if (message.getContent().matches("[.!?,]") && !message.getContent().matches("\\w")) {
                                        story.append(getFirstWordInString(message.getContent()));
                                        System.out.println("Punctuation detected");
                                    } else if (message.getContent().equalsIgnoreCase("-")) {
                                        story.append("-");
                                    } else if (message.getContent().equalsIgnoreCase("[n]")) { //newline
                                        story.append("\n");
                                        message.reply("Added newline.");
                                    } else if (message.getContent().equalsIgnoreCase("[t]")) { //tab
                                        story.append("\t");
                                        message.reply("Added indent.");
                                    } else {
                                        if (story.charAt(story.length() - 1) == '-') {
                                            story.append(getFirstWordInString(message.getContent()));
                                        } else {
                                            story.append(" " + getFirstWordInString(message.getContent()));
                                        }
                                    }
                                    storyGameStories.put(message.getChannelReceiver().getId(), story);

                                    //System.out.println(story.toString());
                                } else {
                                    StringBuilder story = new StringBuilder(getFirstWordInString(message.getContent()));
                                    storyGameStories.put(message.getChannelReceiver().getId(), story);
                                    System.out.println("[STORY START] " + story);
                                }
                            }

                        }

                        //NO COMMANDS BELOW HERE FOR CLARITY PURPOSES

                    }); //end of listener
                }

                @Override
                public void onFailure(Throwable t) {
                    failed = true;
                    t.printStackTrace();
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down...");
                final MessageReceiver receiver = api.getChannelById("189359733377990656");

                receiver.sendMessage("Farewell. <:timeforcrab:292796338645630978>");
                api.disconnect();
            }, "Shutdown-thread"));

            /*
            if (failed) {
                int choice = JOptionPane.showConfirmDialog(null, "Reconnect?", "Reconnect?", JOptionPane.YES_NO_OPTION);
                if (choice == 1) {
                    System.out.println("User chose not to reconnect bot");
                    System.exit(0);
                } else {
                    System.out.println("Attempting to reconnect...");
                }
            }
            */

}


    public static boolean isOnList(String item, ArrayList<?> list) {

        for (Object listName : list){
            if (item.equalsIgnoreCase(listName.toString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOnList(Object item, ArrayList<?> list) {

        for (Object listItem : list){
            if (item.equals(listItem)) {
                return true;
            }
        }
        return false;
    }

    public static String getUrl (String fileContent) {
        fileContent = fileContent.trim();
        String[] contentList = fileContent.split("\r\n");
        for (String content : contentList) {
            if (content.toUpperCase().startsWith("URL")) {
                int i = content.indexOf("=");
                return content.substring(i + 1);
            }
        }
        return fileContent; //placeholder
    }
    public static ArrayList<Integer> bubbleSort (ArrayList<Integer> elements) {

        StringBuilder sb1 = new StringBuilder();//
        for (int element : elements) {//
            sb1.append(element + ", ");//
        }//
        System.out.println(sb1.toString());//

        int sorted = 1; //when it equals 0 the list is sorted
        while (sorted != 0) {
            sorted = 0;
            for (int n = 0; n < elements.size() - 1; n++) {
                if (elements.get(n) > elements.get(n + 1)) {
                    int a = elements.get(n);
                    int b = elements.get(n + 1);
                    System.out.println("Swapped " + a + " and " + b);//
                    elements.set(n, b);//
                    elements.set(n + 1, a);//
                    StringBuilder sb2 = new StringBuilder();//
                    for (int element : elements) {//
                        sb2.append(element + ", ");//
                    }//

                    System.out.println(sb2.toString());//
                    sorted++;
                }
            }
        }
        return elements;
    }

    public static double[] quadratic(double a, double b, double c) {
        double root1, root2 = 0;
        root1 = (-b + Math.sqrt(Math.pow(b,2) - (4*a*c)))/(2*a);
        root2 = (-b - Math.sqrt(Math.pow(b,2) - (4*a*c)))/(2*a);

        double[] roots = new double[2];
        roots[0] = root1;
        roots[1] = root2;

        return roots;
    }

    public static ArrayList<Integer> primeFactors(int x) {
        x = Math.abs(x);
        double max = x / 2;
        ArrayList<Integer> factors = new ArrayList<Integer>();
            for (int i = 2; i <= max; i++) {
                if ((x/(double)i) == (int) (x/i)) {
                    factors.add(i);
                    x = x/i;
                    i = 2;
                }
                if (x == 2) {
                    factors.add(2);
                    return bubbleSort(factors);
                }
            }
        return bubbleSort(factors);
    }

    public static ArrayList<Integer> factors(int x) {
        x = Math.abs(x);
        double max = x / 2;
        ArrayList<Integer> factors = new ArrayList<Integer>();
        for (int i = 1; i <= max; i++) {
            if ((x/(double)i) == (int) (x/i)) {
                factors.add(i);
            }
            if (x == 2) {
                factors.add(2);
                return bubbleSort(factors);
            }
        }
        factors.add(x);
        return bubbleSort(factors);
    }

    public static String scramble( Random random, String inputString ) { //stolen from https://stackoverflow.com/questions/20588736/how-can-i-shuffle-the-letters-of-a-word
        // Convert your string into a simple char array:
        char a[] = inputString.toCharArray();

        // Scramble the letters using the standard Fisher-Yates shuffle,
        for( int i=0 ; i<a.length ; i++ )
        {
            int j = random.nextInt(a.length);
            // Swap letters
            char temp = a[i]; a[i] = a[j];  a[j] = temp;
        }
        return new String( a );
    }

    public static boolean isEmptyOrNull(String s) {
        return s.equals("") || s.equals(null);
    }

    public static int[] simplifyFraction(int num, int den) {
        ArrayList<Integer> numFactors = new ArrayList<Integer>();
        ArrayList<Integer> denFactors = new ArrayList<Integer>();
        ArrayList<Boolean> isCommon = new ArrayList<Boolean>();

        while (true) {

            if (num == 1 || den == 1) { return new int[] {num, den}; }

            numFactors = primeFactors(num);
            denFactors = primeFactors(den);

            if (num != 1 && numFactors.size() == 0) { numFactors.add(num); }
            if (den != 1 && denFactors.size() == 0) { denFactors.add(den); }

            isCommon.clear();

            for (int factor : numFactors) {
                isCommon.add(denFactors.contains(factor));
            }

            if (isCommon.contains(true)) {
                int gcfIndex = isCommon.lastIndexOf(true);
                int gcf = numFactors.get(gcfIndex);
                num = num / gcf;
                den = den / gcf;
            } else {
                return new int[] {num, den};
            }
        }
    }

    public static String asciiGet(String text, String font) throws IOException { //stolen from https://stackoverflow.com/questions/1485708/how-do-i-do-a-http-get-in-java
        StringBuilder result = new StringBuilder();
        String path = "http://artii.herokuapp.com/make?text=" + text + "&font=" + font;
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 500) {
            result.append("Something went wrong. The most likely error is in the font name. To see the list of available fonts, go to: http://artii.herokuapp.com/fonts_list");
            return result.toString();
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;

        while ((line = rd.readLine()) != null) {
            result.append(line + "\n");
        }
        rd.close();
        return result.toString();
    }

    public static double factorial(int x) {
        return ArithmeticUtils.factorialDouble(x);
    }

    public static int rng(int max) {
        Random rng = new Random();
        int num = rng.nextInt(max) + 1;

        return num;
    }
    public static ArrayList<Integer> rng(int max, int amount) {
        Random rng = new Random();
        ArrayList<Integer> numbers = new ArrayList<Integer>();

        for (int i = 0; i < amount; i++) {
            int num = rng.nextInt(max) + 1;
            numbers.add(num);
        }
        return numbers;
    }
    public static boolean arrayListContainsIgnoreCase(ArrayList<String> list, String match) {
        for (String element : list) {
            if (element.equalsIgnoreCase(match)) {
                return true;
            }
        }
        return false;
    }

    public static boolean arrayListContainsIgnoreCase(ArrayList<String> list, String[] matches, boolean exactEqual) {
        if (exactEqual) {
            for (String match : matches) {
                for (String element : list) {
                    if (element.equalsIgnoreCase(match)) {
                        //System.out.println(element + " == " + match);
                        return true;
                    } else {
                        //System.out.println(element + " != " + match);
                    }
                }
            }
            return false;
        } else {
            for (String match : matches) { //checks for containing, not actual exact matches
                for (String element : list) {
                    if (element.contains(match)) {
                        //System.out.println(element + " == " + match);
                        return true;
                    } else {
                        //System.out.println(element + " != " + match);
                    }
                }
            }
            return false;
        }
    }

    public static ArrayList<String> getProcessList() {

        ArrayList<String> lines = new ArrayList<String>();
        try {
            String line;

            Process p = Runtime.getRuntime().exec
                    (System.getenv("windir") +"\\system32\\"+"tasklist.exe /fo csv /nh");
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                //System.out.println(line); //<-- Parse data here.
                lines.add(line);
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return lines;
    }

    public void displayTray(String title, String message) throws AWTException, java.net.MalformedURLException { //modified method taken from https://stackoverflow.com/questions/34490218/how-to-make-a-windows-notification-in-java
        //Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getToolkit().createImage(getClass().getResource("icon.png"));
        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        //Lets the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);
        trayIcon.displayMessage(title, message, MessageType.INFO);
    }

    public static ArrayList<String> getArrayListOfLines(String path) throws Exception {
        File file = new File(ClassLoader.getSystemResource(path).getFile());
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
            //System.out.println(line);
        }
        return lines;
    }

    public static String makeAsciiSpermEgg(int sizeOfSpace) {
        String string = "```\n" +
                "           1, - ~ ~ ~ - ,\n" +
                "       1, '               ' ,\n" +
                "     1,    a     b     c      ,\n" +
                "    1,  d     e     f     g    ,\n" +
                "   1,      h     i     j    k   ,\n" +
                "   1,  l      m     n     o     ,\n" +
                "~~•1,     p     q     r    s    ,\n" +
                "    1,  t    u     v     w     ,\n" +
                "     1,    x     y     z      ,\n" +
                "       1,                  , '\n" +
                "         1' - , _ _ _ ,  '\n" +
                "```";
        String returnString = "";
        if (sizeOfSpace == 0) {
             returnString = string.replace("1", "");
        } else {
            String spaces = multiplyString(" ", sizeOfSpace*5); //change the number sizeOfSpace is multiplied by to change the distance between sperm and egg accordingly
            returnString = string.replace("1", spaces);
        }
        for (char c : game1UnusedChars) {
            returnString = returnString.replace(c, ' ');
        }
        return returnString;
    }

    public static String multiplyString(String string, int times) {
        String returnString = "";
        for (int i = 0; i < times; i++) {
            returnString += string;
        }
        return returnString;
    }

    public static String arrayListAsStringForHitlermanGuess(ArrayList<Character> arrayList) {
        String returnString = "";
        for (char c : arrayList) {
            returnString += c + " ";
        }
        return returnString;
    }

    public static String arrayListOfIdsAsStringListOfNames(ArrayList<String> arrayList, DiscordAPI api) {
        String returnString = "";
        int indexOfLastComma = 0;
        int indexOfSecondLastComma = 0;

        for (String string : arrayList) {
            try {
                string = api.getUserById(string).get().getName();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            returnString += string + ", ";
            indexOfSecondLastComma = indexOfLastComma;
            indexOfLastComma = returnString.length() - 2;
        }

        returnString = returnString.substring(0, indexOfLastComma); //cut the string before the comma
        if (arrayList.size() > 1 && arrayList.size() != 2) {
            returnString = returnString.substring(0, indexOfSecondLastComma) + ", and" + returnString.substring(indexOfSecondLastComma + 1);
        }
        if (arrayList.size() == 2) {
            returnString = returnString.substring(0, indexOfSecondLastComma) + " and" + returnString.substring(indexOfSecondLastComma + 1);
        }
        return returnString;
    }

    public static String arrayListAsStringList(ArrayList<String> arrayList) {
        String returnString = "";
        int indexOfLastComma = 0;
        int indexOfSecondLastComma = 0;

        for (String string : arrayList) {
            returnString += string + ", ";
            indexOfSecondLastComma = indexOfLastComma;
            indexOfLastComma = returnString.length() - 2;
        }

        returnString = returnString.substring(0, indexOfLastComma); //cut the string before the comma
        if (arrayList.size() > 1 && arrayList.size() != 2) {
            returnString = returnString.substring(0, indexOfSecondLastComma) + ", and" + returnString.substring(indexOfSecondLastComma + 1);
        }
        if (arrayList.size() == 2) {
            returnString = returnString.substring(0, indexOfSecondLastComma) + " and" + returnString.substring(indexOfSecondLastComma + 1);
        }
        return returnString;
    }

    public static ArrayList<Character> arrayListFromArray(char[] charArray) {
        ArrayList<Character> characters = new ArrayList<Character>();
        for (char c : charArray) {
            characters.add(c);
        }
        return characters;
    }

    public static ArrayList<Integer> indexesOfCharInArrayList(char character, ArrayList<Character> arrayList) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        int i = 0;
        for (char c : arrayList) {
            if (character == c) {
                indexes.add(i);
            }
            i++;
        }
        return indexes;
    }

    public static String listAsVerticalList(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String string : list) {
            builder.append(string + "\n");
        }
        return builder.toString();
    }

    public static String listAsNumberedVerticalList(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append((i + 1) + ". " + list.get(i) + "\n");
        }
        return builder.toString();
    }

    public static String formatListsAsColumns(List<String>[] lists, int columnWidth) {
        List<String> lines = new ArrayList<>(Arrays.asList(listAsVerticalList(lists[0]).split("\n")));

        int longestLength = 0;
        for (List<String> list : lists) { //makes sure the first column is as long as the longest column
            if (list.size() > longestLength) {
                longestLength = list.size();
            }
        }
        while (lines.size() < longestLength) {
            lines.add(multiplyString(" ", columnWidth));
        }

        for (String line : lines) {
            try {
                lines.set(lines.indexOf(line), line + multiplyString(" ", columnWidth - line.length()));
            } catch (IndexOutOfBoundsException e) {
                System.out.println("IndexOutOfBoundsException");
            }
        }
        for (List<String> arrayList : lists) {
            if (!arrayList.equals(lists[0])) { //won't add the first list twice
                for (String line : lines) {
                    try {
                        lines.set(lines.indexOf(line), line + arrayList.get(lines.indexOf(line)) + multiplyString(" ", (columnWidth - arrayList.get(lines.indexOf(line)).length())));
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("IndexOutOfBoundsException");
                        lines.set(lines.indexOf(line), line + multiplyString(" ", columnWidth));
                    }
                }
            }
        }
        return listAsVerticalList(lines);
    }

    public static String formatListAsColumns(ArrayList<String> arrayList, int numOfColumns, int columnWidth) { //TODO this sometimes cuts off a few elements
        int numOfItemsPerColumn = arrayList.size() / numOfColumns;
        List<String> list = arrayList;
        List<String>[] lists = new List[numOfColumns];

        for(int x = 0, fallbackInt = 0; x < numOfColumns;) {
            try { //if exception is thrown, fallbackInt is incremented, causing items to be removed from the final list until it works
                lists[x] = (list.subList(x * numOfItemsPerColumn, ((x + 1) * numOfItemsPerColumn) - fallbackInt));
                x++;
            } catch (IndexOutOfBoundsException e) {
                fallbackInt++;
                System.out.println("IndexOutOfBoundsException detected, incrementing fallbackInt to " + fallbackInt);
            }
        }

        return formatListsAsColumns(lists, columnWidth);
    }

    public static String formatPetStats(DiscordAPI api, Pet pet) {
        int exp = pet.getExp();

        String returnString = "```\n" +
            "Name: " + pet.getName() + "\n" +
            "Level: " + pet.getLevel() + "\n" +
            "EXP: " + exp + " / " + "TODO" + "\n" +
            "\n" +
            "Species: " + pet.getSpecies().name() + "\n" +
            "Breed: " + pet.getBreed().name() + "\n" +
            "STR: " + pet.getStrength() + "\n" +
            "VIT: " + pet.getVitality() + "\n" +
            "CHR: " + pet.getCharisma() + "\n" +
            "AGI: " + pet.getAgility() + "\n" +
            "INT: " + pet.getIntelligence() + "\n" +
            "```";

        return returnString;
    }

    public static String formatStats(DiscordAPI api, UserStats userStats) {
        String id = userStats.getId();
        int exp = userStats.getExp();
        double hitlermanWinLossRatio = 0;

        try {
            hitlermanWinLossRatio = (userStats.getWonGames1() + userStats.getWonTeamGames1() /
                    (userStats.getPlayedGames1() - (userStats.getWonGames1() + userStats.getWonTeamGames1())));
        } catch (ArithmeticException e) {
            hitlermanWinLossRatio = 0;
        }

        String returnString = "";

        try {
            returnString = "```\n" +
                    "Name: " + api.getUserById(id).get().getName() + "\n" +
                    "ID: " + id + "\n" +
                    "Level: " + userStats.getLevel() + "\n" +
                    "EXP: " + exp + " / " + userStats.xpNeededForLevelUp() + "\n" +
                    "Potentiorbs: " + userStats.getPotentiorbs() + "\n" +
                    "\n" +
                    "Hitlerman games played: " + userStats.getPlayedGames1() + "\n" +
                    "Hitlerman games won: " + userStats.getWonGames1() + "\n" +
                    "Hitlerman team games won: " + userStats.getWonTeamGames1() + "\n" +
                    "Hitlerman W/L ratio: " + hitlermanWinLossRatio + "\n" +
                    "```";
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return returnString;
    }

    public static int hitlermanVictory(ArrayList<String> players, int turns, int lives, int maxLives, String word, DiscordAPI api, Channel channel) {
        double xp = 0;
        double xpBase = 1000;
        double xpDistribution = 1;

        if (maxLives - lives == 0) {
            xp = xpBase / (0.9) / ((double) turns / 2) * ((double) word.length() / 7);
        } else {
            xp = xpBase / (maxLives - lives) / ((double) turns / 2) * ((double) word.length() / 7);
        }

        if (players.size() > 1) {
            xpDistribution = players.size() * 0.625; //makes distribution factor less heavy
        }

        xp = xp / xpDistribution;

        if (xp > 150) { //better curving mechanism
            System.out.print(xp);
            xp = (10 * (Math.sqrt(xp - 150)) + 150);
            System.out.println(" → " + xp);
        }

        int intXp = (int)Math.round(xp);

        for (String id : players) {
            if (statsFileExists(id)) {
                try {
                    UserStats userStats = loadStats(id);
                    userStats.addExp(intXp); //gives xp to players involved

                    while (userStats.canLevelUp()) {
                        userStats.levelUpIfPossible();
                        levelUpDialog(channel, api.getUserById(id).get(), userStats);
                    }

                    if (players.size() > 1) {
                        userStats.addWonTeamGames1(1);
                    } else {
                        userStats.addWonGames1(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                UserStats userStats = new UserStats(id);
                userStats.addExp(intXp);

                if (userStats.levelUpIfPossible()) {
                    try {
                        levelUpDialog(channel, api.getUserById(id).get(), userStats);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                if (players.size() > 1) {
                    userStats.addWonTeamGames1(1);
                } else {
                    userStats.addWonGames1(1);
                }
            }
        }

        return intXp; //return xp value for processing and stuff
    }

    public static int guessNumberVictory(String id, int turns, DiscordAPI api, Channel channel) {
        double xpBase = 10;
        double randomFactor = 0.95 + ((new Random().nextInt(699) + 1) / 100);
        double xp = (xpBase - turns) * randomFactor;

        int intXp = (int) Math.round(xp);

        UserStats userStats = null;
        try {
            userStats = loadStats(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        userStats.addExp(intXp);

        while (userStats.canLevelUp()) {
            userStats.levelUpIfPossible();
            try {
                levelUpDialog(channel, api.getUserById(id).get(), userStats);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return intXp;
    }

    public static Object getRandomObjectFromArrayList(ArrayList<?> arrayList) {
        Random rand = new Random();
        int index = rand.nextInt(arrayList.size());
        return arrayList.get(index);
    }

    public static BufferedImage addLogoToImage(BufferedImage picture, BufferedImage logo) {
        Graphics g = picture.getGraphics();
        g.drawImage(logo, 0, picture.getHeight() - logo.getHeight(), null);
        JPanel imagePanel = new JPanel();
        imagePanel.printAll(g);

        return picture;
    }

    public static boolean stringEndsWithFromArray(String string, String[] strings) {
        for (String s : strings) {
            if (string.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public static BufferedImage resizeBufferedImage(BufferedImage image, double scaleFactor) {
        int scaledWidth = (int) Math.round(image.getWidth() * scaleFactor);
        int scaledHeight = (int) Math.round(image.getHeight() * scaleFactor);
        BufferedImage resizedImage = new BufferedImage(scaledWidth, scaledHeight, image.getType());
        Graphics2D resized = resizedImage.createGraphics();
        resized.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        return resizedImage;
    }

    public static String[] parseClarifaiConcept(String line) {
        line = line.substring(24, line.length() - 2);
        String[] parts = line.split(", ");
        String name = parts[0].substring(parts[0].indexOf("=") + 1);
        String percentage = parts[3].substring(parts[0].indexOf("=") + 2);
        String[] returnArray = new String[] {name, formatDecimalAsPercent(percentage, 4)};
        return returnArray;
    }

    public static ArrayList<String> truncateArrayList(ArrayList<String> arrayList, int desiredNumOfElements) {
        while (arrayList.size() > desiredNumOfElements) {
            arrayList.remove(arrayList.size() - 1);
        }
        return arrayList;
    }

    public static String formatDecimalAsPercent(String decimal, int decimalPlaces) {
        /*int indexOfDecimal = decimal.indexOf(".");
        ArrayList<Character> characters = new ArrayList<Character>();

        for (char c : decimal.toCharArray()) {
            characters.add(c);
        }

        characters.remove(indexOfDecimal);
        characters.add(indexOfDecimal + 2, '.');
        */

        double d = Double.parseDouble(decimal);
        int multiplier = Integer.parseInt("1" + multiplyString("0", decimalPlaces));
        d *= multiplier * 100;

        d = Math.round(d);
        d /= multiplier;

        return String.valueOf(d) + "%";
    }

    public static ArrayList<ArrayList<String>> getListsOfOnlineUsers(DiscordAPI api, Channel channel) {
        ArrayList<User> users = new ArrayList<User>(api.getUsers());
        ArrayList<ArrayList<String>> arrayLists = new ArrayList<ArrayList<String>>();

        ArrayList<String> online = new ArrayList<String>();
        ArrayList<String> idle = new ArrayList<String>();
        ArrayList<String> dnd = new ArrayList<String>(); //do not disturb
        ArrayList<String> bots = new ArrayList<String>();

        for (User user : users) {
            if (channel.getServer().isMember(user)) {

                switch (user.getStatus()) {
                    case ONLINE:
                        if (user.isBot()) bots.add(user.getName()); else online.add(user.getName());
                        break;
                    case IDLE:
                        if (user.isBot()) bots.add(user.getName()); else idle.add(user.getName());
                        break;
                    case DO_NOT_DISTURB:
                        if (user.isBot()) bots.add(user.getName()); else dnd.add(user.getName());
                        break;
                    default:
                        break;
                }
            }
        }

        Collections.sort(online, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(idle, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(dnd, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(bots, String.CASE_INSENSITIVE_ORDER);

        arrayLists.add(online);
        arrayLists.add(idle);
        arrayLists.add(dnd);
        arrayLists.add(bots);

        return arrayLists;
    }

    private static String toBinary(BigInteger x) {
        boolean converted = false;
        StringBuilder builder = new StringBuilder();

        BigInteger[] nums = new BigInteger[2];

        while (converted == false) {
            nums = x.divideAndRemainder(new BigInteger("2"));
            if (x.equals(new BigInteger("0"))) {
                converted = true;
            }
            x = nums[0];
            builder.append(nums[1]);
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.reverse();
        return builder.toString();
    }

    public static String asciiToBinary(String string) {
        char[] chars = string.toCharArray();
        StringBuilder builder = new StringBuilder();

        for (char c : chars) {
            builder.append(toBinary(BigInteger.valueOf((long) c))); //if this doesn't work, cast c to int first
            builder.append(" ");
        }
        return builder.toString();
    }

    public static BigInteger binaryToDecimal(String binary) {
        binary = new StringBuilder(binary).reverse().toString(); //reverses binary string
        BigInteger decimalNumber =  BigInteger.valueOf(0);
        BigInteger valueOfBit = BigInteger.valueOf(1);

        for (char c : binary.toCharArray()) {
            if (c == '1') {
                decimalNumber = decimalNumber.add(valueOfBit);
            }
            valueOfBit = valueOfBit.multiply(BigInteger.valueOf(2));
        }
        return decimalNumber;
    }

    public static char binaryToChar(String binary) {
        binary = new StringBuilder(binary).reverse().toString(); //reverses binary string
        int decimalNumber = 0;
        int valueOfBit = 1;

        for (char c : binary.toCharArray()) {
            if (c == '1') {
                decimalNumber += valueOfBit;
            }
            valueOfBit *= 2;
        }
        return (char) decimalNumber;
    }

    public static String binaryToAscii(String binary) {
        String[] binaryStrings = binary.split(" ");
        String returnString = "";
        for (String binaryString : binaryStrings) {
            returnString += binaryToChar(binaryString);
        }
        return returnString;
    }

    public static String getFirstWordInString(String string) {
        if (string.split(" ").length != 0) {
            return string.split(" ")[0];
        } else {
            return string;
        }
    }

    public static boolean indexOfArrayListExists(ArrayList<?> arrayList, int index) {
        try {
            arrayList.get(index);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    }

class TimedEventHandlerRunnable implements Runnable {

    DiscordAPI api;
    Channel channel;
    private Thread thread;

    public TimedEventHandlerRunnable(DiscordAPI api, Channel channel) {
        System.out.println(getTimestampFull() + " Creating " + this.getClass().getName() + " thread");

        this.api = api;
        this.channel = channel;
    }

    @Override
    public void run() {
        //memebot.checkOnline.start();
        try {

            while (true) {
                if (memebot.checkOnline.getTimedBool()) {
                    ArrayList<ArrayList<String>> arrayLists = memebot.getListsOfOnlineUsers(api, channel);

                    ArrayList<String> online = arrayLists.get(0);
                    ArrayList<String> idle = arrayLists.get(1);
                    ArrayList<String> dnd = arrayLists.get(2);

                    File onlineUserNum = new File(getPathOfResourcesFolder() + "/stats/online.txt");
                    File idleUserNum = new File(getPathOfResourcesFolder() + "/stats/idle.txt");
                    File dndUserNum = new File(getPathOfResourcesFolder() + "/stats/dnd.txt");

                    System.out.println(getTimestampFull() + " Writing user counts to file");
                    onlineUserNum.createNewFile();
                    idleUserNum.createNewFile();
                    dndUserNum.createNewFile();

                    FileWriter onlineWriter = new FileWriter(onlineUserNum, true);
                    onlineWriter.append(getTimestampTime() + " | " + online.size() + "\n");
                    onlineWriter.close();

                    FileWriter idleWriter = new FileWriter(idleUserNum, true);
                    idleWriter.append(getTimestampTime() + " | " + idle.size() + "\n");
                    idleWriter.close();

                    FileWriter dndWriter = new FileWriter(dndUserNum, true);
                    dndWriter.append(getTimestampTime() + " | " + dnd.size() + "\n");
                    dndWriter.close();

                    memebot.checkOnline.setTimedBool(false);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    public void start () {
        System.out.println(getTimestampFull() + " Starting " +  this.getClass().getName() + "!");
        if (thread == null) {
            thread = new Thread (this, "TimedEventsHandlerRunnable");
            thread.start();
        }
    }
}

class ReactionAddingWithSimulatedRateLimitRunnable implements Runnable {

    Message message;
    String[] reactions;
    private Thread thread;

    public ReactionAddingWithSimulatedRateLimitRunnable (Message message, String[] reactions) {
        this.message = message;
        this.reactions = reactions; //TODO make custom emojis work
        System.out.println(getTimestampFull() + " Creating " + this.getClass().getName() + " thread");
    }

    @Override
    public void run() {
        try {
            for (String reaction : reactions) {
                Future<Void> future = message.addUnicodeReaction(reaction);
                //Future<Void> futureCustom = message.addCustomEmojiReaction();
                System.out.println("Adding reaction " + reaction);
                while (!future.isDone()) {}
                Thread.sleep(250);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println(getTimestampFull() + " Starting " +  this.getClass().getName() + "!");
        if (thread == null) {
            thread = new Thread (this, "TimedEventsHandlerRunnable");
            thread.start();
        }
    }
}