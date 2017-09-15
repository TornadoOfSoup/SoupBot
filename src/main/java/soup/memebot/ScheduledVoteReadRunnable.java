package soup.memebot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.Reaction;

import java.util.Comparator;
import java.util.List;

import static soup.memebot.Utils.getTimestampFull;

/**
 * Created by RPGenius on 9/14/2017.
 */
public class ScheduledVoteReadRunnable implements Runnable {

    String threadName;
    long timerLength;
    private Thread thread;
    String question;
    private Message message;
    private DiscordAPI api;

    public ScheduledVoteReadRunnable(String threadName, double timerLengthInMinutes, String question, Message message, DiscordAPI api) {
        this.threadName = threadName;
        this.timerLength = Math.round(timerLengthInMinutes * 1000 * 60);
        this.message = message;
        this.api = api;
        this.question = question;
        System.out.println(getTimestampFull() + " Creating " + this.getClass().getName() + " thread called " + threadName);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(timerLength);
            List<Reaction> reactions = api.getMessageById(message.getId()).getReactions();
            StringBuilder builder = new StringBuilder();

            int totalAmountOfReactions = 0;
            for (Reaction reaction : reactions) {
                totalAmountOfReactions += reaction.getCount();
            }

            reactions.sort(new Comparator<Reaction>() {
                @Override
                public int compare(Reaction o1, Reaction o2) {
                    if (o1.getCount() < o2.getCount()) {
                        return 1;
                    } else if (o1.getCount() > o2.getCount()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });


            int i = 0;
            for (Reaction reaction : reactions) {
                System.out.println(reaction.getUnicodeEmoji() + ": " + (double) Math.round((double) reaction.getCount() / totalAmountOfReactions * 10000) / 100 + "%");
                builder.append(reaction.getUnicodeEmoji() + ": " + (double) Math.round((double) reaction.getCount() / totalAmountOfReactions * 10000) / 100 + "%    ");
                if (i == 5) {
                    builder.append("\n");
                    i = 0;
                } else {
                    i++;
                }
            }

            message.reply("```\n" +
                    "\"" + question + "\"\n\n" +
                    builder.toString() +
                    "```");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start () {
        System.out.println(getTimestampFull() + " Starting " +  threadName + "!");
        if (thread == null) {
            thread = new Thread (this, threadName);
            thread.start();
        }
    }
}
