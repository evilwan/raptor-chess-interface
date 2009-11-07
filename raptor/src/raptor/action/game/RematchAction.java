package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chat.ChatConsoleWindowItem;

public class RematchAction extends AbstractRaptorAction {
	public RematchAction() {
		setName("Rematch");
		setDescription("Issues a rematch to your last opponent.");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null
				&& getChessBoardControllerSource().getConnector() != null) {
			getChessBoardControllerSource().getConnector().onRematch();
			wasHandled = true;
		} else if (getChatConsoleControllerSource() != null) {
			getChatConsoleControllerSource().getConnector().onRematch();
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getWindowItems(ChatConsoleWindowItem.class);
			for (RaptorWindowItem item : items) {
				ChatConsoleWindowItem chatConsoleWindowItem = (ChatConsoleWindowItem) item;
				if (chatConsoleWindowItem.getConnector() != null
						&& chatConsoleWindowItem.getConnector().isConnected()) {
					chatConsoleWindowItem.getConnector().onRematch();
				}
			}
		}
	}
}
