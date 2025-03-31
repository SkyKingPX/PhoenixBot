package de.skyking_px.PhoenixBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.HexFormat;

public class TBSCommand extends ListenerAdapter {
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
                case "install-standalone":
                    time = LocalDateTime.of(2025, 3, 27, 18, 45).atZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1)));
                    embed = new EmbedBuilder()
                            .setColor(HexFormat.fromHexDigits("2073cb"))
                            .addField("Standalone Installation", "1. Download the preferred Jar file from <#1347095790077743205>\n2. [Download GeckoLib from CurseForge](<https://www.curseforge.com/minecraft/mc-mods/geckolib/files/all?page=1&pageSize=20&version=1.20.1&gameVersionTypeId=1>)\n3. Create a new **Forge 1.20.1** Instance\n4. Put **both** Jars inside the mods folder\n5. Start the Instance\n ", false)
                            .setFooter("Last Edited")
                            .setTimestamp(time)
                            .build();
                    break;
                case "install-unsafe":
                    time = LocalDateTime.of(2025, 3, 27, 18, 45).atZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1)));
                    embed = new EmbedBuilder()
                            .setTitle("Installing the Unsafe Version of TBS")
                            .setColor(HexFormat.fromHexDigits("2073cb"))
                            .addField("Modpack Installation", "1. Make sure that the Modpack is downloaded and you know it’s location (right-click on instance and press on „Open Folder“)\n2. Download a version from <#1347095790077743205> \n 3. Find a **„[SAFE] TBS <version>.jar“** inside the Mods folder (inside the file explorer of your OS) and **delete it**\n 4. Move the downloaded .jar **inside the mods** folder\n 5. Start the Modpack\n ", false)
                            .addField("Video Tutoral", "Kalarian has made a tutorial on how to replace the Safe TBS jar with the Unsafe in the Modpack:\n<https://youtu.be/GRkNE4P-es4>\n ", false)
                            .setFooter("Last Edited")
                            .setTimestamp(time)
                            .build();
                    break;
                case "not-whitelisted":
                    time = LocalDateTime.of(2025, 3, 27, 18, 45).atZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1)));
                    embed = new EmbedBuilder()
                            .setColor(HexFormat.fromHexDigits("2073cb"))
                            .addField("I am getting kicked because I am not \"whitelisted\"! Help!", "A member of this discord Server (perpetuum) found out how you can temporarily fix this.\n ", false)
                            .addField("Prerequisites", "* [NBTExplorer](<https://sourceforge.net/projects/nbtexplorer.mirror/>) or any other Minecraft NBT Editor.\n ", false)
                            .addField("Fix", "1. find your World folder (located in `/modpack/saves/`)\n2. Navigate to `/<your-world-name>/data/`\n3. Make sure to **save and close your world**!\n4. Open the `thebrokenscript_mapvars.dat` file with a NBT Editor\n5. Change the `cancorrupted` Value to `0`\n6. Save the file by pressing on the Save icon\n7. Enter your world again\n ", false)
                            .setImage("https://media.discordapp.net/attachments/1119148633409986562/1348040753577988286/nbtexplorer.png?ex=67ce0449&is=67ccb2c9&hm=1fa34185b4eb448aaacf74939270110c70cf55269813f83406b7d065fcd9c754&=&format=webp&quality=lossless&width=419&height=289")
                            .setFooter("Last Edited")
                            .setTimestamp(time)
                            .build();
                    break;
                case "tbs-multiplayer":
                    time = LocalDateTime.of(2025, 3, 27, 18, 50).atZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1)));
                    embed = new EmbedBuilder()
                            .setColor(HexFormat.fromHexDigits("2073cb"))
                            .addField("Does TBS support Multiplayer?", "No, at least not officially. I will still provide Server packs but you need to expect unexpected crashes, broken events and lag.\nKjuMe, a member of this Server, has made a Server patch that prevents servers from crashing often.", false)
                            .setFooter("Last Edited")
                            .setTimestamp(time)
                            .build();
                    break;
                case "devmode-nbt":
                    time = LocalDateTime.of(2025, 3, 27, 18, 55).atZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1)));
                    embed = new EmbedBuilder()
                            .setTitle("Enable the Devmode of TBS without knowing the Code")
                            .setColor(HexFormat.fromHexDigits("2073cb"))
                            .addField("Prerequisites", "* [NBTExplorer](<https://sourceforge.net/projects/nbtexplorer.mirror/>) or any other Minecraft NBT Editor.\n ", false)
                            .addField("Fix", "1. find your World folder (located in `/modpack/saves/`)\n2. Navigate to `/<your-world-name>/data/`\n3. Make sure to **save and close your world**!\n4. Open the `thebrokenscript_mapvars.dat` file with a NBT Editor\n5. Change the `devmodeenabled` Value to `1`\n6. Save the file by pressing on the Save icon\n7. Enter your world again\n ", false)
                            .setImage("https://media.discordapp.net/attachments/1119148633409986562/1354876181194866741/image.png?ex=67e6e245&is=67e590c5&hm=2447ad30d955ef852aa9686310149e92ac7505a87b04f1acf63eb919c2fa63fc&=&format=webp&quality=lossless&width=363&height=274")
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
        return Commands.slash("tbs", "All commands related to \"The Broken Script\" and the \"Lost World\" Modpack")
                .addOptions(
                        new OptionData(OptionType.STRING, "argument", "The argument of your prompt", true)
                                .addChoices(
                                        new Command.Choice("Difference between the [UNSAFE] and [SAFE] Version", "unsafe"),
                                        new Command.Choice("Install TBS outside of the Modpack", "install-standalone"),
                                        new Command.Choice("Install the Unsafe Version of TBS inside the Modpack", "install-unsafe"),
                                        new Command.Choice("Fix the error message \"You are not whitelisted\"", "not-whitelisted"),
                                        new Command.Choice("Multiplayer compatibility with TBS", "tbs-multiplayer"),
                                        new Command.Choice("Enable Devmode by editing NBT Data", "devmode-nbt")
                                ),
                        new OptionData(OptionType.USER, "user", "Optionally choose if you want to ping a member", false)
                );
    }
}

