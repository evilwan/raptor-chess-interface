package raptor.action.chat;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;
import raptor.swt.FicsSeekDialog;

public class FicsSeekAction extends AbstractRaptorAction {
	public FicsSeekAction() {
		setName("Seek");
		setDescription("Display a seek dialog to find a game on Fics.");
		setCategory(Category.IcsCommands);
	}

	public void run() {
		if (getChatConsoleControllerSource() != null) {
			FicsSeekDialog dialog = new FicsSeekDialog(Raptor.getInstance()
					.getWindow().getShell());
			String seek = dialog.open();
			if (seek != null) {
				getChatConsoleControllerSource().getConnector().sendMessage(
						seek);
			}
		}
	}
}