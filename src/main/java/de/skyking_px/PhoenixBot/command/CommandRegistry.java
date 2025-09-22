package de.skyking_px.PhoenixBot.command;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * Registry for all Discord slash commands supported by PhoenixBot.
 * Centralizes command definition and configuration.
 * 
 * @author SkyKing_PX
 */
public class CommandRegistry {
    /**
     * Registers and configures all slash commands for the bot.
     * 
     * @return List of CommandData objects to register with Discord
     */
    public static List<CommandData> registerCommands() {
        CommandData faq = Commands.slash("faq", "Suggests a user to read the FAQ")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "Optionally choose if you want to ping a member", false)
                );

        CommandData info = Commands.slash("info", "Shows some useful information about the bot");

        CommandData close = Commands.slash("close", "Closes a Forum Post");

        CommandData tbs = Commands.slash("tbs", "All commands related to \"The Broken Script\" and the \"Lost World\" Modpack")
                .addOptions(
                        new OptionData(OptionType.STRING, "argument", "The argument of your prompt", true)
                                .addChoices(
                                        new Command.Choice("Install TBS outside of the Modpack", "install-standalone"),
                                        new Command.Choice("Fix the error message \"You are not whitelisted\"", "not-whitelisted"),
                                        new Command.Choice("Multiplayer compatibility with TBS", "tbs-multiplayer"),
                                        new Command.Choice("Enable Cheats by editing the Common Config", "enable-cheats"),
                                        new Command.Choice("Close the Game when the \"Quit Game\" Button isn't working", "no-exit"),
                                        new Command.Choice("Get information about the unsafe Version of TBS", "unsafe"),
                                        new Command.Choice("Fix common issues and crashes", "common-issues"),
                                        new Command.Choice("Unban / Whitelist every player back on a Server", "unban-server"),
                                        new Command.Choice("Unban / Whitelist every player back on an integrated Server", "unban-integrated"),
                                        new Command.Choice("Enable Command Blocks on your Server", "enable-command-blocks")
                                ),
                        new OptionData(OptionType.USER, "user", "Optionally choose if you want to ping a member", false)
                );

        CommandData sendFaq = Commands.slash("sendfaq", "Prints out all configured FAQ entries in the FAQ channel");

        CommandData reload = Commands.slash("reloadconfig", "Reloads the bot's configuration");

        CommandData createTicketPanel = Commands.slash("createticketpanel", "Creates a ticket panel in a specified channel")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel where the ticket panel should be created", true));

        return List.of(faq, info, close, tbs, sendFaq, reload, createTicketPanel);
    }
}
