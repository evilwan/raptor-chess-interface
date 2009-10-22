package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;

public class PrependAction extends AbstractRaptorAction {
	public PrependAction() {
		setName("Prepend");
		setDescription("Toggles if the 'tell person' or 'tell channel' text "
				+ "is prepended to the input field.");
		setCategory(Category.ConsoleCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null) {
			wasHandled = true;
		}

		if (!wasHandled) {
			Raptor.getInstance().alert(
					getName() + " is only avalible from ChatConsole sources.");
		}
	}
}