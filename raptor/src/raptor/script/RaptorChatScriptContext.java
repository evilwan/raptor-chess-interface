package raptor.script;

import raptor.chat.ChatEvent;
import raptor.connector.Connector;

public class RaptorChatScriptContext extends RaptorScriptContext implements
		ChatScriptContext {
	ChatEvent event;

	public RaptorChatScriptContext(Connector connector, ChatEvent event) {
		super(connector);
		this.event = event;
	}

	public ChatEvent getChatEvent() {
		return event;
	}
}