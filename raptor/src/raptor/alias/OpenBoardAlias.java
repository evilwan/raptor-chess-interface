package raptor.alias;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Variant;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.controller.InactiveController;
import raptor.util.RaptorStringTokenizer;
import raptor.util.RaptorStringUtils;

public class OpenBoardAlias extends RaptorAlias {

	public OpenBoardAlias() {
		super(
				"openboard",
				"Brings up an a classic chess inactive board from the starting position. Optionally you can add a variant.",
				"openboard [variant] [FEN].\nVariants supported: "
						+ WordUtils.wrap(RaptorStringUtils.toDelimitedString(
								Variant.values(), ", "), 70)
						+ ".\n Examples: \"openboard\"\n \"openboard suicide\"\n "
						+ "\"openboard rnbbkrqn/pppppppp/8/8/8/8/PPPPPPPP/RNBBKRQN w KQkq - 0 1\"\n "
						+ "\"openboard suicide rnbbkrqn/pppppppp/8/8/8/8/PPPPPPPP/RNBBKRQN w KQkq - 0 1\"\b");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWithIgnoreCase(command, "openboard")) {
			String whatsLeft = command.substring(9).trim();

			String variant = Variant.classic.toString();
			String fen = null;

			if (!StringUtils.isBlank(whatsLeft)) {
				RaptorStringTokenizer tok = new RaptorStringTokenizer(
						whatsLeft, " ", true);
				variant = tok.nextToken();
				if (tok.hasMoreTokens()) {
					fen = tok.getWhatsLeft();
				}
			}

			try {
				Game game = null;
				if (fen == null) {
					game = GameFactory.createStartingPosition(Variant
							.valueOf(variant));
				} else {
					game = GameFactory.createFromFen(fen, Variant
							.valueOf(variant));
				}
				game.addState(Game.UNTIMED_STATE);
				game.addState(Game.UPDATING_ECO_HEADERS_STATE);
				game.addState(Game.UPDATING_SAN_STATE);
				ChessBoardUtils.openBoard(new InactiveController(game,
						"openboard " + variant + " Position", false));
				return new RaptorAliasResult(null, "Position created.");
			} catch (Throwable t) {
				return new RaptorAliasResult(null, "Invalid command: "
						+ getUsage());
			}
		}
		return null;
	}
}