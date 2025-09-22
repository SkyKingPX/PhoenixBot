package de.skyking_px.PhoenixBot.command;

import de.skyking_px.PhoenixBot.util.EmbedUtils;
import de.skyking_px.PhoenixBot.util.MessageHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Slash command that directs users to the FAQ channel.
 * 
 * @author SkyKing_PX
 */
public class FAQCommand extends ListenerAdapter {
    /**
     * Handles the /faq slash command.
     * Sends an embed with a link to the FAQ channel.
     * 
     * @param event The slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("faq")) return;
            event.deferReply().queue();
            MessageEmbed embed = EmbedUtils.createDefault()
                    .addField("Frequently Asked Questions", "You can find the FAQ here: <#1358722002835341423>\nIt contains much information that you should read before asking for help.", false)
                    .build();
            MessageHandler.sendPreparedMessage(event, embed);
    }
}