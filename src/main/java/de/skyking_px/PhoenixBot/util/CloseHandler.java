package de.skyking_px.PhoenixBot.util;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.storage.VoteStorage;
import de.skyking_px.PhoenixBot.util.MessageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CloseHandler extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private static final VoteStorage storage = Bot.getVoteStorage();

    public static void sendConfirmation(ThreadChannel thread, Member invoker, Guild guild, InteractionHook hook) {
        MessageEmbed confirmation = new EmbedBuilder()
                .setColor(Color.RED)
                .addField("Are you sure you want to close this post?",
                        "You won't be able to contact anyone through this post anymore and it will be archived permanently.",
                        false)
                .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                .build();

        Button confirmButton = Button.success("close_confirm:" + thread.getId(), "✅ Confirm");
        Button cancelButton = Button.danger("close_cancel:" + thread.getId(), "❌ Cancel");

        hook.sendMessageEmbeds(confirmation)
                .addActionRow(confirmButton, cancelButton)
                .setEphemeral(true)
                .queue();
    }

    public static void closeThread(ThreadChannel thread, Member invoker, Guild guild, Consumer<MessageEmbed> reply) throws IOException {
        ForumChannel parent = thread.getParentChannel().asForumChannel();

        String threadOwnerId = thread.getOwnerId();
        String userId = invoker.getId();
        String modRoleId;
        try {
            modRoleId = Config.get().getRoles().getModerator();
        } catch (Exception e) {
            logger.error("Error loading mod role", e);
            reply.accept(new EmbedBuilder()
                    .setDescription("❌ Config error")
                    .setColor(Color.RED)
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .build());
            return;
        }

        boolean isOwner = userId.equals(threadOwnerId);
        boolean isModerator = invoker.getRoles().stream().anyMatch(r -> r.getId().equals(modRoleId));

        if (!isOwner && !isModerator) {
            reply.accept(new EmbedBuilder()
                    .setDescription("❌ Only the post creator or a moderator can close this post.")
                    .setColor(Color.RED)
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .build());
            return;
        }

        ForumTag closedTag = parent.getAvailableTags().stream()
                .filter(tag -> tag.getName().toLowerCase().contains("closed"))
                .findFirst().orElse(null);

        if (closedTag == null) {
            reply.accept(new EmbedBuilder()
                    .setDescription("❌ Could not find a 'Closed' tag")
                    .setColor(Color.RED)
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .build());
            return;
        }

        List<ForumTag> updatedTags = new ArrayList<>(thread.getAppliedTags());
        if (!updatedTags.contains(closedTag)) {
            updatedTags.add(closedTag);
        }

        thread.getManager()
                .setAppliedTags(updatedTags)
                .setLocked(true)
                .setArchived(true)
                .queue(
                        success -> {
                            MessageEmbed successEmbed = new EmbedBuilder()
                                    .setColor(Color.GREEN)
                                    .addField("✅ Post Closed", "This post has been successfully closed", false)
                                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                                    .build();

                            reply.accept(successEmbed);

                            MessageEmbed logEmbed = new EmbedBuilder()
                                    .setColor(Color.GREEN)
                                    .addField("✅ Post Closed",
                                            "Name: " + thread.getName() +
                                                    "\nLink: " + thread.getJumpUrl() +
                                                    "\nClosed by: " + invoker.getUser().getName(),
                                            false)
                                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                                    .build();

                            MessageHandler.logToChannel(guild, logEmbed);
                            try {
                                String threadId = thread.getId();
                                storage.removeAllVotes(threadId);
                            } catch (IOException e) {
                                logger.error("[BOT] Error removing votes from thread \"" + thread.getName() + "\"", e);
                            }
                        },
                        failure -> {
                            MessageEmbed failureEmbed = new EmbedBuilder()
                                    .setDescription("❌ Failed to close the post.")
                                    .setColor(Color.RED)
                                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                                    .build();

                            reply.accept(failureEmbed);
                        }
                );
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("close_confirm:") && !event.getComponentId().startsWith("close_cancel:")) return;

        String id = event.getComponentId();
        String threadId = id.split(":")[1];
        Guild guild = event.getGuild();
        Member member = event.getMember();

        if (guild == null || member == null) {
            event.reply("❌ Guild or Member not found.").setEphemeral(true).queue();
            return;
        }

        ThreadChannel thread = guild.getThreadChannelById(threadId);
        if (thread == null) {
            event.reply("❌ Could not find the thread.").setEphemeral(true).queue();
            return;
        }

        if (id.startsWith("close_confirm:")) {
            event.deferEdit().queue();

            try {
                closeThread(thread, member, guild, embed -> {
                    // Nachricht mit dem Ergebnis editieren
                    event.getHook().editOriginalEmbeds(embed).setComponents().queue();
                });
            } catch (IOException e) {
                event.getHook().editOriginal("❌ Error closing the thread.").queue();
            }
        } else if (id.startsWith("close_cancel:")) {
            // Defer edit and then edit the message
            event.deferEdit().queue();

            event.getHook().editOriginalEmbeds(new EmbedBuilder()
                            .setColor(Color.YELLOW)
                            .setDescription("❌ Post closure cancelled.")
                            .build())
                    .setComponents()
                    .queue();
        }
    }
}