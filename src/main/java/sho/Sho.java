package sho;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sho.structs.Command;
import sho.structs.Config;
import sho.structs.Database;
import sho.structs.Time;

import java.util.Map;
import java.util.WeakHashMap;

public class Sho {
    public static final Logger logger = LoggerFactory.getLogger(Sho.class);
    public final Map<String, Command> commands = new WeakHashMap<>();
    public final JDA jda;
    public final Config config;
    public final Database db;
    public final Time time = new Time();

    public Sho() throws Exception {
        this.config = Config.load();
        if (config.token == null || config.token.isBlank()) {
            throw new IllegalArgumentException(
                    "Client token is missing or empty. Please provide a valid token!");
        }

        logger.info("Bootstrapping client instance...");
        this.jda =
                JDABuilder.createLight(
                                config.token,
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.MESSAGE_CONTENT,
                                GatewayIntent.GUILD_MEMBERS)
                        .addEventListeners(
                                new sho.events.MessageListener(this),
                                new sho.events.ReadyListener())
                        .setActivity(Activity.customStatus("Booting up..."))
                        .setStatus(OnlineStatus.IDLE)
                        .build();

        jda.awaitReady();
        loadCommandRegistry();
        logger.info("JDA connection established");

        this.db = new Database(config.getDatabasePath(), config.getTables());
        logger.info("Database connection established");

        sleep(3000);
        logger.info(" ");
        logger.info(
                "RAM usage: {} MB",
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                        / 1024
                        / 1024);
        logger.info("DISK usage: {} MB", new java.io.File(".").getTotalSpace() / 1024 / 1024);
        logger.info("CPU usage: {}%", Runtime.getRuntime().availableProcessors() * 100);
    }

    private void loadCommandRegistry() {
        commands.put("ping", new sho.commands.misc.Ping());
        commands.put("prefix", new sho.commands.misc.Prefix());
        commands.put("balance", new sho.commands.economy.Balance());
        commands.put("daily", new sho.commands.economy.Daily());
        logger.info("Registered {} commands!", commands.size());
    }

    public Command getCommand(String name) {
        String lowerName = name.toLowerCase();
        Command cmd = commands.get(lowerName);
        if (cmd != null) return cmd;

        for (Command command : commands.values()) {
            String[] aliases = command.getAliases();
            if (aliases == null) continue;

            for (String alias : aliases) {
                if (!alias.equalsIgnoreCase(lowerName)) continue;
                return command;
            }
        }

        return null;
    }

    public String getPrefix(String id) {
        String prefix = db.get("guilds", "prefix", id);
        if (prefix == null) prefix = config.getPrefix();
        return prefix;
    }

    public Database getDatabase() {
        return db;
    }

    public JDA getJDA() {
        return jda;
    }

    public Config getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }

    public static void main(String[] args) {
        try {
            new Sho();
        } catch (IllegalArgumentException e) {
            logger.error("Configuration error: {}", e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error("Fatal startup error. Client initialization failed.", e);
            System.exit(1);
        }
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
