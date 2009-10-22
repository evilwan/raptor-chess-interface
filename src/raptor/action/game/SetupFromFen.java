package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.SetupController;

public class SetupFromFen extends AbstractRaptorAction {
	public SetupFromFen() {
		setName("FromFEN");
		setDescription("Sets the current position to a specified FEN (Forsyth Edwards Notation) string (Setup Only).");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource().getConnector() != null
					&& getChessBoardControllerSource() instanceof SetupController) {
				String result = Raptor.getInstance().promptForText(
						"Enter the FEN to set the position to:");
				if (result != null) {
					getChessBoardControllerSource().getConnector()
							.onSetupFromFEN(
									getChessBoardControllerSource().getGame(),
									result);
				}
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
					String result = Raptor.getInstance().promptForText(
							"Enter the FEN to set the position to:");
					if (result != null) {
						controller.getConnector().onSetupFromFEN(
								getChessBoardControllerSource().getGame(),
								result);
					}
				}
			}
		}
	}
}