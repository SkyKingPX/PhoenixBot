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
import java.util.concurrent.TimeUnit;

public class Listener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    public static Instant START_TIME;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA api = event.getJDA();
        try {
            logger.info("[BOT] Resetting Commands...");
            api.retrieveCommands().queue(commands -> {
                for (Command cmd : commands) {
                    api.deleteCommandById(cmd.getId()).queue();
                }
            });
            TimeUnit.SECONDS.sleep(2);
            for (Guild guild : api.getGuilds()) {
                guild.retrieveCommands().queue(commands -> {
                    for (Command cmd : commands) {
                        guild.deleteCommandById(cmd.getId()).queue();
                    }
                });
            }
            logger.info("[BOT] Commands were reset.");
        } catch (Exception e) {
            logger.error("[BOT] An Error occurred while trying to reset commands!", e);
        }
        try {
            TimeUnit.SECONDS.sleep(10);
            logger.info("[BOT] Applying Commands...");
            api.updateCommands()
                    .addCommands(CommandRegistry.regiserCommands())
                    .queue();
            logger.info("[BOT] Commands applied.");
        } catch (Exception e) {
            logger.error("[BOT] An Error occurred while trying to register commands!", e);
        }
        try {
            logger.info("[BOT] Initializing voting storage...");
            Bot.initStorage();
            logger.info("[BOT] Voting storage initialized.");
        } catch (IOException e) {
            logger.error("[BOT] An Error occurred while trying to initialize voting storage!");
        }
        START_TIME = Instant.now();
        logger.info("[BOT] Bot is ready.");
    }
}
