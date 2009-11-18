package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chat.controller.MainController;
import raptor.swt.chat.controller.PersonController;
import raptor.swt.chat.controller.ToolBarItemKey;

public class SpeakPersonTellsAction extends AbstractRaptorAction {
	public SpeakPersonTellsAction() {
		setName("Speak Person Tells");
		setIcon("musicNote");
		setDescription("Speaks all direct tells to you within this tab. Requires sound to be setup. See Preferences->Speech.");
		setCategory(Category.ConsoleCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null
				&& getChatConsoleControllerSource() instanceof MainController) {
			getChatConsoleControllerSource().getConnector()
					.setSpeakingAllPersonTells(
							getChatConsoleControllerSource()
									.isToolItemSelected(
											ToolBarItemKey.SPEAK_TELLS));
			wasHandled = true;
		} else if (getChatConsoleControllerSource() != null
				&& getChatConsoleControllerSource() instanceof PersonController) {
			getChatConsoleControllerSource()
					.getConnector()
					.setSpeakingPersonTells(
							((PersonController) getChatConsoleControllerSource())
									.getPerson(),
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
									+ " is only avalible from main and person console sources.");
		}
	}
}