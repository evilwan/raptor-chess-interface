package raptor.swt.chat.controller;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Button;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.swt.chat.ChatConsole;
import raptor.swt.chat.ChatConsoleController;

public class ChannelController extends ChatConsoleController {

	protected String channel;

	public ChannelController(Connector connector, String channel) {
		super(connector);
		this.channel = channel;
	}

	@Override
	public String getName() {
		return channel;
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				PreferenceKeys.APP_CHANNEL_TAB_QUADRANT);
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
				prependText = connector.getChannelTabPrefix(channel);
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
		return inboundEvent.getType() == ChatType.CHAN_TELL
				&& StringUtils.equals(inboundEvent.getChannel(), channel);
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
