package de.skyking_px.PhoenixBot;

import de.skyking_px.PhoenixBot.command.CloseCommand;
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

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

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
        logger.info("[BOT] Commands were reset");
        logger.info("[BOT] Applying Commands...");
        try {
            if (Config.get().getCommands().isClose_enabled()) {
                Objects.requireNonNull(guild)
                        .updateCommands()
                        .addCommands(TBSCommand.getTBSCommand(), FAQCommand.getFAQCommand(), InfoCommand.getInfoCommand(), CloseCommand.getCloseCommand())
                        .queue();
            } else {
                Objects.requireNonNull(guild)
                        .updateCommands()
                        .addCommands(TBSCommand.getTBSCommand(), FAQCommand.getFAQCommand(), InfoCommand.getInfoCommand())
                        .queue();
            }
        } catch (IOException e) {
            logger.error("[BOT] An Error occurred while trying to register commands!");
        }
        START_TIME = Instant.now();
        logger.info("[BOT] Bot is ready.");
    }
}
