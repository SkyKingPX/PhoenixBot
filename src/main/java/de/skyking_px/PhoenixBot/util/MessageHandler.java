package de.skyking_px.PhoenixBot.util;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public static void sendPreparedMessage(SlashCommandInteractionEvent event, MessageEmbed embed){
        if (event.getOption("user") != null) {

            User user = event.getOption("user").getAsUser();
            String mention = user.getAsMention();

            event.getHook().sendMessage(mention)
                    .addEmbeds(embed)
                    .queue();
        } else {
            event.getHook().sendMessageEmbeds(embed).queue();
        }
    }

    public static void logToChannel(Guild guild, String message) {
        TextChannel logChannel = null;
        try {
            logChannel = guild.getTextChannelById(Config.get().getLogging().getChannel_id());
        } catch (IOException e) {
            logger.error("[BOT] Error sending log message");
        }
        if (logChannel != null) {
            logChannel.sendMessage(message).queue();
        }
    }

    public static void logToChannel(Guild guild, MessageEmbed message) {
        TextChannel logChannel = null;
        try {
            logChannel = guild.getTextChannelById(Config.get().getLogging().getChannel_id());
        } catch (IOException e) {
            logger.error("[BOT] Error sending log message");
        }
        if (logChannel != null) {
            logChannel.sendMessageEmbeds(message).queue();
        }
    }
}
