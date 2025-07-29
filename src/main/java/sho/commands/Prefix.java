package sho.commands;

import sho.Sho;
import sho.structs.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Prefix extends Command {
	@Override
	public String getName() {
		return "prefix";
	}

	@Override
	public String getDescription() {
		return "Changes the guild's prefix.";
	}

	@Override
	public void execute(MessageReceivedEvent event, String[] args, Sho bot) {
		if (args.length == 0) {
			event.getChannel()
				.sendMessage("Please provide a new prefix. Usage: `prefix <newPrefix>`")
				.queue();
			return;
		}

		String guildId = event.getGuild().getId();
		String newPrefix = args[0];
		String oldPrefix = bot.db.get("prefix:" + guildId);
		if (oldPrefix == null) oldPrefix = bot.config.prefix;
		if (oldPrefix == null) oldPrefix = "sho";

		if (newPrefix.isEmpty() || newPrefix.length() > 5) {
			event.getChannel()
				.sendMessage("Prefix must be between 1 and 5 characters long.")
				.queue();
			return;
		}

		bot.db.put("prefix:" + guildId, newPrefix);
		event.getChannel()
			.sendMessage("Prefix changed from `" + oldPrefix + "` to `" + newPrefix + "`.")
			.queue();
	}
}
