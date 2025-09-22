package de.skyking_px.PhoenixBot.util;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.storage.TicketStorage;
import de.skyking_px.PhoenixBot.ticket.Panel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;

/**
 * Handles ticket closure operations for support tickets.
 * Provides confirmation dialogs and manages ticket deletion.
 * 
 * @author SkyKing_PX
 */
public class TicketCloseHandler extends ListenerAdapter {


    /**
     * Sends a confirmation dialog for ticket closure.
     * 
     * @param thread The ticket thread to close
     * @param invoker The member requesting closure
     * @param guild The Discord guild
     * @param event The button interaction event
     */
    public static void sendTicketCloseConfirmation(ThreadChannel thread, Member invoker, Guild guild, ButtonInteractionEvent event) {
        MessageEmbed confirmation = EmbedUtils.createConfirmation("Are you sure you want to close this ticket?",
                "This ticket will be permanently deleted and cannot be recovered.");

        Button confirmButton = Button.success("ticket_close_confirm:" + thread.getId(), "✅ Close Ticket");
        Button cancelButton = Button.danger("ticket_close_cancel:" + thread.getId(), "❌ Cancel");

        event.replyEmbeds(confirmation)
                .addActionRow(confirmButton, cancelButton)
                .setEphemeral(true)
                .queue();
    }

    /**
     * Closes a ticket with proper permission checking and cleanup.
     * 
     * @param thread The ticket thread to close
     * @param invoker The member requesting closure
     * @param guild The Discord guild
     * @throws IOException If there is a permission or configuration error
     */
    public static void closeTicket(ThreadChannel thread, Member invoker, Guild guild) throws IOException {
        String threadId = thread.getId();
        TicketStorage storage = Bot.getTicketStorage();

        String threadOwnerId = storage.getTicketUserId(threadId);
        String userId = invoker.getId();
        String[] modRoleIds;

        try {
            modRoleIds = Config.get().getRoles().getModerators();
        } catch (Exception e) {
            LogUtils.logException("Error loading mod role", e);
            throw new IOException("Config error");
        }

        boolean isOwner = userId.equals(threadOwnerId);
        boolean isModerator = false;
        for (String id : modRoleIds){
            if (invoker.getRoles().stream().anyMatch(r -> r.getId().equals(id))) {
                isModerator = true;
                break;
            }
        }

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
        MessageEmbed logEmbed = EmbedUtils.createLogEmbed("✅ Ticket Closed",
                "Ticket: " + ticketName + "\nClosed by: " + invoker.getUser().getName());

        MessageHandler.logToChannel(guild, logEmbed);

        // Send closure message to thread before deleting
        MessageEmbed closureEmbed = EmbedUtils.createSuccessEmbed("✅ Ticket Closed",
                "This ticket has been closed by " + invoker.getAsMention() + ".\n\n" +
                "The ticket will be deleted shortly.");

        String finalTicketName = ticketName;
        thread.sendMessageEmbeds(closureEmbed).queue(success -> {
            // Delete the thread after a short delay
            thread.delete().queueAfter(30, java.util.concurrent.TimeUnit.SECONDS,
                    deleteSuccess -> LogUtils.logInfo("[BOT] Successfully closed and deleted ticket: " + finalTicketName),
                    deleteError -> LogUtils.logException("[BOT] Failed to delete ticket thread: " + threadId, deleteError)
            );
        });
    }

    /**
     * Handles button interactions for ticket closure.
     * 
     * @param event The button interaction event
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.startsWith("ticket_close:")) {
            // Handle direct close button click
            String threadId = componentId.split(":")[1];
            Guild guild = event.getGuild();
            Member member = event.getMember();

        if (guild == null || member == null) {
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ Guild or Member not found."))
                    .setEphemeral(true).queue();
            return;
        }

        ThreadChannel thread = guild.getThreadChannelById(threadId);
        if (thread == null) {
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ Could not find the ticket thread."))
                    .setEphemeral(true).queue();
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
                event.getHook().editOriginalEmbeds(EmbedUtils.createSuccess()
                                .setDescription("✅ Ticket closed successfully.")
                                .build())
                        .setComponents()
                        .queue();
            } catch (IOException e) {
                event.getHook().editOriginalEmbeds(EmbedUtils.createError()
                                .setDescription("❌ " + e.getMessage())
                                .build())
                        .setComponents()
                        .queue();
            }

        } else if (componentId.startsWith("ticket_close_cancel:")) {
            // Handle cancellation
            event.deferEdit().queue();
            event.getHook().editOriginalEmbeds(EmbedUtils.createInfo()
                            .setDescription("❌ Ticket closure cancelled.")
                            .build())
                    .setComponents()
                    .queue();
        }
    }
}