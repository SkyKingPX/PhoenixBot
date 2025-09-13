package de.skyking_px.PhoenixBot.listener;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.storage.VoteStorage;
import de.skyking_px.PhoenixBot.util.MessageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SuggestionListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final VoteStorage storage;

    public SuggestionListener(VoteStorage storage) throws IOException {
        this.storage = storage;
        Map<String, int[]> votes = storage.loadAllVotes();
        votes.forEach((id, pair) -> {
            yesVotes.put(id, pair[0]);
            noVotes.put(id, pair[1]);
        });
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        try {
            ThreadChannel thread = event.getChannel().asThreadChannel();
            ForumChannel parent = thread.getParentChannel().asForumChannel();
            if (!parent.getId().equals(Config.get().getVoting().getSuggestions_forum_id())) return;
        } catch (IOException e) {
            logger.error("[BOT] Fatal Error - Couldn't get Suggestions Forum ID");
        } catch (IllegalStateException e) {
            logger.error("[BOT] Error - Incorrect channel type, ignoring");
        }

        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .addField("Vote for this Feature", "You are able to vote either **for** or **against** this feature.\nCast your vote below!", false)
                .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                .build();

        event.getChannel().asThreadChannel().sendMessageEmbeds(embed)
                .addActionRow(
                        Button.success("vote:up:" + event.getChannel().getId(), "Upvote").withEmoji(Emoji.fromUnicode("👍")),
                        Button.danger("vote:down:" + event.getChannel().getId(), "Downvote").withEmoji(Emoji.fromUnicode("👎"))
                ).queue();
    }

    private final Map<String, Integer> yesVotes = new HashMap<>();
    private final Map<String, Integer> noVotes = new HashMap<>();
    private final String logChannelId = Config.get().getLogging().getChannel_id();

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("vote:")) return;

        // Check if thread is locked and archived
        if (event.getChannel().asThreadChannel().isLocked() && event.getChannel().asThreadChannel().isArchived()) {
            User user = event.getUser();
            user.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage("**You tried to vote on an already closed post in** `" + event.getGuild().getName() + "`\nThe vote has not been registered."))
                    .queue();
            return;
        }

        String threadID = event.getChannel().asThreadChannel().getId();
        String userId = event.getUser().getId();
        boolean isUpvote = event.getComponentId().startsWith("vote:up:");

        int yes = yesVotes.getOrDefault(threadID, 0);
        int no = noVotes.getOrDefault(threadID, 0);

        String previousVote = storage.getUserVote(threadID, userId);

        try {
            // Defer reply for ephemeral feedback
            event.deferReply(true).queue(); // true = ephemeral

            if (previousVote == null) {
                if (isUpvote) yes++;
                else no++;
                yesVotes.put(threadID, yes);
                noVotes.put(threadID, no);

                storage.setVoteCount(threadID, yes, no);
                storage.saveUserVote(threadID, userId, isUpvote ? "up" : "down");

                MessageEmbed embed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .addField("Vote Added", "**Vote added** by <@" + userId + "> to post " + event.getChannel().asThreadChannel().getJumpUrl() + " - 👍 " + yes + " | 👎 " + no, false)
                        .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                        .build();
                MessageHandler.logToChannel(event.getGuild(), embed);

                // Send ephemeral confirmation
                event.getHook().sendMessage("✅ **Your vote has been registered, thank you!**").queue();

            } else if (!previousVote.equals(isUpvote ? "up" : "down")) {
                if (isUpvote) {
                    yes++;
                    no--;
                } else {
                    no++;
                    yes--;
                }
                yesVotes.put(threadID, yes);
                noVotes.put(threadID, no);

                storage.setVoteCount(threadID, yes, no);
                storage.saveUserVote(threadID, userId, isUpvote ? "up" : "down");

                MessageEmbed embed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .addField("Vote Updated", "**Vote updated** by <@" + userId + "> in post " + event.getChannel().asThreadChannel().getJumpUrl() + " - 👍 " + yes + " | 👎 " + no, false)
                        .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                        .build();
                MessageHandler.logToChannel(event.getGuild(), embed);

                event.getHook().sendMessage("✅ **Your vote has been changed.**").queue();

            } else {
                event.getHook().sendMessage("❌ **You already voted this way.**").queue();
                return;
            }

            // Jetzt die ursprüngliche Nachricht mit neuem Vote-Status editieren
            MessageEmbed edited = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .addField("Vote for this Feature", "You are able to vote either **for** or **against** this feature.\nCast your vote below!", false)
                    .addField("Current Vote Count", "👍 Upvotes: **" + yes + "**\n👎 Downvotes: **" + no + "**", false)
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .build();

            event.getMessage().editMessageEmbeds(edited)
                    .setActionRow(
                            Button.success("vote:up:" + event.getChannel().getId(), "Upvote").withEmoji(Emoji.fromUnicode("👍")),
                            Button.danger("vote:down:" + event.getChannel().getId(), "Downvote").withEmoji(Emoji.fromUnicode("👎"))
                    )
                    .queue();

        } catch (Exception e) {
            logger.error("Failed to process vote", e);
            event.getHook().sendMessage("❌ An error occurred while processing your vote.").queue();
        }
    }
}