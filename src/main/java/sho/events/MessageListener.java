package sho.events;

import sho.Sho;
import sho.structs.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
	private Sho bot;

	public MessageListener(Sho bot) {
		this.bot = bot;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) return;
		String prefix = bot.db.get("prefix:" + event.getGuild().getId());
		if (prefix == null) prefix = bot.config.prefix;
		if (prefix == null) return;
		
		String content = event.getMessage().getContentRaw().trim();
		if (!content.startsWith(prefix)) return;
		String noPrefix = content.substring(prefix.length()).trim();
		if (noPrefix.isEmpty()) return;

		String[] split = noPrefix.split("\\s+");
		String commandName = split[0];
		String[] args = new String[Math.max(0, split.length - 1)];
		System.arraycopy(split, 1, args, 0, args.length);

		Command cmd = bot.getCommand(commandName);
		if (cmd == null) return;

		cmd.execute(event, args, bot);
	}
}
