package sho.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import sho.Sho;
import sho.structs.Command;

import java.awt.Color;

public class Ping extends Command {
    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "Ping? Ping Pong!";
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args, Sho bot) {
        long messagePing =
                System.currentTimeMillis()
                        - event.getMessage().getTimeCreated().toInstant().toEpochMilli();
        long gatewayPing = event.getJDA().getGatewayPing();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setDescription(
                """
                Pong!
                API Ping: %dms
                Gateway Ping: %dms
                """
                        .formatted(messagePing, gatewayPing));
        embed.setColor(Color.GREEN);

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        embed.clear();
    }
}
