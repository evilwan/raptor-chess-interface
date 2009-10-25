package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;

public class ToggleScrollLock extends AbstractRaptorAction {
	public ToggleScrollLock() {
		setName("Toggle Scroll Lock");
		setIcon("locked");
		setDescription("Toggles scroll lock on and off.");
		setCategory(Category.ConsoleCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null) {
			getChatConsoleControllerSource().setAutoScrolling(
					!getChatConsoleControllerSource().isAutoScrolling());
			wasHandled = true;
		}

		if (!wasHandled) {
			Raptor.getInstance().alert(
					getName() + " is only avalible from ChatConsole sources.");
		}
	}
}