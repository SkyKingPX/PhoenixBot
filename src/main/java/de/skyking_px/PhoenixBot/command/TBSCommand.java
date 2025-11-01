package de.skyking_px.PhoenixBot.command;

import de.skyking_px.PhoenixBot.util.EmbedUtils;
import de.skyking_px.PhoenixBot.util.MessageHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Slash command providing help and troubleshooting information for the "The Broken Script" mod in the Lost World Modpack.
 * Handles various issues and questions related to the mod installation and usage.
 * 
 * @author SkyKing_PX
 */
public class TBSCommand extends ListenerAdapter {
    /**
     * Handles the /tbs slash command.
     * Provides detailed help information for various "The Broken Script" mod issues.
     * 
     * @param event The slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("tbs")) return;
        event.deferReply(true).queue();
        String argument = event.getOption("argument").getAsString();

        MessageEmbed embed = null;
        switch (argument) {
            case "install-standalone":
                embed = EmbedUtils.createDefault()
                        .setTitle("Install \"The Broken Script\" outside of the Modpack")
                        .addField("Video Explanation", "Please take a look at this Video explanation: <https://youtu.be/qWPBXK-3o2E?si=Khw1oxkq3XBlX5rc>", false)
                        .build();
                break;
            case "not-whitelisted":
                embed = EmbedUtils.createDefault()
                        .setTitle("Fix the \"You are not whitelisted on this server\" Message")
                        .addField("Prerequisites", "* [NBTExplorer](<https://sourceforge.net/projects/nbtexplorer.mirror/>) or any other Minecraft NBT Editor.\n ", false)
                        .addField("Fix", "1. find your World folder (located in `/modpack/saves/`)\n2. Navigate to `/<your-world-name>/data/`\n3. Make sure to **save and close your world**!\n4. Open the `thebrokenscript_mapvars.dat` file with a NBT Editor\n5. Change the `isNullHere` Value to `0`\n6. Save the file by pressing on the Save icon\n7. Enter your world again\n ", false)
                        .setImage("https://media.discordapp.net/attachments/1353980336626728971/1391829963069718609/isNullHere.png?ex=6871ef6a&is=68709dea&hm=1052f98725850cb7b846491f7284c7c7f749cbfe1bb04ff0b5828ee2d1fb32f4&=&format=webp&quality=lossless&width=994&height=94")
                        .build();
                break;
            case "tbs-multiplayer":
                embed = EmbedUtils.createDefault()
                        .addField("Does TBS support Multiplayer?", "No, at least not officially. I will still provide Server packs but you need to expect unexpected crashes, broken events and lag.\n", false)
                        .addField("Latest Improvements", "The Multiplayer experience seems to get better with every TBS Update, so be sure to always have the latest Version installed!", false)
                        .build();
                break;
            case "enable-cheats":
                embed = EmbedUtils.createDefault()
                        .setTitle("Enable Cheats by editing the Common Config")
                        .addField("Steps", "1. Press `esc` to open the Game Menu\n2. Press on Mods and find `The Broken Script`\n3. Click on `Config` and `Common Config`\n4. Set `Enable Cheats` to `True`\n5. Save your changes and Exit the Menu", false)
                        .setImage("https://media.discordapp.net/attachments/1353980336626728971/1391834015492870237/cheats.png?ex=686d55f0&is=686c0470&hm=13c9968c0d66012510450b66e23383407feec57ff3f077921aea20bf0491ec8d&=&format=webp&quality=lossless&width=2044&height=538")
                        .build();
                break;
            case "no-exit":
                embed = EmbedUtils.createDefault()
                        .setTitle("No way to Exit the Game")
                        .addField("Steps", "If the `Quit Game` Button on the Main Menu is not working, press `alt + f4` to stop the Java Process.", false)
                        .build();
                break;
            case "unsafe":
                embed = EmbedUtils.createDefault()
                        .setTitle("Unsafe Version of \"The Broken Script\"")
                        .addField("Information", "The Unsafe Version of \"The Broken Script\" is a version which can shut your PC off when specific events occur.", false)
                        .addField("Why it was discontinued", "The Unsafe Version of \"The Broken Script\" was officially discontinued due to the fact that the original Developer of TBS was bullied off the interned because many people mistakenly called it malware. Since then those events can no longer shut down a computer.", false)
                        .addField("Why it isn't available here anymore", "If you didn't know, SkyKing_PX, the creator of the Lost World Modpack, kept the Unsafe Version available for download in <#1347095790077743205>.\nDue to the Version Upgrade from `Forge 1.20.1` to `NeoForge 1.21.1`, it got removed here because of too many failed attempts of users trying to install it while dependencies were missing or wrong Versions of the Modloaders were installed.", false)
                        .build();
                break;
            case "common-issues":
                embed = EmbedUtils.createDefault()
                        .setTitle("Common Issues with \"The Broken Script\"")
                        .addField("Game Fails to Load", "Make sure to have those Mods installed as well:\n* [GeckoLib for NeoForge 1.21.1](<https://www.curseforge.com/minecraft/mc-mods/geckolib/files/all?page=1&pageSize=20&version=1.21.1&gameVersionTypeId=6>)\n* [Fzzy Config for NeoForge 1.21.1](<https://www.curseforge.com/minecraft/mc-mods/fzzy-config/files/all?page=1&pageSize=20&version=1.21.1&gameVersionTypeId=6>)\n* [Kotlin for Forge for NeoForge 1.21.1](<https://www.curseforge.com/minecraft/mc-mods/kotlin-for-forge/files/all?page=1&pageSize=20&version=1.21.1&gameVersionTypeId=6>)", false)
                        .addField("Game crashes while Loading (Window Closes)", "1. Make sure your Graphics Drivers are up-to-date\n2. Try to install a newer Java Version ([Adoptium Temurin JDK 21 is recommended](<https://adoptium.net>))", false)
                        .addField("Further Help", "If those methods didn't solve your Problem, please create a Support Post in <#1347289412060582031> or a Ticket in <#1355167810111799599>", false)
                        .build();
                break;
            case "unban-server":
                embed = EmbedUtils.createDefault()
                        .setTitle("Unban / Whitelist every player back on a Server")
                        .addField("Steps", "1. Open the `server_config.toml` file in the `config/thebrokenscript` folder of your Server\n2. Set `disableBanning` to `true`, save the file and restart the server\n3. You should now be able to join the Server again, however a constantly teleporting Null Entity will be following you around. **If this is not the case and you can already play on your server like usual, you are already done and can ignore the following steps.**\n4. To get rid of it, run `/tick freeze` on your server and give yourself a Command Block (`/give @s minecraft:command_block`)\n5. Place the Command Block and paste the following command into it:\n`kill @e[type=thebrokenscript:null_unbeatable_bossfight]`\n6. Set the Command Block to `Repeat` and always active.\n7. Run `/tick unfreeze` and destroy the Command Block after Null disappears.\n\nThis will also prevent further bans from Null.", false)
                        .setImage("https://media.discordapp.net/attachments/1353980336626728971/1397618104133226567/image.png?ex=688260ca&is=68810f4a&hm=3b6e196541357cee1e4d5bfd66f9ece05423e7117e36dceb7fe2d33535dbe71d&=&format=webp&quality=lossless&width=1244&height=604")
                        .build();
                break;
            case "unban-integrated":
                embed = EmbedUtils.createDefault()
                        .setTitle("Unban / Whitelist every player back on an integrated Server")
                        .addField("Steps", "1. Create a new World\n2. Go into the Common Config of TBS (esc > Mods > The Broken Script > Config > Common Config).\n3. Set `disableBanning` to `true`, apply the changes and leave the world.\n3. You should now be able to join the corrupted world again, however a constantly teleporting Null Entity will be following you around. **If this is not the case and you can already play on your world like usual, you are already done and can ignore the following steps.**\n4. To get rid of it, run `/tick freeze` on and give yourself a Command Block (`/give @s minecraft:command_block`)\n5. Place the Command Block and paste the following command into it:\n`kill @e[type=thebrokenscript:null_unbeatable_bossfight]`\n6. Set the Command Block to `Repeat` and always active.\n7. Run `/tick unfreeze` and destroy the Command Block after Null disappears.\n\nThis will also prevent further bans from Null.", false)
                        .setImage("https://media.discordapp.net/attachments/1353980336626728971/1397618104133226567/image.png?ex=688260ca&is=68810f4a&hm=3b6e196541357cee1e4d5bfd66f9ece05423e7117e36dceb7fe2d33535dbe71d&=&format=webp&quality=lossless&width=1244&height=604")
                        .build();
                break;
            case "enable-command-blocks":
                embed = EmbedUtils.createDefault()
                        .setTitle("Enable Command Blocks on your Server")
                        .addField("Steps", "1. Open the `server.properties` file in the root directory of your server\n2. Find the value `enable-command-block` and set it to `true`\n3. Restart your server", false)
                        .build();
                break;
            default:
                embed = EmbedUtils.createErrorEmbed("Error", "❌ **An Error occurred while trying to run this command.**\nPlease try again later.");
                break;
        }

        MessageHandler.sendPreparedMessage(event, embed);
    }
}

