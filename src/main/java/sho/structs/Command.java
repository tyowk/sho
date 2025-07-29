package sho.structs;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sho.Sho;

public class Command {
  public String getName() {
    return this.getClass().getSimpleName().toLowerCase();
  }

  public String getDescription() {
    return "No description provided.";
  }

  public void execute(MessageReceivedEvent event, String[] args, Sho bot) {
    return;
  }
}
