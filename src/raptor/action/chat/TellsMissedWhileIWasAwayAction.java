package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;

public class TellsMissedWhileIWasAwayAction extends AbstractRaptorAction {
	public TellsMissedWhileIWasAwayAction() {
		setName("Show Tells Missed While Away");
		setIcon("chat");
		setDescription("Shows all of the tells you missed while you were away. "
				+ "This list is reset each time you send a message.");
		setCategory(Category.ConsoleCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null) {
			getChatConsoleControllerSource().onAway();
			wasHandled = true;
		}

		if (!wasHandled) {
			Raptor.getInstance().alert(
					getName() + " is only avalible from ChatConsole sources.");
		}
	}
}