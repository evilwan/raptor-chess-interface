package raptor.swt.chat.controller;

import org.apache.commons.lang.StringUtils;

import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.swt.chat.ChatConsoleController;

public class PersonConsoleController extends ChatConsoleController {

	protected String person;

	public PersonConsoleController(String person) {
		super();
		this.person = person;
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
		return false;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}
}
