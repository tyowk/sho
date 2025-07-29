package sho.events;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import sho.Sho;
import sho.structs.Command;

import java.util.Arrays;

public class MessageListener extends ListenerAdapter {
    private final Sho bot;

    public MessageListener(Sho bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getGuild() == null) return;

        String prefix = bot.db.get("prefix", event.getGuild().getId());
        if (prefix == null) prefix = bot.config.prefix;
        if (prefix == null) return;

        String content = event.getMessage().getContentRaw().trim();
        if (!content.startsWith(prefix)) return;

        String noPrefix = content.substring(prefix.length()).trim();
        if (noPrefix.isEmpty()) return;

        String[] split = noPrefix.split("\\s+");
        String commandName = split[0];
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        Command cmd = bot.getCommand(commandName);
        if (cmd == null) return;

        String userId = event.getAuthor().getId();
        long remaining = getCooldown(commandName, userId);
        if (remaining > 0) {
            event.getChannel()
                    .sendMessage(
                            "Wait "
                                    + (bot.time.format(remaining, true))
                                    + " before using this command again.")
                    .queue(
                            msg ->
                                    msg.delete()
                                            .queueAfter(10, java.util.concurrent.TimeUnit.SECONDS));
            return;
        }

        setCooldown(commandName, userId, bot.time.parse(cmd.getCooldown()));
        cmd.execute(event, args, bot);
    }

    private long getCooldown(String command, String userId) {
        String key = "cooldown:" + command;
        String value = bot.db.get(key, userId);
        if (value == null) return 0;
        try {
            long expire = Long.parseLong(value);
            long now = System.currentTimeMillis();
            if (now >= expire) {
                bot.db.delete(key, userId);
                return 0;
            }
            return expire - now;
        } catch (NumberFormatException e) {
            bot.db.delete(key, userId);
            return 0;
        }
    }

    private void setCooldown(String command, String userId, long cooldownMillis) {
        if (cooldownMillis <= 0) return;
        String key = "cooldown:" + command;
        long expireAt = System.currentTimeMillis() + cooldownMillis;
        bot.db.put(key, userId, String.valueOf(expireAt));
    }
}
