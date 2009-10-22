package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;

public class AutoScrollAction extends AbstractRaptorAction {
	public AutoScrollAction() {
		setName("Auto Scroll");
		setIcon("down");
		setDescription("Forces auto scrolling.");
		setCategory(Category.ConsoleCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null) {
			getChatConsoleControllerSource().onForceAutoScroll();
			wasHandled = true;
		}

		if (!wasHandled) {
			Raptor.getInstance().alert(
					getName() + " is only avalible from ChatConsole sources.");
		}
	}
}