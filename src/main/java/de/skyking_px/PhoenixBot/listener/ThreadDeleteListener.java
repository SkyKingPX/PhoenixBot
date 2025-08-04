package de.skyking_px.PhoenixBot.listener;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.storage.VoteStorage;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ThreadDeleteListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private static final VoteStorage storage = Bot.getVoteStorage();

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        if (!event.getChannel().getType().isThread()) {
            return;
        }

        try {
            if (event.getChannel().asThreadChannel().getParentChannel().getId().equals(Config.get().getVoting().getSuggestions_forum_id())) {
                String threadID = event.getChannel().asThreadChannel().getId();
                storage.removeAllVotes(threadID);
            }
        } catch (IOException e) {
            logger.error("[BOT] Couldn't remove Votes from Thread \"" + event.getChannel().asThreadChannel().getName() + "\" with ID " + event.getChannel().asThreadChannel().getId(), e);
        }
    }
}
