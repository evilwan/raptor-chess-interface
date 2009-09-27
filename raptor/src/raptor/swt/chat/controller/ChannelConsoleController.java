package raptor.swt.chat.controller;

import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.swt.chat.ChatConsoleController;

public class ChannelConsoleController extends ChatConsoleController {

	protected String channel;

	public ChannelConsoleController(String channel) {
		super();
		this.channel = channel;
	}

	@Override
	public String getPrompt() {
		return chatConsole.getConnector().getPrompt();
	}

	@Override
	public String getTitle() {
		return chatConsole.getConnector().getShortName() + "(" + channel + ")";
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return inboundEvent.getType() == ChatTypes.CHAN_TELL
				&& inboundEvent.getChannel().equals(channel);
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public boolean isPrependable() {
		return false;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}
}
