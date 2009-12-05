package raptor.alias;

import org.apache.commons.lang.StringUtils;

import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Variant;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.controller.InactiveController;

public class OpenBoardAlias extends RaptorAlias {

	public OpenBoardAlias() {
		super(
				"openboard",
				"Brings up an a classic chess inactive board from the starting position. Optionally you can add a variant.",
				"openboard [variant]. Examples: \"openboard\" \"openboard suicide\"");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("openboard")) {
			String whatsLeft = command.substring(9).trim();

			try {
				Game game = StringUtils.isEmpty(whatsLeft) ? GameFactory
						.createStartingPosition(Variant.classic) : GameFactory
						.createStartingPosition(Variant.valueOf(whatsLeft));
				game.addState(Game.UNTIMED_STATE);
				game.addState(Game.UPDATING_ECO_HEADERS_STATE);
				game.addState(Game.UPDATING_SAN_STATE);
				ChessBoardUtils.openBoard(new InactiveController(game,
						"openboard Position", false));
				return new RaptorAliasResult(null, "Position created.");
			} catch (Throwable t) {
				return new RaptorAliasResult(
						null,
						"The variant name was either invalid, or the variant requires a FEN starting position: "
								+ whatsLeft + ".");
			}
		}
		return null;
	}
}