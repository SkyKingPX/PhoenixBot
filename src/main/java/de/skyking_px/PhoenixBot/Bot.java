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
import de.skyking_px.PhoenixBot.storage.VoteStorage;
import de.skyking_px.PhoenixBot.ticket.Panel;
import de.skyking_px.PhoenixBot.util.CloseHandler;
import de.skyking_px.PhoenixBot.util.LogUploader;
import de.skyking_px.PhoenixBot.util.Reload;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Bot {
    public static final String VERSION = "2.0.0-rc3";
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    private static VoteStorage voteStorage;

    public static void initStorage() throws IOException {
        voteStorage = new VoteStorage();
    }

    public static VoteStorage getVoteStorage() {
        return voteStorage;
    }

    public static void main(String[] args) throws Exception {
        initStorage();

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
                        new ThreadDeleteListener())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing(Config.get().getBot().getActivity()))
                .setStatus(OnlineStatus.ONLINE)
                .build();
    }
}
