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
		return chatConsole.getConnector().getShortName() + "("
				+ StringUtils.abbreviate(person, 10) + ")";
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent event) {
		return isDirectTellFromPerson(event)
				|| isOutboundTellPertainingToPerson(event);
	}

	@Override
	public boolean isAwayable() {
		return false;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	protected boolean isDirectTellFromPerson(ChatEvent event) {
		return StringUtils.equalsIgnoreCase(event.getSource(), person)
				&& (event.getType() == ChatTypes.TELL || event.getType() == ChatTypes.PARTNER_TELL);
	}

	protected boolean isOutboundTellPertainingToPerson(ChatEvent event) {
		return StringUtils.containsIgnoreCase(event.getMessage(), person)
				&& event.getType() == ChatTypes.OUTBOUND;
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
