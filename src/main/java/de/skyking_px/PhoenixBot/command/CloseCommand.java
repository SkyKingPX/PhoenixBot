package de.skyking_px.PhoenixBot.command;

import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.util.CloseHandler;
import de.skyking_px.PhoenixBot.util.EmbedUtils;
import de.skyking_px.PhoenixBot.util.LogUtils;
import de.skyking_px.PhoenixBot.util.TicketCloseHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Slash command for closing forum threads and tickets.
 * Validates thread permissions and delegates to the appropriate handler based on thread type.
 *
 * @author SkyKing_PX
 */
public class CloseCommand extends ListenerAdapter {

    /**
     * Checks if a thread is a forum post (from a ForumChannel).
     *
     * @param thread The thread to check
     * @return true if the thread is from a forum channel, false if it's a regular thread
     */
    private static boolean isForumPost(ThreadChannel thread) {
        try {
            return thread.getParentChannel().asForumChannel() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Handles the /close slash command.
     * Validates that the command is used in an appropriate thread/post from a whitelisted channel,
     * then delegates to the appropriate handler (CloseHandler for forum posts, or TicketCloseHandler for regular threads).
     *
     * @param event The slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("close")) return;

        if (!event.isFromGuild() || !event.getChannel().getType().isThread()) {
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ This command can only be used inside a thread or forum post."))
                    .setEphemeral(true).queue();
            return;
        }

        try {
            String parentId = event.getChannel().asThreadChannel().getParentChannel().getId();
            String[] whitelistedChannels = Config.get().getCloseCommand().getWhitelisted_channels();

            boolean isWhitelisted = false;
            for (String channelId : whitelistedChannels) {
                if (parentId.equals(channelId)) {
                    isWhitelisted = true;
                    break;
                }
            }

            if (!isWhitelisted) {
                event.replyEmbeds(EmbedUtils.createSimpleError("❌ This command can only be used in whitelisted channels."))
                        .setEphemeral(true).queue();
                return;
            }
        } catch (IOException e) {
            LogUtils.logException("Error while executing /close command", e);
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ Config error. Please try again later."))
                    .setEphemeral(true).queue();
            return;
        }

        ThreadChannel thread = event.getChannel().asThreadChannel();
        Member invoker = event.getMember();
        Guild guild = event.getGuild();

        // Route to appropriate handler based on thread type
        event.deferReply(true).queue(hook -> {
            if (isForumPost(thread)) {
                // Handle as forum post
                CloseHandler.sendConfirmation(thread, invoker, guild, hook);
            } else {
                // Handle as ticket
                TicketCloseHandler.sendTicketCloseConfirmation(thread, invoker, guild, hook);
            }
        });
    }
}