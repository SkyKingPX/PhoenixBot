package de.skyking_px.PhoenixBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bot {
    public static final String[] TBS_COMMAND_ARGUMENTS = {"unsafe", "install-tbs", "not-whitelisted", "tbs-multiplayer", "devmode-nbt"};

    public static File config;
    public static String BOT_TOKEN;
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] arguments) throws Exception {

        try {
            config = new File("bot.txt");
            if (config.createNewFile()) {
                logger.info("[BOT] Config File created. Please paste the Bot token inside it");
            } else {
                logger.info("[BOT] Config File already exists.");
            }
        } catch (IOException e) {
            logger.error("[BOT] An error occurred while creating the config file.");
            e.printStackTrace();
        }
        try {
            Scanner myReader = new Scanner(config);
            while (myReader.hasNextLine()) {
                BOT_TOKEN = myReader.nextLine();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            logger.error("[BOT] An error occurred.");
            e.printStackTrace();
        }

        JDA api = JDABuilder.createDefault(BOT_TOKEN)
                .addEventListeners(new TBSCommand(), new LogUploader())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("Version 1.3"))
                .setStatus(OnlineStatus.ONLINE)
                .build();

        // Register Commands
        Guild guild = api.getGuildById("1116745011837534239");
        if (guild != null) {
            guild.updateCommands().addCommands(TBSCommand.getTBSCommand())
                    .queue();
        }
    }

}
