package raptor.alias;

import raptor.swt.chat.ChatConsoleController;

public class ClearFingerNotesAlias extends RaptorAlias {
	public ClearFingerNotesAlias() {
		super("clear finger", "Removes all of your finger notes.",
				"'clear finger' " + "Example: 'clear finger'");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.equals("clear finger")) {
			RaptorAliasResult result = new RaptorAliasResult(null,
					"Clearing your finger notes.");
			controller.getConnector().sendMessage("set 9", true);
			controller.getConnector().sendMessage("set 8", true);
			controller.getConnector().sendMessage("set 7", true);
			controller.getConnector().sendMessage("set 6", true);
			controller.getConnector().sendMessage("set 5", true);
			controller.getConnector().sendMessage("set 4", true);
			controller.getConnector().sendMessage("set 3", true);
			controller.getConnector().sendMessage("set 2", true);
			controller.getConnector().sendMessage("set 1", true);
			return result;
		} else {
			return null;
		}
	}
}