package raptor.swt.chat.controller;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.swt.chat.ChatConsoleController;

public class MainController extends ChatConsoleController {

	public MainController(Connector connector) {
		super(connector);
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				PreferenceKeys.APP_MAIN_TAB_QUADRANT);
	}

	@Override
	public String getPrompt() {
		return connector.getPrompt();
	}

	@Override
	public String getTitle() {
		return connector.getShortName() + "(Main)";
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return true;
	}

	@Override
	public boolean isAwayable() {
		return true;
	}

	@Override
	public boolean isCloseable() {
		return false;
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
