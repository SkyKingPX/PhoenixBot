package de.skyking_px.PhoenixBot.command;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.Config;
import de.skyking_px.PhoenixBot.util.CloseHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CloseCommand extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("close")) return;

        if (!event.isFromGuild() || !event.getChannel().getType().isThread()) {
            event.reply("❌ This command can only be used inside a forum thread.").setEphemeral(true).queue();
            return;
        }

        try {
            String supportForumId = Config.get().getSupport().getSupport_forum_id();
            String bugReportForumId = Config.get().getBugReport().getBugReport_forum_id();
            String suggestionForumId = Config.get().getVoting().getSuggestions_forum_id();
            String parentId = event.getChannel().asThreadChannel().getParentChannel().getId();

            if (!parentId.equals(supportForumId) && !parentId.equals(bugReportForumId) && !parentId.equals(suggestionForumId)) {
                event.reply("❌ This command can only be used inside selected forum threads.").setEphemeral(true).queue();
                return;
            }
        } catch (IOException e) {
            logger.error("[BOT] Error while executing /close", e);
            event.reply("❌ Config error. Please try again later.").setEphemeral(true).queue();
            return;
        }

        ThreadChannel thread = event.getChannel().asThreadChannel();
        Member invoker = event.getMember();
        Guild guild = event.getGuild();

        // Defer the reply to show confirmation buttons (handled inside CloseHandler)
        event.deferReply(true).queue(hook -> CloseHandler.sendConfirmation(thread, invoker, guild, hook));
    }
}