package raptor.swt.chat.controller;

import org.eclipse.swt.widgets.Button;

import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.swt.chat.ChatConsole;
import raptor.swt.chat.ChatConsoleController;

public class PartnerTellController extends ChatConsoleController {

	public PartnerTellController() {
		super();
	}

	@Override
	public String getPrependText() {
		String prependText = "";

		Button prependButton = chatConsole
				.getButton(ChatConsole.PREPEND_TEXT_BUTTON);
		if (prependButton != null) {
			if (prependButton.getSelection()) {
				prependText = chatConsole.getConnector().getPartnerTellPrefix();
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
		return chatConsole.getConnector().getShortName() + "(PartnerTells)";
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return (inboundEvent.getType() == ChatTypes.PARTNER_TELL)
				|| (inboundEvent.getType() == ChatTypes.OUTBOUND && chatConsole
						.getConnector().isLikelyPartnerTell(
								inboundEvent.getMessage()));
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