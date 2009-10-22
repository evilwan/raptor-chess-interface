package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardWindowItem;

public class FirstAction extends AbstractRaptorAction {
	public FirstAction() {
		setName("First Move");
		setDescription("Goes back to the first move in the game.");
		setIcon("first");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			getChessBoardControllerSource().onFirst();
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardController controller = ((ChessBoardWindowItem) item)
						.getController();
				controller.onFirst();
			}
		}
	}

}
