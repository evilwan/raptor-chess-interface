package raptor.alias;

import java.util.HashMap;

import org.apache.commons.lang.math.NumberUtils;

import raptor.service.ThreadService;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class TimedCommandAlias extends RaptorAlias {

	public static HashMap<Runnable, Boolean> runningTimedCommands = new HashMap<Runnable, Boolean>();

	public TimedCommandAlias() {
		super(
				"timed",
				"Sends a repeating command every ### minutes. "
						+ "You can stop all timed commands with 'timed kill'",
				"'timed ### message'. Example: 'timed 1 tell 24 partner' will "
						+ "send tell 24 partner every 1 minute. To stop all timed commands use 'timed kill'.");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			final String command) {
		if (command.startsWith("timed")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			tok.nextToken();
			final String firstWord = tok.nextToken();

			if (firstWord.equals("kill")) {
				int commandsKilled = 0;
				for (Runnable key : runningTimedCommands.keySet()) {
					runningTimedCommands.put(key, false);
					commandsKilled++;
				}
				return new RaptorAliasResult(null, "Killed " + commandsKilled
						+ " timed commands.");
			} else if (NumberUtils.isDigits(firstWord)) {
				final String message = tok.getWhatsLeft();

				Runnable runnable = new Runnable() {
					public void run() {
						if (!controller.isDisposed()
								&& runningTimedCommands.get(this) != null
								&& runningTimedCommands.get(this) == true) {
							controller.getConnector().sendMessage(message);
							ThreadService.getInstance().scheduleOneShot(
									Integer.parseInt(firstWord) * 1000 * 60,
									this);
						}
					}
				};

				ThreadService.getInstance().scheduleOneShot(
						Integer.parseInt(firstWord) * 1000 * 60, runnable);
				runningTimedCommands.put(runnable, true);
				return new RaptorAliasResult(tok.getWhatsLeft(),
						"Commands will be from now on ever every " + firstWord
								+ " minutes. Use 'timed kill' to stop it.");
			} else {
				return new RaptorAliasResult(null, "Invalid syntax: Usage"
						+ getUsage());
			}
		}
		return null;
	}
}