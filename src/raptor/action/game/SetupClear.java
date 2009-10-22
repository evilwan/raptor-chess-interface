package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.SetupController;

public class SetupClear extends AbstractRaptorAction {
	public SetupClear() {
		setName("Clear");
		setDescription("Clears the board of all pieces (Setup Only).");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource().getConnector() != null
					&& getChessBoardControllerSource() instanceof SetupController) {
				getChessBoardControllerSource().getConnector().onSetupClear(
						getChessBoardControllerSource().getGame());
			}
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardController controller = ((ChessBoardWindowItem) item)
						.getController();
				if (controller.getConnector() != null
						&& controller instanceof SetupController) {
					controller.getConnector().onSetupClear(
							getChessBoardControllerSource().getGame());
				}
			}
		}
	}
}