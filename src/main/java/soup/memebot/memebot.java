package soup.memebot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.UserStatus;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageHistory;
import de.btobastian.javacord.entities.message.MessageReceiver;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import de.btobastian.javacord.*;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.Random;
import java.awt.TrayIcon.MessageType;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;

import de.btobastian.javacord.listener.user.UserChangeStatusListener;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.math.util.MathUtils;

public class memebot {

    static final ArrayList<String> whitelist = new ArrayList<String>(Arrays.asList("TornadoOfSoup", "Kotamonn", "SoupBot"));
    static final ArrayList<String> unitTypeList = new ArrayList<String>(Arrays.asList("temp", "weight", "length", "angle"));
    static final ArrayList<String> unitTempList = new ArrayList<String>(Arrays.asList("C", "F", "K"));
    static final ArrayList<String> unitAngleList = new ArrayList<String>(Arrays.asList("D", "R", "deg", "rad"));

    static boolean[] games = new boolean[] {false, false}; //0 = guessnumber, 1 = hangman

    static int game0Num = 0;
    static String gameUserID = "";

    static String game1Word = "";
    static int game1lives = 0;
    static ArrayList<Character> game1GuessedWord = new ArrayList<Character>();
    static ArrayList<Character> game1ActualWord = new ArrayList<Character>();
    static ArrayList<String> game1HelpfulUsers = new ArrayList<String>();

    public static void main(String[] args) {
        String token = "";
        String password = "";

        try {
            ArrayList<String> admin = getArrayListOfLines("admin.pass");
            token = admin.get(0);
            password = admin.get(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("admin.pass not found, an admin.pass file should have two lines. " +
                    "The first should contain a token, and the second should contain the bot password. It should be located in the resources folder.");
            System.exit(0);
        }

        DiscordAPI api = Javacord.getApi(token, true);
        final int numOfCommands = 29;
        final int numOfSubCommands = 17;
        final String version = "1.2.9.10";
        final String complieDate = "7/31/17 23:13 EST";
        final String chatFilterVersion = "1.5";
        final boolean[] censor = {false};
        final long[] cooldown = {0};
        final boolean[] modes = {false, false}; //0 = fastPictures, 1 = restricted


        while(true) {
            String pass = JOptionPane.showInputDialog("Password:"); //checks for a password
            if (pass.equals(password)) {
                break;
            }
        }

        System.out.println("Logging in...");

        api.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(final DiscordAPI api) {
                System.out.println("Successful login");
                final MessageReceiver receiver = api.getChannelById("189359733377990656"); //general in ddc
                receiver.sendMessage("Hello everyone! SoupBot v" + version + " here!");   //that thing travis and nick dislike
                api.setAutoReconnect(true);

                Collection users = api.getUsers();
                System.out.print(users.size() + " users: ");
                for (Object username : users) {
                    System.out.print(username.toString());
                }
                System.out.println();

                api.setGame("$help");

                api.registerListener(new UserChangeStatusListener() { //user status changes
                    @Override
                    public void onUserChangeStatus(DiscordAPI discordAPI, User user, UserStatus oldStatus) {
                        System.out.println(new Timestamp(System.currentTimeMillis()) + ": " + user.getName() + "'s status has changed from " + oldStatus.toString() + " to " + user.getStatus().toString());

                        ArrayList<String> notificationPeople = new ArrayList<String>(Arrays.asList("Silver", "Almurray155", "Meme", "Rickl", "Cyrinthia"));
                        ArrayList<String> runningProcesses = getProcessList();
                        String[] uninterruptibleProcesses = new String[] {"MinecraftLauncher.exe", "Terraria.exe"}; //add more to here

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
                    }
                });

                api.registerListener(new MessageCreateListener() {
                    @Override
                    public void onMessageCreate(DiscordAPI api, Message message) {
                        // check the content of the message

                        // censorship below

                        String editedMessage = message.getContent();
                        if (censor[0] && (message.getContent().contains("fuck") || message.getContent().contains("shit") || message.getContent().contains("bastard") || message.getContent().contains("dick") || message.getContent().contains("pussy") || message.getContent().contains("cunt") || message.getContent().contains("bitch") || message.getContent().contains("cock")
                                || message.getContent().contains("Fuck") || message.getContent().contains("Shit") || message.getContent().contains("Dick") || message.getContent().contains("Pussy") || message.getContent().contains("Cunt") || message.getContent().contains("Bitch") || message.getContent().contains("nigger") || message.getContent().contains("or does it"))) {
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

                            message.delete();
                            message.reply(String.valueOf("**" + message.getAuthor()) + "**: \n" + editedMessage);
                            System.out.println(editedMessage);
                        }
                        // end of censorship

                        if (modes[1]) { //restricted mode
                            if ((isOnList(message.getAuthor().getName(), whitelist) == true) && message.getContent().startsWith("$")) {

                            } else {
                                if (message.getContent().startsWith("$")) {
                                    message.reply("Restricted mode is active and you, **" + message.getAuthor().getName() + "**, are not on the whitelist.");
                                    return;
                                }
                            }
                        }

                        if (games[0]) { //guessnumber game

                            if (message.getAuthor().getId().equals(gameUserID)) {
                                if (message.getContent().startsWith("$guess ")) {
                                    String msg = message.getContent().replace("$guess ", "");
                                    int guess = Integer.parseInt(msg);
                                    if (guess < game0Num) message.reply("Higher.");
                                    else if (guess > game0Num) message.reply("Lower.");
                                    else if (guess == game0Num) {
                                        message.reply("That's it!");
                                        message.reply(message.getAuthor().getName() + " is victorious!");
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
                                    ArrayList<Integer> indexes = indexesOfCharInArrayList(c, game1ActualWord);
                                    if (indexes.size() > 0) {
                                        for (int i : indexes) {
                                            game1GuessedWord.set(i, game1ActualWord.get(i));
                                        }
                                        if (indexes.size() == 1) {
                                            message.reply("There is 1 " + c + " in the word.");
                                            if (!arrayListContainsIgnoreCase(game1HelpfulUsers, message.getAuthor().getName())) {
                                                game1HelpfulUsers.add(message.getAuthor().getName());
                                            }
                                        } else {
                                            message.reply("There are " + indexes.size() + " " + c + "'s in the word.");
                                            if (!arrayListContainsIgnoreCase(game1HelpfulUsers, message.getAuthor().getName())) {
                                                game1HelpfulUsers.add(message.getAuthor().getName());
                                            }
                                        }
                                    } else {
                                        game1lives--;
                                        message.reply("There are no " + c + "'s in the word.");
                                    }
                                    if (indexesOfCharInArrayList('_', game1GuessedWord).size() == 0) {
                                        message.reply("Congratulations to " + arrayListAsStringList(game1HelpfulUsers) + "! You've stopped Hitler from being born and saved millions of lives!\n" +
                                                "The word was `" + game1Word + "`. You were victorious with " + game1lives + " lives remaining.\n");
                                        games[1] = false;
                                    } else {

                                        if (game1lives == 0) {
                                            if (game1HelpfulUsers.size() > 1){
                                                message.reply("Oh no! " + arrayListAsStringList(game1HelpfulUsers) + " have failed and millions of lives have been doomed to fall to Hitler.");
                                            } else {
                                                message.reply("Oh no! " + arrayListAsStringList(game1HelpfulUsers) + " has failed and millions of lives have been doomed to fall to Hitler.");
                                            }
                                            message.reply("the word was `" + game1Word + "`");
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
                                    message.reply("Error: Make sure you guess exactly one character at a time.");
                                    return;
                                }
                            } else if (message.getContent().startsWith("$guessword")) {
                                String word = message.getContent().replace("$guessword ", "");
                                if (message.getContent().equalsIgnoreCase("$guessword")){ //user didn't give a word
                                    message.reply("Use \"$guessword [word]\" to guess a word.");
                                } else if (word.equalsIgnoreCase(game1Word)) {
                                    if (!arrayListContainsIgnoreCase(game1HelpfulUsers, message.getAuthor().getName())) {
                                        game1HelpfulUsers.add(message.getAuthor().getName());
                                    }
                                    message.reply("Congratulations to " + arrayListAsStringList(game1HelpfulUsers) + "! You've stopped Hitler from being born and saved millions of lives!\n" +
                                            "The word was `" + game1Word + "`. You were victorious with " + game1lives + " lives remaining.\n");
                                    games[1] = false;
                                } else {
                                    game1lives--;
                                    if (game1lives > 0) {
                                        message.reply("That's not it! Try again.\n" +
                                                game1lives + " lives remaining.");
                                        message.reply(makeAsciiSpermEgg(game1lives) + "\n" +
                                                "```\n" +
                                                arrayListAsStringForHitlermanGuess(game1GuessedWord) + "\n" +
                                                "```");
                                    } else {
                                        if (game1HelpfulUsers.size() > 1){
                                            message.reply("Oh no! " + arrayListAsStringList(game1HelpfulUsers) + " have failed and millions of lives have been doomed to fall to Hitler.");
                                        } else {
                                            message.reply("Oh no! " + arrayListAsStringList(game1HelpfulUsers) + " has failed and millions of lives have been doomed to fall to Hitler.");
                                        }
                                        message.reply("the word was `" + game1Word + "`");
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
                                if (isOnList(message.getAuthor().getName(), whitelist)) {
                                    game1lives++;
                                    message.reply("Number of lives increased to " + game1lives + ".");
                                } else {
                                    message.reply("Error: You are not a whitelisted user.");
                                }
                            }
                        }

                        //end of games

                        if (message.getContent().equalsIgnoreCase("$help")) {
                            // reply to the message
                            message.reply("Hello! I am primarily a meme bot, but I might do other things too. \n" +
                                    "Currently, I have **" + numOfCommands + "** command(s), **" + numOfSubCommands + "** subcommand(s), and **1** hidden command.\n" +
                                    "Commands only usable by whitelisted members are marked with an ^.\n" +
                                    "Commands partially usable by non-whitelisted members are marked with a ^^.\n" +
                                    "The command list is as follows:\n" +
                                    "```\n" +
                                    "$help\n" +
                                    "$memelist\n" +
                                    "$meme\n" +
                                    "$info\n" +
                                    "$shutdown^\n" +
                                    "$getAvatar\n" +
                                    "$silverTime\n" +
                                    "$vote\n" +
                                    "$upcoming\n" +
                                    "$hypixel\n" +
                                    "$censor^\n" +
                                    "$mode^\n" +
                                    "$whitelist^^\n" +
                                    "$prune^\n" +
                                    "$convert\n" +
                                    "$choose\n" +
                                    "$bubblesort\n" +
                                    "$math\n" +
                                    "$mute^\n" +
                                    "$unmute^\n" +
                                    "$supermute^\n" +
                                    "$quadratic\n" +
                                    "$primeFactors\n" +
                                    "$string\n" +
                                    "$simplify\n" +
                                    "$ascii\n" +
                                    "$rng\n" +
                                    "$factors\n" +
                                    "$leetspeak\n" +
                                    "$game\n" +
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
                            ArrayList<String> arrayListOfFiles = new ArrayList<String>();
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
                                ArrayList<String> arrayListOfFiles = new ArrayList<String>();

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
                            if (isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("Farewell. <:timeforcrab:292796338645630978>");
                                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); } //a try catch in one line because it should be more compact
                                api.disconnect();
                                System.exit(0);
                            } else {
                                message.reply("Only whitelisted members have the ability to shut me down.");
                            }
                        } else if (message.getContent().startsWith("$getAvatar")) {
                                    if (message.getContent().equalsIgnoreCase("$getAvatar")) {
                                        message.reply("```\n" +
                                                "Returns the profile picture of the given user.\n" +
                                                "Syntax: \"$getAvatar [user]\"\n" +
                                                "Example: \"$getAvatar @iamadog$1234\"\n" +
                                                "```");
                                    } else {
                                        List<User> mentions = message.getMentions();
                                        if (mentions.size() != 1) {
                                            message.reply("Error: The given command contains **" + mentions.size() + "** mentions instead of the necessary 1.\n" +
                                                    "Syntax: \"$getAvatar [user]\"\n" +
                                                    "Example: \"$getAvatar @iamadog#1234\"");
                                        } else if (mentions.size() == 1) {
                                            message.reply("Profile picture of **" + mentions.get(0) + "**: " + mentions.get(0).getAvatarUrl().toString());
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
                                        "Syntax: \"$vote [question]\"\n" +
                                        "React with a :thumbsup: for yes or a :thumbsdown: for no.\n" +
                                        "```");
                            } else if (message.getContent().equalsIgnoreCase("$vote yes") || message.getContent().equalsIgnoreCase("$vote no") ||message.getContent().equalsIgnoreCase("$vote y") || message.getContent().equalsIgnoreCase("$vote n")) {
                                message.reply("```\n" +
                                        "The given command is only meant to be used during an active vote session.\n" +
                                        "Please start a vote to use this command.\n" +
                                        "```");
                            } else {
                                String[] parts = message.getContent().split(" ");
                                if (parts.length < 2) {
                                    message.reply("Error: The given command contains **" + parts.length + "** parts instead of the necessary 2 or more.");
                                } else {

                                    message.reply("```\n" +
                                            "This command is currently under maintenance because Shoji has no idea what the hell to do.\n" +
                                            "We are sorry for the inconvenience.\n" +
                                            "```");
                                    /*
                                    try {
                                        String question = message.getContent().substring(message.getContent().indexOf(" ") + 1);

                                        Future<Message> vote = message.reply("/tts```\n" +
                                                "Beginning vote on question: \"" + question + "\"\n" +
                                                "The vote will last for 45 seconds.\n" +
                                                "React with :thumbsup: or :thumbsdown: to vote!\n" +
                                                "```");
                                        Message voteMessage = vote.get();
                                        Thread.sleep(15000);
                                        message.reply("/tts```\n" +
                                                "30 seconds left!\n" +
                                                "```");
                                        //Thread.sleep(20000);
                                        message.reply("/tts```\n" +
                                                "10 seconds left!\n" +
                                                "```");
                                        Thread.sleep(10000);

                                        System.out.println(voteMessage.getReactions() + String.valueOf(voteMessage.getReactions().size()));
                                        message.reply(voteMessage.getReactions() + String.valueOf(voteMessage.getReactions().size()));




                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (java.util.concurrent.ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                    */
                                }
                            }
                        } else if (message.getContent().equalsIgnoreCase("$upcoming")) {
                            message.reply("```\n" +
                                    "Here's a non-exhaustive list of what is currently planned for updates in the near future.\n" +
                                    "   A way to add memes to the library and maybe rename them from the discord.\n" +
                                    "   Implement coterminal and other math things.\n" +
                                    "   Maybe find a way to make $vote work?\n" +
                                    "   Make the $mode command work more.\n" +
                                    "   Add levels of restriction to restricted mode.\n" +
                                    "   Implement picture reformatter.\n" +
                                    "   ```");
                        } else if (message.getContent().equalsIgnoreCase("$hypixel")) {

                            System.out.println("hypixel: " + message.getAuthor().getName() + " | " + message.getAuthor().getId());
                            if (message.getAuthor().getId().equals("193043685053562881")) { //if the message author is nick | 190519404780322818
                                System.out.println("it's a nick");
                                if (System.currentTimeMillis() < (cooldown[0] + 3600000) && System.currentTimeMillis() != 0) { //double the cooldown
                                    message.reply("Because you're Nick, this command is on cooldown for another " + Math.abs(((System.currentTimeMillis() - cooldown[0]) / 1000) - 3600) + " seconds.");
                                    return;
                                }
                            }

                            if (System.currentTimeMillis() > (cooldown[0] + 600000)) {

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
                        } else if (message.getContent().equalsIgnoreCase("$censor")) {
                            if (message.getAuthor().getName().equalsIgnoreCase("TornadoOfSoup")) {
                                censor[0] = !censor[0];
                                message.reply("Censorship mode toggled to *" + censor[0] + "*");
                            } else {
                                message.reply("Only TornadoOfSoup has the ability to toggle censorship. You, **" + message.getAuthor().getName() + "**, do not possess that ability.");
                            }
                        } else if (message.getContent().startsWith("$mode")) {
                            if (!isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("Only whitelisted members can use the $mode command.");
                                return;
                            }
                            if (message.getContent().equalsIgnoreCase("$mode")){
                                message.reply("```\n" +
                                        "Toggles certain modes on/off." +
                                        "   \"$mode fastPictures\" -- replaces sending pictures with sending links, resulting in overall speed improvements\n" +
                                        "   \"$mode restricted\" -- toggles \"restricted mode\" on, allowing only certain roles to send commands\n" +
                                        "```");
                            } else if (message.getContent().equalsIgnoreCase("$mode fastPictures")){
                                modes[0] = !modes[0];
                                message.reply("\"fastPictures\" mode toggled to **" + modes[0] + "**.");
                            } else if (message.getContent().equalsIgnoreCase("$mode restricted")){
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
                            if (!isOnList(message.getAuthor().getName(), whitelist)) {
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
                                        int numOfMessages = Integer.parseInt(parts[1]);
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
                                        degrees = Math.round(degrees * 10000)/10000; //rounds to 4 decimal places
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
                            if (isOnList(message.getAuthor().getName(), whitelist)) {
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
                            ArrayList<String> choices = new ArrayList<String>();
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
                            ArrayList<Integer> elements = new ArrayList<Integer>();
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
                            sb.deleteCharAt(sb.length()-1); //removes the final comma and space at the end of the string
                            sb.deleteCharAt(sb.length()-1);

                            double mean = 0;
                            for (int x : sortedList) {
                                mean += x;
                            }
                            mean = mean / sortedList.size();

                            double median = 0;
                            if (sortedList.size()%2 == 0) {
                                double m1 = (sortedList.size()/2)+0.5;
                                double m2 = (sortedList.size()/2)-0.5;

                                median = ((double)sortedList.get((int)m1) + (double)sortedList.get((int)m2)) / 2;
                            } else {
                                median = sortedList.get(sortedList.size()/2);
                            }

                            double range = sortedList.get(sortedList.size()-1) - sortedList.get(0);

                            if (sb.toString().length() > 1650) {
                                message.reply("The given list is too long, so sending it here is likely to break Discord's character limit. If it doesn't send below, the final message was over 2000 characters.");
                            }

                            message.reply( "The given list has **" + sortedList.size() + "** sortable elements. It is sorted below:\n" +
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

                            } else if(message.getContent().startsWith("$mute")) {
                                if (message.getContent().equalsIgnoreCase("$mute")) {
                                    message.reply("```\n" +
                                            "Mutes a user. The user must be mentioned." +
                                            "Syntax: \"$mute [name]\"\n" +
                                            "Example: \"$mute @rem\"\n" +
                                            "```");
                                    return;
                                }
                            if (!isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("This command is only usable by whitelisted members.");
                                return;
                            }
                            boolean roleExists = false;
                            Collection<Role> roles = message.getChannelReceiver().getServer().getRoles();
                            for (Role role : roles) {
                                if(role.getName().equalsIgnoreCase("SoupMuted")) {
                                    Role mutedRole = role;
                                    List<User> users = message.getMentions();
                                    if (users.size() == 0) {
                                        message.reply("No mentions found.");
                                    }
                                    for (User user : users) {
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
                                List<User> users = message.getMentions();
                                if (users.size() == 0) {
                                    message.reply("No mentions found.");
                                }
                                for (User user : users) {
                                    mutedRole.addUser(user);
                                    message.reply("Muted " + user.getName() + ".");
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                        } else if(message.getContent().startsWith("$supermute")) {
                        if (message.getContent().equalsIgnoreCase("$supermute")) {
                            message.reply("```\n" +
                                    "Super mutes a user, preventing them from speaking in channels that don't silence muted people. The user must be mentioned." +
                                    "Syntax: \"$supermute [name]\"\n" +
                                    "Example: \"$supermute @rem\"\n" +
                                    "```");
                            return;
                        }
                        if (!isOnList(message.getAuthor().getName(), whitelist)) {
                            message.reply("This command is only usable by whitelisted members.");
                            return;
                        }
                        boolean roleExists = false;
                        Collection<Role> roles = message.getChannelReceiver().getServer().getRoles();
                        for (Role role : roles) {
                            if(role.getName().equalsIgnoreCase("SoupSuperMuted")) {
                                Role mutedRole = role;
                                List<User> users = message.getMentions();
                                if (users.size() == 0) {
                                    message.reply("No mentions found.");
                                }
                                for (User user : users) {
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
                            List<User> users = message.getMentions();
                            if (users.size() == 0) {
                                message.reply("No mentions found.");
                            }
                            for (User user : users) {
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
                            if (!isOnList(message.getAuthor().getName(), whitelist)) {
                                message.reply("This command is only usable by whitelisted members.");
                                return;
                            }
                            if (message.getMentions().size() == 0) {
                                message.reply("No mentions found.");
                            }
                            Collection<Role> roles = message.getChannelReceiver().getServer().getRoles();
                            for (Role role : roles) {
                                if(role.getName().equalsIgnoreCase("SoupMuted")) {
                                    Role mutedRole = role;
                                    for (User user : message.getMentions()) {
                                        mutedRole.removeUser(user);
                                        message.reply("Unmuted " + user.getName() + ".");
                                    }
                                    return;
                                } else if(role.getName().equalsIgnoreCase("SoupSuperMuted")) {
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
                                    reversedString += msg.charAt(msg.length()-1-i);
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
                                        "Example: \"$ascii hello gothic\"\n" +
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

                            message.reply(msg + "! = *" + String.format("%.0f",result) + "*");
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
                                sb.delete(sb.length()-2, sb.length()); //deletes last comma and space
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
                                    gameUserID = message.getAuthor().getId();
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
                                        game1lives = 8;
                                        System.out.println(game1Word);
                                        game1GuessedWord.clear();
                                        game1ActualWord.clear();
                                        game1HelpfulUsers.clear();
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

                        }

                    }
                }); //end of listener


            }

            @Override
            public void onFailure(Throwable t) {
                // login failed
                t.printStackTrace();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("Shutting down...");
                DiscordAPI api = Javacord.getApi("MzExMDI0NDI1NzkxMjU4NjI0.C_Gfog.0vwCHEjspyBl_HFIJtRo7rB0voQ", true);
                final MessageReceiver receiver = api.getChannelById("189359733377990656");

                receiver.sendMessage("Farewell. <:timeforcrab:292796338645630978>");
            }
        }, "Shutdown-thread"));
    }

    public static boolean isOnList(String item, ArrayList<String> list) {

        for (String listName : list){
            if (item.equalsIgnoreCase(listName)) {
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
        if (s.equals("") || s.equals(null)) {
            return true;
        } else {
            return false;
        }
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
        return MathUtils.factorialDouble(x);
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
                "     1,                       ,\n" +
                "    1,                         ,\n" +
                "   1,                           ,\n" +
                "   1,                           ,\n" +
                "~~•1,                           ,\n" +
                "    1,                         ,\n" +
                "     1,                       ,\n" +
                "       1,                  , '\n" +
                "         1' - , _ _ _ ,  '\n" +
                "```";
        String returnString = "";
        if (sizeOfSpace == 0) {
             returnString = string.replace("1", "");
        } else {
            String spaces = multiplyString(" ", sizeOfSpace*2);
            returnString = string.replace("1", spaces);
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

}

