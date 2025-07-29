package sho.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sho.Sho;
import sho.structs.Command;

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
        System.currentTimeMillis() - event.getMessage().getTimeCreated().toInstant().toEpochMilli();
    long gatewayPing = event.getJDA().getGatewayPing();

    event
        .getChannel()
        .sendMessage(
            """
            Pong!
            API Ping: %dms
            Gateway Ping: %dms
            """
                .formatted(messagePing, gatewayPing))
        .queue();
  }
}
