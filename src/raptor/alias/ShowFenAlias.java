package raptor.alias;

import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Variant;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.controller.InactiveController;

public class ShowFenAlias extends RaptorAlias {

	public ShowFenAlias() {
		super(
				"showfen",
				"Brings up an inactive board from a specified FEN string. Currently only supports classic chess.",
				"showfen fenString. Example: \"showfen rnbbkrqn/pppppppp/8/8/8/8/PPPPPPPP/RNBBKRQN w KQkq - 0 1\"");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("showfen")) {
			String whatsLeft = command.substring(7).trim();

			if (whatsLeft.equals("")) {

			} else {
				try {
					Game game = GameFactory.createFromFen(whatsLeft,
							Variant.classic);
					game.addState(Game.UNTIMED_STATE);
					game.addState(Game.UPDATING_ECO_HEADERS_STATE);
					game.addState(Game.UPDATING_SAN_STATE);
					ChessBoardUtils.openBoard(new InactiveController(game,
							"showfen Position", false));
					return new RaptorAliasResult(null, "Position created.");
				} catch (Throwable t) {
					return new RaptorAliasResult(null, "Invalid FEN "
							+ whatsLeft);
				}
			}
		}
		return null;
	}
}