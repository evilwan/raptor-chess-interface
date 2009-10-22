package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;

public class ChatSaveAction extends AbstractRaptorAction {
	public ChatSaveAction() {
		setName("Save Chat Console");
		setIcon("save");
		setDescription("Saves the content of the chat console to a file.");
		setCategory(Category.ConsoleCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null) {
			getChatConsoleControllerSource().onSave();
			wasHandled = true;
		}

		if (!wasHandled) {
			Raptor.getInstance().alert(
					getName() + " is only avalible from ChatConsole sources.");
		}
	}
}