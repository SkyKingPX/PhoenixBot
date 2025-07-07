package de.skyking_px.PhoenixBot.util;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class MessageHandler {
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
}
