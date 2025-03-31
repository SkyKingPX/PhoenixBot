package de.skyking_px.PhoenixBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Bot {
    public static final String[] TBS_COMMAND_ARGUMENTS = {"unsafe", "install-tbs", "not-whitelisted", "tbs-multiplayer", "devmode-nbt"};

    public static File config;
    public static String BOT_TOKEN;

    public static void main(String[] arguments) throws Exception {

        try {
            config = new File("bot.txt");
            if (config.createNewFile()) {
                System.out.println("Config File created. Please paste the Bot token inside it");
            } else {
                System.out.println("Config File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the config file.");
            e.printStackTrace();
        }
        try {
            Scanner myReader = new Scanner(config);
            while (myReader.hasNextLine()) {
                BOT_TOKEN = myReader.nextLine();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        JDA api = JDABuilder.createDefault(BOT_TOKEN)
                .addEventListeners(new LogUploader(), new de.skyking_px.PhoenixBot.GuildJoinListener())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("Version 1.3 TESTING"))
                .setStatus(OnlineStatus.ONLINE)
                .build();

        // Register Commands
        api.updateCommands()
                .addCommands(TBSCommand.getTBSCommand())
                .queue();
    }

}
