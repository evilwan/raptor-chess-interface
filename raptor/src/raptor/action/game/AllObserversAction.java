package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardWindowItem;

public class AllObserversAction extends AbstractRaptorAction {
	public AllObserversAction() {
		setName("Observers");
		setDescription("Shows all the users watching the game.");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			allObservers(getChessBoardControllerSource());
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardController controller = ((ChessBoardWindowItem) item)
						.getController();
				allObservers(controller);

			}
		}
	}

	protected void allObservers(ChessBoardController controller) {
		if (controller.getConnector() != null) {
			controller.getConnector().onObservers(controller.getGame());
		}
	}
}
