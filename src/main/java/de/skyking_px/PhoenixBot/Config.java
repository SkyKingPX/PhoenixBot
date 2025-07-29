package de.skyking_px.PhoenixBot;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

    private static final Path CONFIG_PATH = Paths.get("config.yml");
    private static Config instance;

    // --- Config fields ---
    private Bot bot;
    private Logging logging;
    private Commands commands;
    private Voting voting;
    private Roles roles;
    private BugReport bugReport;
    private Support support;

    // Singleton getter
    public static Config get() throws IOException {
        if (instance == null) {
            instance = new Config();
            instance.load();
        }
        return instance;
    }

    // Laden der Config oder Erstellen einer Default-Config
    private void load() throws IOException {
        if (Files.notExists(CONFIG_PATH)) {
            createDefaultConfig();
        }

        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            LoaderOptions options = new LoaderOptions();
            Constructor constructor = new Constructor(Config.class, options);
            Yaml yaml = new Yaml(constructor);
            Config loaded = yaml.loadAs(input, Config.class);

            // Übernehme Werte aus geladener Config
            this.bot = loaded.bot;
            this.logging = loaded.logging;
            this.commands = loaded.commands;
            this.voting = loaded.voting;
            this.roles = loaded.roles;
            this.bugReport = loaded.bugReport;
            this.support = loaded.support;
        }
    }

    private void createDefaultConfig() throws IOException {
        String defaultConfig = """
            bot:
              token: "YOUR_BOT_TOKEN"
              activity: ""

            logging:
              channel_id: "1353805483428937738"

            commands:
              close_enabled: true

            voting:
              suggestions_forum_id: "1347456623576092764"
            
            support:
              support_forum_id: "1347289412060582031"
            
            bugReport:
              bugReport_forum_id: "1349267451883556894"
            
            roles:
              moderator: "1123224840426500228"
            """;

        Files.writeString(CONFIG_PATH, defaultConfig);
        System.out.println("Default config.yml created!");
    }

    // --- Getter + Setter ---

    public Bot getBot() { return bot; }
    public void setBot(Bot bot) { this.bot = bot; }

    public Logging getLogging() { return logging; }
    public void setLogging(Logging logging) { this.logging = logging; }

    public Commands getCommands() { return commands; }
    public void setCommands(Commands commands) { this.commands = commands; }

    public Voting getVoting() { return voting; }
    public void setVoting(Voting voting) { this.voting = voting; }

    public Roles getRoles() { return roles; }
    public void setRoles(Roles roles) { this.roles = roles; }

    public BugReport getBugReport() { return bugReport; }
    public void setBugReport(BugReport bugReport) { this.bugReport = bugReport; }

    public Support getSupport() { return support; }
    public void setSupport(Support support) { this.support = support; }

    public static class Bot {
        private String token;
        private String activity;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }
    }

    public static class Logging {
        private String channel_id;
        public String getChannel_id() { return channel_id; }
        public void setChannel_id(String channel_id) { this.channel_id = channel_id; }
    }

    public static class Commands {
        private boolean close_enabled;
        public boolean isClose_enabled() { return close_enabled; }
        public void setClose_enabled(boolean close_enabled) { this.close_enabled = close_enabled; }
    }

    public static class Voting {
        private String suggestions_forum_id;
        public String getSuggestions_forum_id() { return suggestions_forum_id; }
        public void setSuggestions_forum_id(String suggestions_forum_id) { this.suggestions_forum_id = suggestions_forum_id; }
    }

    public static class Roles {
        private String moderator;
        public String getModerator() { return moderator; }
        public void setModerator(String moderator) { this.moderator = moderator; }
    }

    public static class BugReport {
        private String bugReport_forum_id;
        public String getBugReport_forum_id() { return bugReport_forum_id; }
        public void setBugReport_forum_id(String bugReport_forum_id) { this.bugReport_forum_id = bugReport_forum_id; }
    }

    public static class Support {
        private String support_forum_id;
        public String getSupport_forum_id() { return support_forum_id; }
        public void setSupport_forum_id(String support_forum_id) { this.support_forum_id = support_forum_id; }
    }
}
