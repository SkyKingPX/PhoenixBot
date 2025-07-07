package de.skyking_px.PhoenixBot.command;

import de.skyking_px.PhoenixBot.util.MessageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.HexFormat;

public class FAQCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        if (event.getName().equals("faq")) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(HexFormat.fromHexDigits("2073cb"))
                    .addField("Frequently Asked Questions", "You can find the FAQ here: <#1358722002835341423>\nIt contains much information that you should read before asking for help.", false)
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .build();
            MessageHandler.sendPreparedMessage(event, embed);
        }
    }

    public static CommandData getFAQCommand() {
        return Commands.slash("faq", "Suggests a user to read the FAQ")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "Optionally choose if you want to ping a member", false)
                );
    }
}