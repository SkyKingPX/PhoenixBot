package de.skyking_px.PhoenixBot;

import de.skyking_px.PhoenixBot.command.FAQCommand;
import de.skyking_px.PhoenixBot.command.InfoCommand;
import de.skyking_px.PhoenixBot.command.TBSCommand;
import de.skyking_px.PhoenixBot.util.LogUploader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bot {
    public static File config;
    public static String BOT_TOKEN;
    public static String BOT_STATUS;
    public static final String VERSION = "1.4.2";
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
                BOT_STATUS = myReader.nextLine();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            logger.error("[BOT] An error occurred.");
            e.printStackTrace();
        }

        JDA api = JDABuilder.createDefault(BOT_TOKEN)
                .addEventListeners(new TBSCommand(), new InfoCommand(), new FAQCommand(), new  LogUploader(), new Listener())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing(BOT_STATUS))
                .setStatus(OnlineStatus.ONLINE)
                .build();
    }

}
