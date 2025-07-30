package sho.structs;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import sho.Sho;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

public class Command {
    public String getName() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    public String getDescription() {
        return "No description provided.";
    }

    public String[] getAliases() {
        return null;
    }

    public String getCooldown() {
        return "2s";
    }

    public void execute(MessageReceivedEvent event, String[] args, Sho bot) {
        Sho.logger.warn("Command {} does not implement the execute() method.", getName());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.RED);
        embed.setDescription(
                "❓  |  Command `" + getName() + "` does not implement the execute method.");

        event.getChannel().sendMessageEmbeds(embed.build()).queue(msg -> deleteAfter(msg, 10));

        embed.clear();
        return;
    }

    public static void deleteAfter(Message msg, int seconds) {
        if (seconds <= 0 || seconds > 60 || msg == null) return;
        msg.delete().queueAfter(seconds, TimeUnit.SECONDS);
    }
}
