package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.chess.Game;
import raptor.chess.pgn.PgnHeader;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardWindowItem;

public class FENAction extends AbstractRaptorAction {
	public FENAction() {
		setName("FEN");
		setDescription("Displays the current position's FEN (Forsyth Edwards Notation).");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			Game game = getChessBoardControllerSource().getGame();
			Raptor.getInstance().promptForText(
					"FEN for game " + game.getHeader(PgnHeader.White) + " vs "
							+ game.getHeader(PgnHeader.Black), game.toFen());
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardController controller = ((ChessBoardWindowItem) item)
						.getController();
				Game game = controller.getGame();
				Raptor.getInstance().promptForText(
						"FEN for game " + game.getHeader(PgnHeader.White)
								+ " vs " + game.getHeader(PgnHeader.Black),
						game.toFen());
			}
		}
	}

}