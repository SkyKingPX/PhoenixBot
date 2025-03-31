package de.skyking_px.PhoenixBot;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildJoinListener extends ListenerAdapter {
    private static final long ALLOWED_SERVER_ID = 1116745011837534239L;

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (guildId != ALLOWED_SERVER_ID) {
            event.getGuild().leave().queue();
            System.out.println("Bot left Server named '" + event.getGuild().getName() + "' created by '"
                    + event.getGuild().getOwner() + "' because it was not allowed to join it. Member Count: " + event.getGuild().getMembers().size());
        }
    }
}
