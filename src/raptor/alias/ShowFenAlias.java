package raptor.alias;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.chess.Game;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chess.ChessBoardWindowItem;

public class ShowFenAlias extends RaptorAlias {

	public ShowFenAlias() {
		super("fen",
				"Shows the FEN for all of the boards currently being viwed.",
				"showfen fenString. Example: \"showfen\"");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.equalsIgnoreCase("fen")) {
			RaptorWindowItem[] windowItems = Raptor.getInstance().getWindow()
					.getWindowItems(ChessBoardWindowItem.class);

			StringBuilder text = new StringBuilder(400);
			if (windowItems.length > 0) {
				text.append("FEN for opened boards:\n");
				for (RaptorWindowItem item : windowItems) {
					Game game = ((ChessBoardWindowItem) item).getController()
							.getGame();
					text.append("Game " + game.getId() + "\t" + game.toFen());
				}
			} else {
				text.append("There are no open boards to display FEN for.");
			}

			return new RaptorAliasResult(null, text.toString());

		}
		return null;
	}
}