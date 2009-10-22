package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.ToolBarItemKey;

public class AutoQueenAction extends AbstractRaptorAction {
	public AutoQueenAction() {
		setName("Auto Queen");
		setDescription("Sets promotions to always be queens.");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardWindowItem chessBoardItem = (ChessBoardWindowItem) item;
				chessBoardItem.getController().setToolItemSelected(
						ToolBarItemKey.AUTO_QUEEN,
						chessBoardItem.getController().isToolItemSelected(
								ToolBarItemKey.AUTO_QUEEN));
			}
		}
	}
}
