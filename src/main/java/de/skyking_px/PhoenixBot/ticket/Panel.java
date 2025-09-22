package de.skyking_px.PhoenixBot.ticket;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.storage.TicketStorage;
import de.skyking_px.PhoenixBot.util.EmbedUtils;
import de.skyking_px.PhoenixBot.util.LogUtils;
import de.skyking_px.PhoenixBot.util.MessageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.io.IOException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Manages ticket panel creation and ticket lifecycle automation.
 * Handles ticket creation, response tracking, and automatic cleanup after 24 hours.
 * 
 * @author SkyKing_PX
 */
public class Panel extends ListenerAdapter {

    
    /** Map tracking pending tickets and their scheduled deletion tasks */
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> pendingTickets = new ConcurrentHashMap<>();
    /** Scheduler for automated ticket cleanup tasks */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * Gets the map of pending tickets for external access.
     * 
     * @return Map of thread IDs to scheduled deletion tasks
     */
    public static ConcurrentHashMap<String, ScheduledFuture<?>> getPendingTickets() {
        return pendingTickets;
    }

    private static void deleteExpiredTicket(String threadId, String ticketName, String guildId, TicketStorage storage) {
        try {
            Guild guild = Bot.class.getClassLoader().getResource("") != null 
                ? storage != null ? null : null
                : null;
        } catch (Exception e) {
            LogUtils.logException("Error in deleteExpiredTicket", e);
        }
    }

    /**
     * Restores pending ticket deletion tasks after bot restart.
     * Calculates remaining time for each pending ticket and schedules deletion.
     * 
     * @param api JDA instance for accessing Discord entities
     */
    public static void restorePendingTickets(JDA api) {
        try {
            TicketStorage storage = Bot.getTicketStorage();
            Map<String, TicketStorage.TicketInfo> tickets = storage.loadAllPendingTickets();
            
            long currentTime = System.currentTimeMillis();
            
            for (Map.Entry<String, TicketStorage.TicketInfo> entry : tickets.entrySet()) {
                String threadId = entry.getKey();
                TicketStorage.TicketInfo info = entry.getValue();
                
                long timeElapsed = currentTime - info.creationTime;
                long timeRemaining = TimeUnit.HOURS.toMillis(24) - timeElapsed;

                if (timeRemaining <= 0) {
                    // Ticket has already expired, delete it immediately
                    LogUtils.logInfo("Deleting expired ticket thread: " + info.ticketName + " (" + threadId + ")");
                    Guild guild = api.getGuildById(info.guildId);
                    if (guild != null) {
                        ThreadChannel thread = guild.getThreadChannelById(threadId);
                        if (thread != null) {
                            // Get user info for logging
                            User ticketUser = api.getUserById(info.userId);
                            String userName = ticketUser != null ? ticketUser.getName() : "Unknown User";

                            thread.delete().queue(
                                success -> {
                                    LogUtils.logInfo("Successfully deleted expired ticket thread: " + info.ticketName);

                            // Log to channel
                            MessageEmbed logEmbed = EmbedUtils.createWarning()
                                    .setTitle("⏰ Ticket Expired")
                                    .addField("Ticket", info.ticketName, true)
                                    .addField("User", userName, true)
                                    .addField("Reason", "No response within 24 hours", false)
                                    .build();

                                    MessageHandler.logToChannel(guild, logEmbed);

                                    try {
                                        storage.removeTicket(threadId);
                                    } catch (IOException e) {
                                        LogUtils.logException("Failed to remove ticket from storage: " + threadId, e);
                                    }
                                },
                                error -> {
                                    LogUtils.logException("Failed to delete expired ticket thread: " + threadId, error);
                                    // Still try to clean up storage
                                    try {
                                        storage.removeTicket(threadId);
                                    } catch (IOException e) {
                                        LogUtils.logException("Failed to remove ticket from storage: " + threadId, e);
                                    }
                                }
                            );
                        } else {
                            // Thread doesn't exist anymore, just remove from storage
                            try {
                                storage.removeTicket(threadId);
                                LogUtils.logInfo("Removed non-existent ticket from storage: " + info.ticketName);
                            } catch (IOException e) {
                                LogUtils.logException("Failed to remove ticket from storage: " + threadId, e);
                            }
                        }
                    }
                } else {
                    // Schedule deletion for remaining time
                    ScheduledFuture<?> deletionTask = scheduler.schedule(() -> {
                        LogUtils.logInfo("Deleting inactive ticket thread: " + info.ticketName + " (" + threadId + ")");
                        Guild guild = api.getGuildById(info.guildId);
                        if (guild != null) {
                            ThreadChannel thread = guild.getThreadChannelById(threadId);
                            if (thread != null) {
                                // Get user info for logging
                                User ticketUser = api.getUserById(info.userId);
                                String userName = ticketUser != null ? ticketUser.getName() : "Unknown User";

                                thread.delete().queue(
                                    success -> {
                                        LogUtils.logInfo("Successfully deleted inactive ticket thread: " + info.ticketName);

                                        // Log to channel
                                        MessageEmbed logEmbed = EmbedUtils.createWarning()
                                                .setTitle("⏰ Ticket Expired")
                                                .addField("Ticket", info.ticketName, true)
                                                .addField("User", userName, true)
                                                .addField("Reason", "No response within 24 hours", false)
                                                .build();

                                        MessageHandler.logToChannel(guild, logEmbed);

                                        try {
                                            storage.removeTicket(threadId);
                                        } catch (IOException e) {
                                            LogUtils.logException("Failed to remove ticket from storage: " + threadId, e);
                                        }
                                    },
                                    error -> {
                                        LogUtils.logException("Failed to delete inactive ticket thread: " + threadId, error);
                                        // Still try to clean up storage
                                        try {
                                            storage.removeTicket(threadId);
                                        } catch (IOException e) {
                                            LogUtils.logException("Failed to remove ticket from storage: " + threadId, e);
                                        }
                                    }
                                );
                            } else {
                                // Thread doesn't exist, just clean up storage
                                try {
                                    storage.removeTicket(threadId);
                                    LogUtils.logInfo("Cleaned up non-existent ticket from storage: " + info.ticketName);
                                } catch (IOException e) {
                                    LogUtils.logException("Failed to remove ticket from storage: " + threadId, e);
                                }
                            }
                        }
                        pendingTickets.remove(threadId);
                    }, timeRemaining, TimeUnit.MILLISECONDS);

                    pendingTickets.put(threadId, deletionTask);
                    LogUtils.logDebug("Restored deletion task for ticket thread: " + info.ticketName + " (remaining: " + timeRemaining + "ms)");
                }
            }

            LogUtils.logInfo("Restored " + pendingTickets.size() + " pending tickets");
        } catch (Exception e) {
            LogUtils.logException("Error restoring pending tickets", e);
        }
    }

    /**
     * Handles the /createticketpanel slash command.
     * Creates a ticket panel with a button for users to create support tickets.
     * 
     * @param event The slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("createticketpanel")) {
            event.deferReply(true).queue();
                try {
                    String ownerId = Config.get().getBot().getOwner_id();
                    if (!event.getUser().getId().equals(ownerId)) {
                        LogUtils.logCommandFailure("createticketpanel", event.getUser().getId(), "Unauthorized access attempt");
                        event.replyEmbeds(EmbedUtils.createSimpleError("❌ You are not authorized to use this command."))
                                .setEphemeral(true).queue();
                        return;
                    }
                } catch (IOException e) {
                    LogUtils.logException("Error checking authorization for createticketpanel", e);
                    return;
                }

            String channelId = event.getOption("channel").getAsChannel().getId();
            GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelId);

            if (guildChannel == null) {
                LogUtils.logEmptyException("Channel not found: " + channelId);
                return;
            }

                MessageEmbed success = EmbedUtils.createSuccessEmbed("✅ Ticket Panel created", 
                        "The ticket panel has been created in " + event.getOption("channel").getAsChannel().getJumpUrl() + ".");

                MessageEmbed prompt = EmbedUtils.createTicketEmbed("Create a Support Ticket", 
                        "Click on the button below to create a support ticket for our Moderators to help you out.");

            switch (guildChannel.getType()) {
                case TEXT -> {
                    ((TextChannel) guildChannel).sendMessageEmbeds(prompt).addActionRow(
                            Button.danger("create_ticket:" + guildChannel.getId(), "Create Ticket")
                ).queue();
                    event.getHook().sendMessageEmbeds(success).setEphemeral(true).queue();
                }
                case NEWS -> {
                    ((NewsChannel) guildChannel).sendMessageEmbeds(prompt).addActionRow(
                            Button.danger("create_ticket:" + guildChannel.getId(), "Create Ticket")
                ).queue();
                    event.getHook().sendMessageEmbeds(success).setEphemeral(true).queue();
                }
                default -> {
                    MessageEmbed fail = EmbedUtils.createErrorEmbed("❌ Ticket Panel creation failed", 
                            "Channel is not a text or announcement channel: " + channelId);
                    event.getHook().sendMessageEmbeds(fail).setEphemeral(true).queue();
                }
            }
        }
    }

    /**
     * Handles ticket creation button interactions.
     * Creates new support tickets with automatic cleanup scheduling.
     * 
     * @param event The button interaction event
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("create_ticket:")) return;

        LogUtils.logDebug("Button pressed: " + event.getComponentId());
        String buttonID = event.getComponentId();
        Guild guild = event.getGuild();
        if ( guild == null ) return;
        TextChannel channelID = guild.getTextChannelById(buttonID.split(":")[1]);
        Member member = event.getMember();

        if (member == null) return;

        // Generate unique ticket name with username and global counter
        TicketStorage storage = Bot.getTicketStorage();
        String ticketName;
        try {
            ticketName = storage.generateTicketName(member.getUser().getName());
        } catch (IOException e) {
            LogUtils.logException("Failed to generate ticket name", e);
            event.reply("❌ Failed to create ticket. Please try again later.").setEphemeral(true).queue();
            return;
        }

        LogUtils.logDebug("Creating Support thread with name: " + ticketName);
        channelID.createThreadChannel(ticketName, true).queue(threadChannel -> {
            threadChannel.addThreadMember(member).queue();

            StringBuilder ping = new StringBuilder();
            try {
                String[] pingRoles = Config.get().getTickets().getPingRoles();
                if (pingRoles.length > 0) {
                    List<String> roleMentions = threadChannel.getGuild().getRoles().stream()
                            .filter(role -> List.of(pingRoles).contains(role.getId()))
                            .map(role -> "<@&" + role.getId() + ">")
                            .toList();
                    ping.append(String.join(" ", roleMentions)).append("\n");
                }
            } catch (IOException e) {
                LogUtils.logException("Error while getting ping roles", e);
            }

            MessageEmbed ticketCreated = new EmbedBuilder()
                    .setColor(HexFormat.fromHexDigits("2073cb"))
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .setTitle("✅ Support Ticket created")
                    .setDescription("Your support ticket **" + ticketName + "** has been created: " + threadChannel.getAsMention() + "\n\n"
                            + "Please describe your issue in the thread so\n"
                            + "our Moderators can assist you as soon as possible.")
                    .build();

            event.replyEmbeds(ticketCreated).setEphemeral(true).queue();

            MessageEmbed ticketPrompt = new EmbedBuilder()
                    .setColor(HexFormat.fromHexDigits("2073cb"))
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .setTitle("Support Ticket: " + ticketName)
                    .setDescription("Hello " + member.getAsMention() + ",\n\n"
                            + "Thank you for reaching out to us!\n\n"
                            + "Please describe your issue and provide as much detail as possible so we can better assist you.\n\n"
                            + "> **Important:** Our Moderators will only be able to see this ticket if you respond in this thread within 24 hours.\n\n"
                            + "Use the button below to close this ticket when your issue is resolved.")
                    .build();

            Button closeButton = Button.danger("ticket_close:" + threadChannel.getId(), "🗑️ Close Ticket");

            threadChannel.sendMessageEmbeds(ticketPrompt)
                    .addActionRow(closeButton)
                    .queue();

            // Save ticket to persistent storage with the unique name
            try {
                long creationTime = System.currentTimeMillis();
                storage.saveTicket(threadChannel.getId(), ticketName, member.getId(), guild.getId(), creationTime);
                LogUtils.logDebug("Saved ticket to storage: " + ticketName + " (" + threadChannel.getId() + ")");
            } catch (IOException e) {
                LogUtils.logException("Failed to save ticket to storage: " + threadChannel.getId(), e);
            }

            ScheduledFuture<?> deletionTask = scheduler.schedule(() -> {
                LogUtils.logInfo("Deleting inactive ticket thread: " + ticketName + " (" + threadChannel.getId() + ")");

                // Get user info for logging before deletion
                String userName = member.getUser().getName();

                threadChannel.delete().queue(
                    success -> {
                        LogUtils.logInfo("Successfully deleted inactive ticket thread: " + ticketName);

                        // Log to channel
                        MessageEmbed logEmbed = new EmbedBuilder()
                                .setColor(Color.ORANGE)
                                .setTitle("⏰ Ticket Expired")
                                .addField("Ticket", ticketName, true)
                                .addField("User", userName, true)
                                .addField("Reason", "No response within 24 hours", false)
                                .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                                .build();

                        MessageHandler.logToChannel(guild, logEmbed);

                        try {
                            storage.removeTicket(threadChannel.getId());
                        } catch (IOException e) {
                            LogUtils.logException("Failed to remove ticket from storage: " + threadChannel.getId(), e);
                        }
                    },
                    error -> {
                        LogUtils.logException("Failed to delete inactive ticket thread: " + threadChannel.getId(), error);
                        // Still try to clean up storage
                        try {
                            storage.removeTicket(threadChannel.getId());
                        } catch (IOException e) {
                            LogUtils.logException("Failed to remove ticket from storage: " + threadChannel.getId(), e);
                        }
                    }
                );
                pendingTickets.remove(threadChannel.getId());
            }, 24, TimeUnit.HOURS);
            
            // Store the deletion task
            pendingTickets.put(threadChannel.getId(), deletionTask);
            LogUtils.logDebug("Scheduled deletion for ticket thread: " + ticketName);
        });
    }
    
    /**
     * Handles user messages in ticket threads.
     * Cancels automatic deletion when users respond to their tickets.
     * 
     * @param event The message received event
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bot messages
        if (event.getAuthor().isBot()) return;
        
        // Check if message is in a ticket thread
        if (event.getChannel() instanceof ThreadChannel threadChannel) {
            String threadId = threadChannel.getId();
            TicketStorage storage = Bot.getTicketStorage();
            
            // Check if this is a pending ticket (both in memory and storage)
            ScheduledFuture<?> deletionTask = pendingTickets.get(threadId);
            boolean isPendingInStorage = storage.isTicketPending(threadId);
            
            if (deletionTask != null || isPendingInStorage) {
                // Cancel the deletion task if it exists
                if (deletionTask != null) {
                    deletionTask.cancel(false);
                    pendingTickets.remove(threadId);
                    LogUtils.logDebug("Cancelled deletion task for ticket thread: " + threadId);
                }
                
                // Mark ticket as responded in storage
                try {
                    storage.markTicketResponded(threadId);
                } catch (IOException e) {
                    LogUtils.logException("Failed to mark ticket as responded: " + threadId, e);
                }
                
                String ticketName = storage.getTicketName(threadId);
                LogUtils.logDebug("User responded to ticket thread: " + ticketName + ", cancelling deletion");
                
                // Send thank you message
                MessageEmbed thankYou = new EmbedBuilder()
                        .setColor(HexFormat.fromHexDigits("2073cb"))
                        .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                        .setTitle("Thank You!")
                        .setDescription("Thank you for your response! Our Moderators have been notified and will assist you shortly.")
                        .build();

                StringBuilder ping = new StringBuilder();
                try {
                    String[] pingRoles = Config.get().getTickets().getPingRoles();
                    if (pingRoles.length > 0) {
                        List<String> roleMentions = threadChannel.getGuild().getRoles().stream()
                                .filter(role -> List.of(pingRoles).contains(role.getId()))
                                .map(role -> "<@&" + role.getId() + ">")
                                .toList();
                        ping.append(String.join(" ", roleMentions)).append("\n");
                    }
                } catch (IOException e) {
                    LogUtils.logException("Error while getting ping roles", e);
                }
                
                threadChannel.sendMessage(ping.toString()).queue();
                threadChannel.sendMessageEmbeds(thankYou).queue();
            }
        }
    }
}
