package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.InactiveController;

public class SaveAsPgn extends AbstractRaptorAction {
	public SaveAsPgn() {
		setName("Save Game as PGN");
		setIcon("save");
		setDescription("Saves the current game as PGN(portable game notation).");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource() instanceof InactiveController) {
				((InactiveController) getChessBoardControllerSource()).onSave();
			}
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardController controller = ((ChessBoardWindowItem) item)
						.getController();
				if (controller instanceof InactiveController) {
					((InactiveController) getChessBoardControllerSource())
							.onSave();
				}
			}
		}
	}

}