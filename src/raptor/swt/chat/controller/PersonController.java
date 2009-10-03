package raptor.swt.chat.controller;

import org.apache.commons.lang.StringUtils;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.Connector;
import raptor.swt.chat.ChatConsoleController;

public class PersonController extends ChatConsoleController {

	protected String person;

	public PersonController(Connector connector, String person) {
		super(connector);
		this.person = person;
	}

	@Override
	public String getName() {
		return person;
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getCurrentLayoutQuadrant(
				PERSON_TAB_QUADRANT);
	}

	@Override
	public String getPrependText(boolean checkButton) {
		if (this.isIgnoringActions()) {
			return "";
		}

		if (checkButton && isToolItemSelected(PREPEND_TEXT_BUTTON)) {
			return connector.getPersonTabPrefix(person);
		} else if (!checkButton) {
			return connector.getPersonTabPrefix(person);
		} else {
			return "";
		}
	}

	@Override
	public String getPrompt() {
		return connector.getPrompt();
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
				&& (event.getType() == ChatType.TELL || event.getType() == ChatType.PARTNER_TELL);
	}

	protected boolean isOutboundTellPertainingToPerson(ChatEvent event) {
		return StringUtils.containsIgnoreCase(event.getMessage(), person)
				&& event.getType() == ChatType.OUTBOUND;
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
