package de.skyking_px.PhoenixBot;

import de.skyking_px.PhoenixBot.command.CloseCommand;
import de.skyking_px.PhoenixBot.command.FAQCommand;
import de.skyking_px.PhoenixBot.command.InfoCommand;
import de.skyking_px.PhoenixBot.command.TBSCommand;
import de.skyking_px.PhoenixBot.listener.BugReportListener;
import de.skyking_px.PhoenixBot.listener.SuggestionListener;
import de.skyking_px.PhoenixBot.listener.SupportListener;
import de.skyking_px.PhoenixBot.util.CloseHandler;
import de.skyking_px.PhoenixBot.util.LogUploader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bot {
    public static final String VERSION = "2.0.0-rc1";
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] arguments) throws Exception {
        Config config = Config.get();

        JDA api = JDABuilder.createDefault(Config.get().getBot().getToken())
                .addEventListeners(
                        new TBSCommand(),
                        new InfoCommand(),
                        new FAQCommand(),
                        new LogUploader(),
                        new Listener(),
                        new CloseCommand(),
                        new SuggestionListener(),
                        new BugReportListener(),
                        new SupportListener(),
                        new CloseHandler())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing(Config.get().getBot().getActivity()))
                .setStatus(OnlineStatus.ONLINE)
                .build();
    }

}
