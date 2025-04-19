package de.skyking_px.PhoenixBot;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class GuildJoinListener extends ListenerAdapter {
    private static final long ALLOWED_SERVER_ID = 1116745011837534239L;
    private static final Logger logger = LoggerFactory.getLogger(GuildJoinListener.class);

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (guildId != ALLOWED_SERVER_ID) {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            event.getGuild().leave().queue();
            logger.info("[BOT] Bot left Server named '{}', created by '{}'. Member Count: {}",
                    event.getGuild().getName(),
                    event.getGuild().getOwner(),
                    event.getGuild().getMembers().size()
            );
        }
    }
}
