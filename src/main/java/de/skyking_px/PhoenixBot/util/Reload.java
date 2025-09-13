package de.skyking_px.PhoenixBot.util;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reload extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("reloadconfig")) return;

        try {
            String ownerId = Config.get().getBot().getOwner_id();
            if (!event.getUser().getId().equals(ownerId)) {
                event.reply("❌ You are not authorized to use this command.").setEphemeral(true).queue();
                return;
            }

            event.deferReply(true).queue(hook -> {
                try {
                    Config.reload();
                    hook.editOriginal("✅ Config reloaded successfully!").queue();
                } catch (Exception e) {
                    hook.editOriginal("❌ Failed to reload config: `" + e.getMessage() + "`").queue();
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            event.reply("❌ Failed to access config: `" + e.getMessage() + "`").setEphemeral(true).queue();
            e.printStackTrace();
        }
    }
}
