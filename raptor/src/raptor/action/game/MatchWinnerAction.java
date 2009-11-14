package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.chess.Variant;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.ObserveController;
import raptor.swt.chess.controller.ToolBarItemKey;

public class MatchWinnerAction extends AbstractRaptorAction {
	public MatchWinnerAction() {
		setName("Winners");
		setDescription("Matches the winner of this game automatically when it is over.");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource() instanceof ObserveController) {
				matchWinner(getChessBoardControllerSource());
			}
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardWindowItem chessBoardWindowItem = (ChessBoardWindowItem) item;
				if (chessBoardWindowItem.getController() instanceof ObserveController) {
					matchWinner(chessBoardWindowItem.getController());
				}
			}
		}
	}

	protected void matchWinner(ChessBoardController controller) {
		if (getChessBoardControllerSource().isToolItemSelected(
				ToolBarItemKey.MATCH_WINNER)) {
			if (controller.getGame().getVariant() == Variant.bughouse
					|| controller.getGame().getVariant() == Variant.fischerRandomBughouse) {
				getChessBoardControllerSource().getConnector().kibitz(
						getChessBoardControllerSource().getGame(),
						"Winners please");
			} else {
				getChessBoardControllerSource().getConnector().kibitz(
						getChessBoardControllerSource().getGame(),
						"Winner please");
			}
		} else {
			if (controller.getGame().getVariant() == Variant.bughouse
					|| controller.getGame().getVariant() == Variant.fischerRandomBughouse) {
				getChessBoardControllerSource().getConnector().kibitz(
						getChessBoardControllerSource().getGame(),
						"No longer calling winners.");
			} else {
				getChessBoardControllerSource().getConnector().kibitz(
						getChessBoardControllerSource().getGame(),
						"No longer calling winner.");
			}
		}
	}
}