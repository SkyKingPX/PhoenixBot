package de.skyking_px.PhoenixBot;

import de.skyking_px.PhoenixBot.command.FAQCommand;
import de.skyking_px.PhoenixBot.command.InfoCommand;
import de.skyking_px.PhoenixBot.command.TBSCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Listener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    public static Instant START_TIME;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Guild guild = event.getJDA().getGuildById("1116745011837534239");
        if (guild != null) {
            guild.retrieveCommands().queue(commands -> {
                for (Command cmd : commands) {
                    guild.deleteCommandById(cmd.getId()).queue();
                }
            });
        }
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            logger.error("[BOT] An error occurred while waiting for the bot to finish its startup.");
        }
        Objects.requireNonNull(guild)
                .updateCommands()
                .addCommands(TBSCommand.getTBSCommand(), FAQCommand.getFAQCommand(), InfoCommand.getInfoCommand())
                .queue();
        START_TIME = Instant.now();
        logger.info("[BOT] Bot is ready.");
    }
}
