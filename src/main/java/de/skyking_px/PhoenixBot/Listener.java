package de.skyking_px.PhoenixBot;

import de.skyking_px.PhoenixBot.command.*;
import de.skyking_px.PhoenixBot.faq.FaqHandler;
import de.skyking_px.PhoenixBot.util.Reload;
import net.dv8tion.jda.api.JDA;
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
        JDA api = event.getJDA();
        api.retrieveCommands().queue(commands -> {
            for (Command cmd : commands) {
                api.deleteCommandById(cmd.getId()).queue();
            }
        });
        for (Guild guild : api.getGuilds()) {
            guild.retrieveCommands().queue(commands -> {
                for (Command cmd : commands) {
                    guild.deleteCommandById(cmd.getId()).queue();
                }
            });
        }
        logger.info("[BOT] Commands were reset");
        logger.info("[BOT] Applying Commands...");
        try {
            api.updateCommands()
                    .addCommands(CommandRegistry.regiserCommands())
                    .queue();
        } catch (Exception e) {
            logger.error("[BOT] An Error occurred while trying to register commands!", e);
        }
        try {
            Bot.initStorage();
        } catch (IOException e) {
            logger.error("[BOT] An Error occurred while trying to initialize voting storage!");
        }
        START_TIME = Instant.now();
        logger.info("[BOT] Bot is ready.");
    }
}
