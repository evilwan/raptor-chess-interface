package raptor.swt.chat.controller;

import org.eclipse.swt.widgets.Button;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.Connector;
import raptor.swt.chat.ChatConsole;
import raptor.swt.chat.ChatConsoleController;

public class PartnerTellController extends ChatConsoleController {

	public PartnerTellController(Connector connector) {
		super(connector);
	}

	@Override
	public String getName() {
		return "PartnerTells";
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getCurrentLayoutQuadrant(
				PARTNER_TELL_TAB_QUADRANT);
	}

	@Override
	public String getPrependText() {
		if (isIgnoringActions()) {
			return "";
		}

		String prependText = "";

		Button prependButton = chatConsole
				.getButton(ChatConsole.PREPEND_TEXT_BUTTON);
		if (prependButton != null) {
			if (prependButton.getSelection()) {
				prependText = connector.getPartnerTellPrefix();
			}
		}

		return prependText;
	}

	@Override
	public String getPrompt() {
		return connector.getPrompt();
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return (inboundEvent.getType() == ChatType.PARTNER_TELL)
				|| (inboundEvent.getType() == ChatType.OUTBOUND && connector
						.isLikelyPartnerTell(inboundEvent.getMessage()));
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