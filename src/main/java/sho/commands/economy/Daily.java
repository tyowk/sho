package sho.commands.economy;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import sho.Sho;
import sho.structs.Command;

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
        String balanceStr = bot.db.get("balance", userId);
        if (balanceStr == null) balanceStr = "0";
        int balance = Integer.parseInt(balanceStr);

        int reward = (int) (Math.random() * 300 + 200);
        bot.db.put("balance", userId, Integer.toString(balance + reward));
        event.getChannel()
                .sendMessage("You claimed your daily reward of __**" + reward + "**__ Sho Coins!")
                .queue();
    }
}
