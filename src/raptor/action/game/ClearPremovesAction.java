package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.PlayingController;

public class ClearPremovesAction extends AbstractRaptorAction {
	public ClearPremovesAction() {
		setName("Clear Premoves");
		setDescription("Clears all premoves in the game you are playing. (Playing Only)");
		setIcon("redx");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource() instanceof PlayingController) {
				((PlayingController) getChessBoardControllerSource())
						.onClearPremoves();
				wasHandled = true;
			}
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardWindowItem chessBoardItem = (ChessBoardWindowItem) item;
				if (chessBoardItem.getController() instanceof PlayingController) {
					((PlayingController) chessBoardItem.getController())
							.onClearPremoves();
				}
			}
		}
	}
}