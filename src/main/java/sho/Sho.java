package sho;

import sho.structs.Command;
import sho.structs.Config;
import sho.structs.Database;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.WeakHashMap;

public class Sho {
	public static final Logger logger = LoggerFactory.getLogger(Sho.class);
	public final Map<String, Command> commands = new WeakHashMap<>();
	public final JDA jda;
	public final Config config;
	public final Database db;

	public Sho(Config config) throws Exception {
		this.config = config;
		if (config.token == null || config.token.isBlank()) {
			throw new IllegalArgumentException("Client token is missing or empty. Please provide a valid token!");
		}

		logger.info("Bootstrapping client instance...");
		this.jda = JDABuilder.createLight(
			config.token,
			GatewayIntent.GUILD_MESSAGES,
			GatewayIntent.MESSAGE_CONTENT,
			GatewayIntent.GUILD_MEMBERS
		)
			.addEventListeners(
				new sho.events.MessageListener(this),
				new sho.events.ReadyListener()
			)
			.setActivity(Activity.playing("Learning Java"))
			.build();

		jda.awaitReady();
		loadCommandRegistry();
		logger.info("JDA connection established.");

		this.db = new Database(config.database);
		logger.info("Database connection established.");
	}

	private void loadCommandRegistry() {
		commands.put("ping", new sho.commands.Ping());
		commands.put("prefix", new sho.commands.Prefix());
		logger.info("Registered {} commands!", commands.size());
	}

	public Command getCommand(String name) {
		return commands.getOrDefault(name, null);
	}

	public static void main(String[] args) {
		try {
			Config config = Config.load();
			new Sho(config);
		} catch (IllegalArgumentException e) {
			logger.error("Configuration error: {}", e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			logger.error("Fatal startup error. Client initialization failed.", e);
			System.exit(1);
		}
	}
}
