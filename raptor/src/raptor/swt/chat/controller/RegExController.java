package raptor.swt.chat.controller;

import org.apache.commons.lang.StringUtils;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.swt.chat.ChatConsoleController;

public class RegExController extends ChatConsoleController {

	protected String regularExpression;

	public RegExController(Connector connector, String regularExpressions) {
		super(connector);
		this.regularExpression = regularExpressions;
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				PreferenceKeys.APP_REGEX_TAB_QUADRANT);
	}

	@Override
	public String getPrependText() {
		return "";
	}

	@Override
	public String getPrompt() {
		return connector.getPrompt();
	}

	@Override
	public String getTitle() {
		return connector.getShortName() + "("
				+ StringUtils.abbreviate(regularExpression, 10) + ")";
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent event) {
		return event.getMessage() != null ? event.getMessage().matches(
				regularExpression) : false;
	}

	@Override
	public boolean isAwayable() {
		return false;
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