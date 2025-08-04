package de.skyking_px.PhoenixBot.ticket;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HexFormat;

public class Panel extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("createticketpanel")) {
            event.deferReply(true).queue();
            try {
                String ownerId = Config.get().getBot().getOwner_id();
                if (!event.getUser().getId().equals(ownerId)) {
                    event.reply("❌ You are not authorized to use this command.").setEphemeral(true).queue();
                    return;
                }
            } catch (IOException e) {
                return;
            }

            String channelId = event.getOption("channel").getAsChannel().getId();
            GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelId);

            if (guildChannel == null) {
                logger.error("[BOT] Channel not found: " + channelId);
                return;
            }

            MessageEmbed success = new EmbedBuilder()
                    .setColor(HexFormat.fromHexDigits("2073cb"))
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .setTitle("✅ Ticket Panel created")
                    .setDescription("The ticket panel has been created in" + event.getOption("channel").getAsChannel().getJumpUrl() + ".")
                    .build();

            MessageEmbed prompt = new EmbedBuilder()
                    .setColor(HexFormat.fromHexDigits("2073cb"))
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .setTitle("Create a Support Ticket")
                    .setDescription("Click on the button below to create a support ticket for our Moderators to help you out.")
                    .build();

            switch (guildChannel.getType()) {
                case TEXT -> {
                    ((TextChannel) guildChannel).sendMessageEmbeds(prompt).addActionRow(
                            Button.danger("create_ticket:" + event.getGuild().getId(), "Create Ticket")
                ).queue();
                    event.getHook().sendMessageEmbeds(success).setEphemeral(true).queue();
                }
                case NEWS -> {
                    ((NewsChannel) guildChannel).sendMessageEmbeds(prompt).addActionRow(
                            Button.danger("create_ticket:" + event.getGuild().getId(), "Create Ticket")
                ).queue();
                    event.getHook().sendMessageEmbeds(success).setEphemeral(true).queue();
                }
                default -> {
                    MessageEmbed fail = new EmbedBuilder()
                            .setColor(HexFormat.fromHexDigits("2073cb"))
                            .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                            .setTitle("❌ Ticket Panel creation failed")
                            .setDescription("Channel is not a text or announcement channel: " + channelId)
                            .build();
                    event.getHook().sendMessageEmbeds(fail).setEphemeral(true).queue();
                }
            }
        }
    }
}
