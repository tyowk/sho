package sho.events;

import sho.Sho;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.session.ReadyEvent;

public class ReadyListener extends ListenerAdapter {
	@Override
	public void onReady(ReadyEvent event) {
		System.out.println(" ");
		System.out.println(
			"""
			 ______     __  __     ______    
			/\\  ___\\   /\\ \\_\\ \\   /\\  __ \\   
			\\ \\___  \\  \\ \\  __ \\  \\ \\ \\/\\ \\  
			 \\/\\_____\\  \\ \\_\\ \\_\\  \\ \\_____\\ 
			  \\/_____/   \\/_/\\/_/   \\/_____/ 
			
			"""
		);

		Sho.logger.info(
			"logged in as {}#{}",
			event.getJDA().getSelfUser().getName(),
			event.getJDA().getSelfUser().getDiscriminator()
		);
	}
}