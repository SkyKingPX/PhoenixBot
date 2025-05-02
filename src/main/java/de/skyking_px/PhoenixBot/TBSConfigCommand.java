package de.skyking_px.PhoenixBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.HexFormat;

public class TBSConfigCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        if (event.getName().equals("tbs")) {
            String argument = event.getOption("argument").getAsString();

            TemporalAccessor time;
            MessageEmbed embed = null;
            switch (argument) {
                case "unsafe":
                    time = LocalDateTime.of(2025, 3, 27, 18, 45).atZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1)));
                    embed = new EmbedBuilder()
                            .setTitle("Difference of TBS Versions")
                            .setColor(HexFormat.fromHexDigits("2073cb"))
                            .addField("[SAFE] Version", "Everything except the PC Shutdown Event. For Example:\n* Game crashes\n* World corruption\n* Harmless TXT Files on Desktop\n ", false)
                            .addField("[UNSAFE] Version", "Everything from above **+** the PC Shutdown Event.\n(Not recommended to use when recording/streaming)\n ", false)
                            .addField("Download", "You can download both versions from here: <#1347095790077743205>\n ", false)
                            .setFooter("Last Edited")
                            .setTimestamp(time)
                            .build();
                    break;
                default:
                    embed = new EmbedBuilder()
                            .setColor(HexFormat.fromHexDigits("EE080A"))
                            .addField("Error", "❌ **An Error occurred while trying to run this command.**\nPlease try again later.", false)
                            .build();
                    break;
            }

            sendPreparedMessage(event, embed);
        }
    }

    public void sendPreparedMessage(SlashCommandInteractionEvent event, MessageEmbed embed){
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

    public static CommandData getTBSCommand() {
        return Commands.slash("tbs-config", "All Config Values of TBS explained")
                .addOptions(
                        new OptionData(OptionType.STRING, "argument", "The argument of your prompt", true)
                                .addChoices(
                                        new Command.Choice("", "")
                                ),
                        new OptionData(OptionType.USER, "user", "Optionally choose if you want to ping a member", false)
                );
    }
}

