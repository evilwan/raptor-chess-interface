package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;

public class SearchAction extends AbstractRaptorAction {
	public SearchAction() {
		setName("Search");
		setIcon("search");
		setDescription("Searches backward for the message in the console text. "
				+ "The search is case insensitive and does not use regular expressions.");
		setCategory(Category.ConsoleCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null) {
			getChatConsoleControllerSource().onSearch();
			wasHandled = true;
		}

		if (!wasHandled) {
			Raptor.getInstance().alert(
					getName() + " is only avalible from ChatConsole sources.");
		}
	}
}