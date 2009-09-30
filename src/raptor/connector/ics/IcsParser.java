package raptor.connector.ics;

import raptor.chat.ChatEvent;
import raptor.service.GameService;

public interface IcsParser {
	public ChatEvent[] parse(String inboundEvent);

	public String parseOutAndProcessGameEvents(GameService service,
			String inboundEvent);
}
