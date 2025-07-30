package sho.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import sho.Sho;
import sho.structs.Command;

import java.awt.Color;

public class Daily extends Command {
    @Override
    public String getName() {
        return "daily";
    }

    @Override
    public String getDescription() {
        return "Claim your daily reward.";
    }

    @Override
    public String getCooldown() {
        return "24h";
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args, Sho bot) {
        String userId = event.getAuthor().getId();
        String balanceStr = bot.db.get("economy", "shorency", userId);
        if (balanceStr == null) balanceStr = "0";
        int balance = Integer.parseInt(balanceStr);

        int reward = (int) (Math.random() * 2000 + 1500);
        bot.db.put("economy", "shorency", userId, Integer.toString(balance + reward));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.GREEN);
        embed.setDescription(
                """
                🪙  |  You claimed your daily reward of __**%,d**__ shorency!
                You can claim your next daily reward in **24 hours**!
                """
                        .formatted(reward));

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        embed.clear();
    }
}
