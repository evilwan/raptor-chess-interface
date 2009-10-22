package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.chess.GameCursor.Mode;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.ObserveController;
import raptor.swt.chess.controller.ToolBarItemKey;

public class ForceUpdateAction extends AbstractRaptorAction {
	public ForceUpdateAction() {
		setName("UPDATE");
		setDescription("If checked, this will update the position to the latest moves as"
				+ " they arrive. If unchecked this will not update the position to the latest "
				+ "moves as they arrive. This is useful when you are looking at a previous move.");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource().isToolItemSelected(
					ToolBarItemKey.FORCE_UPDATE)) {
				if (getChessBoardControllerSource() instanceof ObserveController) {
					ObserveController controller = (ObserveController) getChessBoardControllerSource();
					controller.getCursor().setMode(
							Mode.MakeMovesOnMasterSetCursorToLast);
					controller.getCursor().setCursorMasterLast();
					controller.refresh();
				}
			} else if (getChessBoardControllerSource() instanceof ObserveController) {
				ObserveController controller = (ObserveController) getChessBoardControllerSource();
				controller.getCursor().setMode(Mode.MakeMovesOnMaster);
			}
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardWindowItem chessBoardItem = (ChessBoardWindowItem) item;
				chessBoardItem.getController().setToolItemSelected(
						ToolBarItemKey.FORCE_UPDATE,
						!chessBoardItem.getController().isToolItemSelected(
								ToolBarItemKey.FORCE_UPDATE));
				if (chessBoardItem.getController().isToolItemSelected(
						ToolBarItemKey.FORCE_UPDATE)) {
					if (getChessBoardControllerSource() instanceof ObserveController) {
						ObserveController controller = (ObserveController) getChessBoardControllerSource();
						controller.getCursor().setMode(
								Mode.MakeMovesOnMasterSetCursorToLast);
						controller.getCursor().setCursorMasterLast();
						controller.refresh();
					}
				} else if (chessBoardItem.getController() instanceof ObserveController) {
					ObserveController controller = (ObserveController) getChessBoardControllerSource();
					controller.getCursor().setMode(Mode.MakeMovesOnMaster);
				}
			}
		}
	}
}