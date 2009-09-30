package raptor.swt.chat.controller;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.swt.chat.ChatConsoleController;

public class MainController extends ChatConsoleController {
	protected boolean isCloseable = false;

	public MainController(Connector connector) {
		super(connector);
	}

	@Override
	public boolean confirmClose() {
		boolean result = true;
		if (connector.isConnected()) {
			result = Raptor.getInstance().confirm(
					"Closing a main console will disconnect you from "
							+ connector.getShortName()
							+ ". Do you wish to proceed?");

			if (result) {
				connector.disconnect();
			}
		}
		return result;
	}

	@Override
	public String getName() {
		return "Main";
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
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return true;
	}

	@Override
	public boolean isAwayable() {
		return true;
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

	public void setCloseable() {

	}
}
