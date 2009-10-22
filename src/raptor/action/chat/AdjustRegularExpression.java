package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chat.controller.RegExController;

public class AdjustRegularExpression extends AbstractRaptorAction {
	public AdjustRegularExpression() {
		setName("Adjust Regular Expression (RegEx Tabs Only)");
		setIcon("wrench");
		setDescription("Changes the regular expression currently being used in the RegEx tab.");
		setCategory(Category.ConsoleCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null
				&& getChatConsoleControllerSource() instanceof RegExController) {
			((RegExController) getChatConsoleControllerSource())
					.onAdjustRegEx();
			wasHandled = true;
		}

		if (!wasHandled) {
			Raptor.getInstance().alert(
					getName() + " is only avalible from ChatConsole sources.");
		}
	}
}