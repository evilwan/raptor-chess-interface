package raptor.swt.chat.controller;

import org.eclipse.swt.widgets.Button;

import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.swt.chat.ChatConsole;
import raptor.swt.chat.ChatConsoleController;

public class ChannelController extends ChatConsoleController {

	protected String channel;

	public ChannelController(String channel) {
		super();
		this.channel = channel;
	}

	@Override
	public String getPrependText() {
		String prependText = "";

		Button prependButton = chatConsole
				.getButton(ChatConsole.PREPEND_TEXT_BUTTON);
		if (prependButton != null) {
			if (prependButton.getSelection()) {
				prependText = chatConsole.getConnector().getChannelTabPrefix(
						channel);
			}
		}

		return prependText;
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
		return true;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}
}
