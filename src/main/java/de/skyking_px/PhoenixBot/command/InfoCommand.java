package de.skyking_px.PhoenixBot.command;

import de.skyking_px.PhoenixBot.Bot;
import de.skyking_px.PhoenixBot.util.MessageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

public class InfoCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        if (event.getName().equals("info")) {
            Instant currentTime = Instant.now();
            String uptime = Duration.between(Bot.START_TIME, currentTime).get(ChronoUnit.DAYS) + "Days " +
                            Duration.between(Bot.START_TIME, currentTime).get(ChronoUnit.HOURS) + "Hours " +
                            Duration.between(Bot.START_TIME, currentTime).get(ChronoUnit.MINUTES) + "Minutes " +
                            Duration.between(Bot.START_TIME, currentTime).get(ChronoUnit.SECONDS) + "Seconds";
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(HexFormat.fromHexDigits("2073cb"))
                    .setTitle("Phoenix Bot")
                    .setThumbnail("https://cdn.discordapp.com/avatars/1347561107744882781/129c785aad035070d8d19d4addc258eb.webp?size=1024")
                    .addField("General Information", "Bot Version: `" + getClass().getPackage().getImplementationVersion() + "`\nUptime: " + uptime + "\nFollows Lost World Modpack Version: `3.0.0+`", false)
                    .setFooter("Phoenix Bot | Developed by SkyKing_PX")
                    .build();
            MessageHandler.sendPreparedMessage(event, embed);
        }
    }

    public static CommandData getInfoCommand() {
        return Commands.slash("info", "Shows some useful information about the bot");
    }
}