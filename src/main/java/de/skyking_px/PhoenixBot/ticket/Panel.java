package de.skyking_px.PhoenixBot.ticket;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.storage.TicketStorage;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Panel extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    
    // Track pending tickets and their scheduled deletion tasks
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> pendingTickets = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // Public getter for TicketCloseHandler
    public static ConcurrentHashMap<String, ScheduledFuture<?>> getPendingTickets() {
        return pendingTickets;
    }

    private static void deleteExpiredTicket(String threadId, String ticketName, String guildId, TicketStorage storage) {
        try {
            Guild guild = Bot.class.getClassLoader().getResource("") != null 
                ? storage != null ? null : null
                : null;
        } catch (Exception e) {
            logger.error("[BOT] Error in deleteExpiredTicket", e);
        }
    }

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
                    logger.info("[BOT] Deleting expired ticket thread: " + info.ticketName + " (" + threadId + ")");
                    Guild guild = api.getGuildById(info.guildId);
                    if (guild != null) {
                        ThreadChannel thread = guild.getThreadChannelById(threadId);
                        if (thread != null) {
                            // Get user info for logging
                            User ticketUser = api.getUserById(info.userId);
                            String userName = ticketUser != null ? ticketUser.getName() : "Unknown User";

                            thread.delete().queue(
                                success -> {
                                    logger.info("[BOT] Successfully deleted expired ticket thread: " + info.ticketName);

                                    // Log to channel
                                    MessageEmbed logEmbed = new EmbedBuilder()
                                            .setColor(Color.ORANGE)
                                            .setTitle("⏰ Ticket Expired")
                                            .addField("Ticket", info.ticketName, true)
                                            .addField("User", userName, true)
                                            .addField("Reason", "No response within 24 hours", false)
                                            .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                                            .build();

                                    MessageHandler.logToChannel(guild, logEmbed);

                                    try {
                                        storage.removeTicket(threadId);
                                    } catch (IOException e) {
                                        logger.error("[BOT] Failed to remove ticket from storage: " + threadId, e);
                                    }
                                },
                                error -> {
                                    logger.error("[BOT] Failed to delete expired ticket thread: " + threadId, error);
                                    // Still try to clean up storage
                                    try {
                                        storage.removeTicket(threadId);
                                    } catch (IOException e) {
                                        logger.error("[BOT] Failed to remove ticket from storage: " + threadId, e);
                                    }
                                }
                            );
                        } else {
                            // Thread doesn't exist anymore, just remove from storage
                            try {
                                storage.removeTicket(threadId);
                                logger.info("[BOT] Removed non-existent ticket from storage: " + info.ticketName);
                            } catch (IOException e) {
                                logger.error("[BOT] Failed to remove ticket from storage: " + threadId, e);
                            }
                        }
                    }
                } else {
                    // Schedule deletion for remaining time
                    ScheduledFuture<?> deletionTask = scheduler.schedule(() -> {
                        logger.info("[BOT] Deleting inactive ticket thread: " + info.ticketName + " (" + threadId + ")");
                        Guild guild = api.getGuildById(info.guildId);
                        if (guild != null) {
                            ThreadChannel thread = guild.getThreadChannelById(threadId);
                            if (thread != null) {
                                // Get user info for logging
                                User ticketUser = api.getUserById(info.userId);
                                String userName = ticketUser != null ? ticketUser.getName() : "Unknown User";

                                thread.delete().queue(
                                    success -> {
                                        logger.info("[BOT] Successfully deleted inactive ticket thread: " + info.ticketName);

                                        // Log to channel
                                        MessageEmbed logEmbed = new EmbedBuilder()
                                                .setColor(Color.ORANGE)
                                                .setTitle("⏰ Ticket Expired")
                                                .addField("Ticket", info.ticketName, true)
                                                .addField("User", userName, true)
                                                .addField("Reason", "No response within 24 hours", false)
                                                .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                                                .build();

                                        MessageHandler.logToChannel(guild, logEmbed);

                                        try {
                                            storage.removeTicket(threadId);
                                        } catch (IOException e) {
                                            logger.error("[BOT] Failed to remove ticket from storage: " + threadId, e);
                                        }
                                    },
                                    error -> {
                                        logger.error("[BOT] Failed to delete inactive ticket thread: " + threadId, error);
                                        // Still try to clean up storage
                                        try {
                                            storage.removeTicket(threadId);
                                        } catch (IOException e) {
                                            logger.error("[BOT] Failed to remove ticket from storage: " + threadId, e);
                                        }
                                    }
                                );
                            } else {
                                // Thread doesn't exist, just clean up storage
                                try {
                                    storage.removeTicket(threadId);
                                    logger.info("[BOT] Cleaned up non-existent ticket from storage: " + info.ticketName);
                                } catch (IOException e) {
                                    logger.error("[BOT] Failed to remove ticket from storage: " + threadId, e);
                                }
                            }
                        }
                        pendingTickets.remove(threadId);
                    }, timeRemaining, TimeUnit.MILLISECONDS);

                    pendingTickets.put(threadId, deletionTask);
                    logger.debug("[BOT - Debug] Restored deletion task for ticket thread: " + info.ticketName + " (remaining: " + timeRemaining + "ms)");
                }
            }

            logger.info("[BOT] Restored " + pendingTickets.size() + " pending tickets");
        } catch (Exception e) {
            logger.error("[BOT] Error restoring pending tickets", e);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("createticketpanel")) {
            event.deferReply(true).queue();
            try {
                String ownerId = Config.get().getBot().getOwner_id();
                if (!event.getUser().getId().equals(ownerId)) {
                    event.reply("❌ You are not authorized to use this command.").setEphemeral(true).queue();
                    return;
                }
            } catch (IOException e) {
                return;
            }

            String channelId = event.getOption("channel").getAsChannel().getId();
            GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelId);

            if (guildChannel == null) {
                logger.error("[BOT] Channel not found: " + channelId);
                return;
            }

            MessageEmbed success = new EmbedBuilder()
                    .setColor(HexFormat.fromHexDigits("2073cb"))
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .setTitle("✅ Ticket Panel created")
                    .setDescription("The ticket panel has been created in " + event.getOption("channel").getAsChannel().getJumpUrl() + ".")
                    .build();

            MessageEmbed prompt = new EmbedBuilder()
                    .setColor(HexFormat.fromHexDigits("2073cb"))
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .setTitle("Create a Support Ticket")
                    .setDescription("Click on the button below to create a support ticket for our Moderators to help you out.")
                    .build();

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
                    MessageEmbed fail = new EmbedBuilder()
                            .setColor(HexFormat.fromHexDigits("2073cb"))
                            .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                            .setTitle("❌ Ticket Panel creation failed")
                            .setDescription("Channel is not a text or announcement channel: " + channelId)
                            .build();
                    event.getHook().sendMessageEmbeds(fail).setEphemeral(true).queue();
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("create_ticket:")) return;

        logger.debug("[BOT - Debug] Button pressed: " + event.getComponentId());
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
            logger.error("[BOT] Failed to generate ticket name", e);
            event.reply("❌ Failed to create ticket. Please try again later.").setEphemeral(true).queue();
            return;
        }

        logger.debug("[BOT - Debug] Creating Support thread with name: " + ticketName);
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
                logger.error("[BOT] Error while getting ping roles", e);
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
                logger.debug("[BOT - Debug] Saved ticket to storage: " + ticketName + " (" + threadChannel.getId() + ")");
            } catch (IOException e) {
                logger.error("[BOT] Failed to save ticket to storage: " + threadChannel.getId(), e);
            }

            ScheduledFuture<?> deletionTask = scheduler.schedule(() -> {
                logger.info("[BOT] Deleting inactive ticket thread: " + ticketName + " (" + threadChannel.getId() + ")");

                // Get user info for logging before deletion
                String userName = member.getUser().getName();

                threadChannel.delete().queue(
                    success -> {
                        logger.info("[BOT] Successfully deleted inactive ticket thread: " + ticketName);

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
                            logger.error("[BOT] Failed to remove ticket from storage: " + threadChannel.getId(), e);
                        }
                    },
                    error -> {
                        logger.error("[BOT] Failed to delete inactive ticket thread: " + threadChannel.getId(), error);
                        // Still try to clean up storage
                        try {
                            storage.removeTicket(threadChannel.getId());
                        } catch (IOException e) {
                            logger.error("[BOT] Failed to remove ticket from storage: " + threadChannel.getId(), e);
                        }
                    }
                );
                pendingTickets.remove(threadChannel.getId());
            }, 24, TimeUnit.HOURS);
            
            // Store the deletion task
            pendingTickets.put(threadChannel.getId(), deletionTask);
            logger.debug("[BOT - Debug] Scheduled deletion for ticket thread: " + ticketName);
        });
    }
    
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
                    logger.debug("[BOT - Debug] Cancelled deletion task for ticket thread: " + threadId);
                }
                
                // Mark ticket as responded in storage
                try {
                    storage.markTicketResponded(threadId);
                } catch (IOException e) {
                    logger.error("[BOT] Failed to mark ticket as responded: " + threadId, e);
                }
                
                String ticketName = storage.getTicketName(threadId);
                logger.debug("[BOT - Debug] User responded to ticket thread: " + ticketName + ", cancelling deletion");
                
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
                    logger.error("[BOT] Error while getting ping roles", e);
                }
                
                threadChannel.sendMessage(ping.toString()).queue();
                threadChannel.sendMessageEmbeds(thankYou).queue();
            }
        }
    }
}
