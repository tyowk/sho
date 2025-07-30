package sho.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import sho.Sho;
import sho.structs.Command;

import java.awt.Color;

public class Balance extends Command {
    @Override
    public String getName() {
        return "balance";
    }

    @Override
    public String getDescription() {
        return "Check your balance.";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"bal", "cash"};
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args, Sho bot) {
        String userId = event.getAuthor().getId();
        String balance = bot.db.get("economy", "shorency", userId);
        if (balance == null) balance = "0";

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.GREEN);
        embed.setDescription(
                "👛  |  You have __**"
                        + String.format("%,d", Integer.parseInt(balance))
                        + "**__ shorency.");

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        embed.clear();
    }
}
