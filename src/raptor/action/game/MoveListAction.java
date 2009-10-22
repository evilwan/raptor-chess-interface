package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.ToolBarItemKey;

public class MoveListAction extends AbstractRaptorAction {
	public MoveListAction() {
		setName("Toggle Showing Move List");
		setDescription("If the move list is being displayed, it is hidden. "
				+ "If the move list is not being displayed, it is shown.");
		setIcon("moveList");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			getChessBoardControllerSource().onMoveList();
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardController controller = ((ChessBoardWindowItem) item)
						.getController();
				controller.setToolItemSelected(ToolBarItemKey.MOVE_LIST,
						!controller
								.isToolItemSelected(ToolBarItemKey.MOVE_LIST));
			}
		}
	}

}