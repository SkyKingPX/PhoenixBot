package de.skyking_px.PhoenixBot;

import de.skyking_px.PhoenixBot.command.CloseCommand;
import de.skyking_px.PhoenixBot.command.FAQCommand;
import de.skyking_px.PhoenixBot.command.InfoCommand;
import de.skyking_px.PhoenixBot.command.TBSCommand;
import de.skyking_px.PhoenixBot.faq.FaqHandler;
import de.skyking_px.PhoenixBot.listener.BugReportListener;
import de.skyking_px.PhoenixBot.listener.SuggestionListener;
import de.skyking_px.PhoenixBot.listener.SupportListener;
import de.skyking_px.PhoenixBot.listener.ThreadDeleteListener;
import de.skyking_px.PhoenixBot.storage.TicketStorage;
import de.skyking_px.PhoenixBot.storage.VoteStorage;
import de.skyking_px.PhoenixBot.ticket.Panel;
import de.skyking_px.PhoenixBot.util.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;

/**
 * Main Bot class for the PhoenixBot Discord application.
 * This class initializes the bot, registers event listeners, and configures JDA.
 *
 * @author SkyKing_PX
 */
public class Bot {
    /**
     * Current version of the bot
     */
    public static final String VERSION = "2.0.0";


    /**
     * Storage for vote data across suggestion forums
     */
    private static VoteStorage voteStorage;
    /**
     * Storage for ticket data and tracking
     */
    private static TicketStorage ticketStorage;

    /**
     * Initializes the storage systems for votes and tickets.
     * This method must be called before accessing any storage-related functionality.
     *
     * @throws IOException If there is an error initializing the storage files
     */
    public static void initStorage() {
        LogUtils.logStorage("Initializing...", "Vote Storage");
        try {
            voteStorage = new VoteStorage();
        } catch (Exception e) {
            LogUtils.logFatalException("Error initializing vote storage", e);
        }
        LogUtils.logStorage("Initialized", "Vote Storage");
        LogUtils.logStorage("Initializing...", "Ticket Storage");
        try {
            ticketStorage = new TicketStorage();
        } catch (Exception e) {
            LogUtils.logFatalException("Error initializing ticket storage", e);
        }
        LogUtils.logStorage("Initializing", "Ticket Storage");
    }

    /**
     * Gets the vote storage instance for managing suggestion votes.
     *
     * @return The vote storage instance
     */
    public static VoteStorage getVoteStorage() {
        return voteStorage;
    }

    /**
     * Gets the ticket storage instance for managing support tickets.
     *
     * @return The ticket storage instance
     */
    public static TicketStorage getTicketStorage() {
        return ticketStorage;
    }

    /**
     * Main entry point for the PhoenixBot application.
     * Initializes storage, configures JDA, and registers all event listeners.
     *
     * @param args Command line arguments (not used)
     * @throws Exception If any error occurs during initialization
     */
    public static void main(String[] args) throws Exception {
        initStorage();

        String activity = "Incorrect Configuration";
        try {
            activity = Config.get().getBot().getActivity();
            activity = activity.replace("{Version}", VERSION);
        } catch (IOException e) {
            LogUtils.logException("Error loading Activity from Config. It may be corrupted", e);
        }

        JDA api = JDABuilder.createDefault(Config.get().getBot().getToken())
                .addEventListeners(
                        new TBSCommand(),
                        new InfoCommand(),
                        new FAQCommand(),
                        new LogUploader(),
                        new Listener(),
                        new CloseCommand(),
                        new SuggestionListener(Bot.getVoteStorage()),
                        new BugReportListener(),
                        new SupportListener(),
                        new CloseHandler(),
                        new FaqHandler(),
                        new Reload(),
                        new Panel(),
                        new TicketCloseHandler(),
                        new ThreadDeleteListener())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing(activity))
                .setStatus(OnlineStatus.ONLINE)
                .build();
    }
}
