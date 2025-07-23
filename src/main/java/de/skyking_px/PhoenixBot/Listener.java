package de.skyking_px.PhoenixBot;

import de.skyking_px.PhoenixBot.command.FAQCommand;
import de.skyking_px.PhoenixBot.command.InfoCommand;
import de.skyking_px.PhoenixBot.command.TBSCommand;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;

public class Listener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    public static Instant START_TIME;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Objects.requireNonNull(event.getJDA().getGuildById("1116745011837534239"))
                .updateCommands()
                .addCommands(TBSCommand.getTBSCommand(), FAQCommand.getFAQCommand(), InfoCommand.getInfoCommand())
                .queue();
        START_TIME = Instant.now();
        logger.info("[BOT] Bot is ready.");
    }
}
