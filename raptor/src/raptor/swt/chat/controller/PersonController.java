package raptor.swt.chat.controller;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Button;

import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.swt.chat.ChatConsole;
import raptor.swt.chat.ChatConsoleController;

public class PersonController extends ChatConsoleController {

	protected String person;

	public PersonController(String person) {
		super();
		this.person = person;
	}

	@Override
	public String getPrependText() {
		String prependText = "";

		Button prependButton = chatConsole
				.getButton(ChatConsole.PREPEND_TEXT_BUTTON);
		if (prependButton != null) {
			if (prependButton.getSelection()) {
				prependText = chatConsole.getConnector().getChannelTabPrefix(
						person);
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
		return chatConsole.getConnector().getShortName() + "(" + person + ")";
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return (!StringUtils.isBlank(inboundEvent.getSource())
				&& inboundEvent.getSource().equals(person) && (inboundEvent
				.getType() == ChatTypes.TELL || inboundEvent.getType() == ChatTypes.PARTNER_TELL))
				|| (inboundEvent.getType() == ChatTypes.OUTBOUND && inboundEvent
						.getMessage().contains(person));
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
