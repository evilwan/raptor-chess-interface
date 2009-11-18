package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chat.controller.ChannelController;
import raptor.swt.chat.controller.ToolBarItemKey;

public class SpeakChannelTellsAction extends AbstractRaptorAction {
	public SpeakChannelTellsAction() {
		setName("Speak Channel Tells");
		setIcon("musicNote");
		setDescription("Speaks all channel tells to you within this tab. Requires sound to be setup. See Preferences->Speech.");
		setCategory(Category.ConsoleCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null
				&& getChatConsoleControllerSource() instanceof ChannelController) {
			getChatConsoleControllerSource()
					.getConnector()
					.setSpeakingChannelTells(
							((ChannelController) getChatConsoleControllerSource())
									.getChannel(),
							getChatConsoleControllerSource()
									.isToolItemSelected(
											ToolBarItemKey.SPEAK_TELLS));
			wasHandled = true;
		}

		if (!wasHandled) {
			Raptor
					.getInstance()
					.alert(
							getName()
									+ " is only avalible from channel console sources.");
		}
	}
}