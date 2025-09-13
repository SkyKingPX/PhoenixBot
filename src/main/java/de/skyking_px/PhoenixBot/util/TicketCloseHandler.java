package de.skyking_px.PhoenixBot.util;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.storage.TicketStorage;
import de.skyking_px.PhoenixBot.ticket.Panel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;

public class TicketCloseHandler extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public static void sendTicketCloseConfirmation(ThreadChannel thread, Member invoker, Guild guild, ButtonInteractionEvent event) {
        MessageEmbed confirmation = new EmbedBuilder()
                .setColor(Color.RED)
                .addField("Are you sure you want to close this ticket?",
                        "This ticket will be permanently deleted and cannot be recovered.",
                        false)
                .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                .build();

        Button confirmButton = Button.success("ticket_close_confirm:" + thread.getId(), "✅ Close Ticket");
        Button cancelButton = Button.danger("ticket_close_cancel:" + thread.getId(), "❌ Cancel");

        event.replyEmbeds(confirmation)
                .addActionRow(confirmButton, cancelButton)
                .setEphemeral(true)
                .queue();
    }

    public static void closeTicket(ThreadChannel thread, Member invoker, Guild guild) throws IOException {
        String threadId = thread.getId();
        TicketStorage storage = Bot.getTicketStorage();

        String threadOwnerId = storage.getTicketUserId(threadId);
        String userId = invoker.getId();
        String modRoleId;

        try {
            modRoleId = Config.get().getRoles().getModerator();
        } catch (Exception e) {
            logger.error("Error loading mod role", e);
            throw new IOException("Config error");
        }

        boolean isOwner = userId.equals(threadOwnerId);
        boolean isModerator = invoker.getRoles().stream().anyMatch(r -> r.getId().equals(modRoleId));

        if (!isOwner && !isModerator) {
            throw new IOException("Only the ticket creator or a moderator can close this ticket.");
        }

        String ticketName = storage.getTicketName(threadId);
        if (ticketName == null) {
            ticketName = "Unknown Ticket";
        }

        // Cancel any pending deletion task
        ScheduledFuture<?> deletionTask = Panel.getPendingTickets().remove(threadId);
        if (deletionTask != null) {
            deletionTask.cancel(false);
        }

        // Remove from storage first
        storage.removeTicket(threadId);

        // Log the closure
        MessageEmbed logEmbed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .addField("✅ Ticket Closed",
                        "Ticket: " + ticketName +
                                "\nClosed by: " + invoker.getUser().getName(),
                        false)
                .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                .build();

        MessageHandler.logToChannel(guild, logEmbed);

        // Send closure message to thread before deleting
        MessageEmbed closureEmbed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("✅ Ticket Closed")
                .setDescription("This ticket has been closed by " + invoker.getAsMention() + ".\n\n"
                        + "The ticket will be deleted shortly.")
                .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                .build();

        String finalTicketName = ticketName;
        thread.sendMessageEmbeds(closureEmbed).queue(success -> {
            // Delete the thread after a short delay
            thread.delete().queueAfter(30, java.util.concurrent.TimeUnit.SECONDS,
                    deleteSuccess -> logger.info("[BOT] Successfully closed and deleted ticket: " + finalTicketName),
                    deleteError -> logger.error("[BOT] Failed to delete ticket thread: " + threadId, deleteError)
            );
        });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.startsWith("ticket_close:")) {
            // Handle direct close button click
            String threadId = componentId.split(":")[1];
            Guild guild = event.getGuild();
            Member member = event.getMember();

            if (guild == null || member == null) {
                event.reply("❌ Guild or Member not found.").setEphemeral(true).queue();
                return;
            }

            ThreadChannel thread = guild.getThreadChannelById(threadId);
            if (thread == null) {
                event.reply("❌ Could not find the ticket thread.").setEphemeral(true).queue();
                return;
            }

            // Check if this is actually a ticket thread
            if (!Bot.getTicketStorage().isTicketThread(threadId)) {
                event.reply("❌ This is not a valid ticket thread.").setEphemeral(true).queue();
                return;
            }

            sendTicketCloseConfirmation(thread, member, guild, event);

        } else if (componentId.startsWith("ticket_close_confirm:")) {
            // Handle confirmation
            String threadId = componentId.split(":")[1];
            Guild guild = event.getGuild();
            Member member = event.getMember();

            if (guild == null || member == null) {
                event.reply("❌ Guild or Member not found.").setEphemeral(true).queue();
                return;
            }

            ThreadChannel thread = guild.getThreadChannelById(threadId);
            if (thread == null) {
                event.reply("❌ Could not find the ticket thread.").setEphemeral(true).queue();
                return;
            }

            event.deferEdit().queue();

            try {
                closeTicket(thread, member, guild);
                event.getHook().editOriginalEmbeds(new EmbedBuilder()
                                .setColor(Color.GREEN)
                                .setDescription("✅ Ticket closed successfully.")
                                .build())
                        .setComponents()
                        .queue();
            } catch (IOException e) {
                event.getHook().editOriginalEmbeds(new EmbedBuilder()
                                .setColor(Color.RED)
                                .setDescription("❌ " + e.getMessage())
                                .build())
                        .setComponents()
                        .queue();
            }

        } else if (componentId.startsWith("ticket_close_cancel:")) {
            // Handle cancellation
            event.deferEdit().queue();
            event.getHook().editOriginalEmbeds(new EmbedBuilder()
                            .setColor(Color.YELLOW)
                            .setDescription("❌ Ticket closure cancelled.")
                            .build())
                    .setComponents()
                    .queue();
        }
    }
}