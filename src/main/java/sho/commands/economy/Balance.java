package sho.commands.economy;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import sho.Sho;
import sho.structs.Command;

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
        return new String[] {"bal"};
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args, Sho bot) {
        String userId = event.getAuthor().getId();
        String balance = bot.db.get("balance", userId);
        if (balance == null) balance = "0";

        event.getChannel().sendMessage("You have __**" + balance + "**__ Sho Coins!").queue();
    }
}
