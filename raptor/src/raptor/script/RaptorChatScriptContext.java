package raptor.script;

import raptor.chat.ChatEvent;
import raptor.connector.Connector;

public class RaptorChatScriptContext extends RaptorScriptContext implements
		ChatScriptContext {
	ChatEvent event;
	boolean ignoreEvent = false;

	public RaptorChatScriptContext(Connector connector, ChatEvent event) {
		super(connector);
		this.event = event;
	}

	public RaptorChatScriptContext(Connector connector, String[] parameters,
			ChatEvent event) {
		super(connector, parameters);
		this.event = event;
	}

	public ChatEvent getChatEvent() {
		return event;
	}
}