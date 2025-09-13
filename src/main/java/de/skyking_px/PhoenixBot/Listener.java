package de.skyking_px.PhoenixBot;

import de.skyking_px.PhoenixBot.command.*;
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
import java.util.concurrent.TimeUnit;
import de.skyking_px.PhoenixBot.storage.TicketStorage;
import de.skyking_px.PhoenixBot.ticket.Panel;

public class Listener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    public static Instant START_TIME;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA api = event.getJDA();

        logger.info("[BOT] Registering Commands...");

        api.updateCommands()
                .addCommands(CommandRegistry.registerCommands())
                .queue(success -> logger.info("[BOT] Global commands updated."));

        try {
            logger.info("[BOT] Initializing voting storage...");
            Bot.initStorage();
            logger.info("[BOT] Voting storage initialized.");
            
            logger.info("[BOT] Restoring pending tickets...");
            Panel.restorePendingTickets(api);
            logger.info("[BOT] Pending tickets restored.");
        } catch (IOException e) {
            logger.error("[BOT] An Error occurred while trying to initialize storage!");
        }

        START_TIME = Instant.now();
        logger.info("[BOT] Bot is ready.");
    }
}