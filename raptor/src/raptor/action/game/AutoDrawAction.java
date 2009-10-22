package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.PlayingController;
import raptor.swt.chess.controller.ToolBarItemKey;

public class AutoDrawAction extends AbstractRaptorAction {
	public AutoDrawAction() {
		setName("Auto Draw");
		setDescription("Immediately offers a draw, then proceeds to offer a "
				+ "draw on every subsequent move until it is unselected.");
		setIcon("draw");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource() instanceof PlayingController) {
				((PlayingController) getChessBoardControllerSource())
						.onAutoDraw();
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
							.onAutoDraw();
					chessBoardItem.getController().setToolItemSelected(
							ToolBarItemKey.AUTO_DRAW,
							!chessBoardItem.getController().isToolItemSelected(
									ToolBarItemKey.AUTO_DRAW));
				}
			}
		}
	}
}
