package raptor.alias;

import raptor.chess.Game;
import raptor.swt.chat.ChatConsoleController;

public class DumpGamesAlias extends RaptorAlias {

	public DumpGamesAlias() {
		super(
				"dumpgames",
				"Dumps debug information to the console of all the games currently being managed by the Connector.",
				"'dumpgames'. Example: 'dumpgames'");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("dumpgames")) {
			StringBuilder output = new StringBuilder(8000);
			output.append("dumpgames output:\n");
			Game[] games = controller.getConnector().getGameService()
					.getAllActiveGames();
			for (Game game : games) {
				output.append("Game " + game.getId() + ":\n" + game.toString());
			}
			return new RaptorAliasResult(null, output.toString());
		}
		return null;
	}
}
