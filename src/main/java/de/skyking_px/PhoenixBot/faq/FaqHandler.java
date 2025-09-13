package de.skyking_px.PhoenixBot.faq;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.util.MessageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HexFormat;

public class FaqHandler extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("sendfaq")) {
            event.deferReply(true).queue();
            try {
                String ownerId = Config.get().getBot().getOwner_id();
                if (!event.getUser().getId().equals(ownerId)) {
                    event.reply("❌ You are not authorized to use this command.").setEphemeral(true).queue();
                    return;
                }
            } catch (IOException e) {
                logger.error("[BOT] Error while executing /sendfaq", e);
                return;
            }

            try {
                Config config = Config.get();
                TextChannel faqChannel = event.getGuild().getTextChannelById(config.getFaq().getFaq_channel_id());

                if (faqChannel == null) {
                    logger.info("[BOT] FAQ Channel not found.");
                    event.getHook().sendMessage("❌ FAQ channel not found!").setEphemeral(true).queue();
                    return;
                }

                for (FaqEntry entry : config.getFaq().getFaq_entries()) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(MessageHandler.parseEmojis(event.getJDA(), entry.getQuestion()))
                            .setColor(HexFormat.fromHexDigits("2073cb"));

                    if (entry.getAnswer() != null && !entry.getAnswer().isEmpty()) {
                        embed.setDescription(MessageHandler.parseEmojis(event.getJDA(), entry.getAnswer()));
                    }

                    if (entry.getThumbnailUrl() != null && !entry.getThumbnailUrl().isEmpty()) {
                        embed.setThumbnail(entry.getThumbnailUrl());
                    } else if (entry.getImageUrl() != null && !entry.getImageUrl().isEmpty()) {
                        embed.setImage(entry.getImageUrl());
                    }

                    faqChannel.sendMessageEmbeds(embed.build()).queue();
                }
            } catch (IOException e) {
                logger.error("[BOT] Error while sending FAQ messages.", e);
            }
            event.getHook().sendMessage("✅ FAQ messages sent!").setEphemeral(true).queue();
        }
    }
}

