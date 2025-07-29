package de.skyking_px.PhoenixBot.listener;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.util.CloseHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

public class BugReportListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        ThreadChannel thread = event.getChannel().asThreadChannel();
        ForumChannel parent = thread.getParentChannel().asForumChannel();
        try {
            if (!parent.getId().equals(Config.get().getBugReport().getBugReport_forum_id())) return;
        } catch (IOException e) {
            logger.error("[BOT] Fatal Error - Couldn't get Bug Report Forum ID");
        }

        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .addField("Is your Issue resolved?", "When your issue is resolved, please press on the `Close` Button below.", false)
                .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                .build();

        event.getChannel().asThreadChannel().sendMessageEmbeds(embed)
                .addActionRow(
                        Button.success("bugReport:close:" + event.getChannel().getId(), "Close").withEmoji(Emoji.fromUnicode("✅"))
                ).queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("bugReport:close:")) return;
        if (!event.isFromGuild() || !event.getChannel().getType().isThread()) {
            event.reply("❌ This button only works inside selected forum threads.").setEphemeral(true).queue();
            return;
        }

        ThreadChannel thread = event.getChannel().asThreadChannel();
        Member invoker = event.getMember();
        Guild guild = event.getGuild();

        event.deferReply(true).queue(hook -> CloseHandler.sendConfirmation(thread, invoker, guild, hook));
    }
}
