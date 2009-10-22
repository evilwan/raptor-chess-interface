package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;

public class PrependLastPersonWhoToldAction extends AbstractRaptorAction {
	public PrependLastPersonWhoToldAction() {
		setName("PrependLastPersonToTell");
		setDescription("Replaces the input text with tell followed by the name of the last person who sent you a direct tell.");
		setCategory(Category.ConsoleCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null) {
			getChatConsoleControllerSource().setInputToLastTell();
			wasHandled = true;
		}

		if (!wasHandled) {
			Raptor.getInstance().alert(
					getName() + " is only avalible from ChatConsole sources.");
		}
	}
}