package de.skyking_px.PhoenixBot;

import de.skyking_px.PhoenixBot.command.CommandRegistry;
import de.skyking_px.PhoenixBot.ticket.Panel;
import de.skyking_px.PhoenixBot.util.LogUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Main event listener for the PhoenixBot.
 * Handles bot initialization, command registration, and startup tasks.
 *
 * @author SkyKing_PX
 */
public class Listener extends ListenerAdapter {

    /**
     * Timestamp when the bot started
     */
    public static Instant START_TIME;

    /**
     * Handles the bot ready event.
     * Registers slash commands, initializes storage systems, and restores pending tickets.
     *
     * @param event The ReadyEvent from JDA
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA api = event.getJDA();
        LogUtils.logInfo("Registering Commands...");

        api.updateCommands()
                .addCommands(CommandRegistry.registerCommands())
                .queue(success -> LogUtils.logInfo("Global commands updated."));

        try {
            Bot.initStorage();

            LogUtils.logInfo("Restoring pending tickets...");
            Panel.restorePendingTickets(api);
            LogUtils.logInfo("Pending tickets restored.");
        } catch (Exception e) {
            LogUtils.logFatalException(api, "An Error occurred while trying to initialize storage!", e);
        }

        START_TIME = Instant.now();
        LogUtils.logInfo("Bot is ready.");
    }
}