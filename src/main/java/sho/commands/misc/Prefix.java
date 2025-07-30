package sho.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import sho.Sho;
import sho.structs.Command;

import java.awt.Color;

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
        EmbedBuilder embed = new EmbedBuilder();
        if (args.length == 0) {
            embed.setColor(Color.RED);
            embed.setDescription("❓  |  Please provide a new prefix.");
            event.getChannel()
                    .sendMessageEmbeds(embed.build())
                    .queue(msg -> Command.deleteAfter(msg, 10));
            embed.clear();
            return;
        }

        String guildId = event.getGuild().getId();
        String newPrefix = args[0];
        String oldPrefix = bot.getPrefix(guildId);

        if (newPrefix.isEmpty() || newPrefix.length() > 5) {
            embed.setColor(Color.RED);
            embed.setDescription("❓  |  Prefix must be between 1 and 5 characters long.");
            event.getChannel()
                    .sendMessageEmbeds(embed.build())
                    .queue(msg -> Command.deleteAfter(msg, 10));
            embed.clear();
            return;
        }

        bot.db.put("guilds", "prefix", guildId, newPrefix);
        embed.setColor(Color.GREEN);
        embed.setDescription(
                "📁  |  Prefix changed from `" + oldPrefix + "` to `" + newPrefix + "`.");
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        embed.clear();
    }
}
