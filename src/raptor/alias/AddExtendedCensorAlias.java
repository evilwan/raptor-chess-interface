package raptor.alias;

import raptor.swt.chat.ChatConsoleController;

public class AddExtendedCensorAlias extends RaptorAlias {
	public AddExtendedCensorAlias() {
		super("+extcensor", "Adds a user to extended censor. ",
				"'+extcensor userName'" + "Example: '+extcensor NewFoundGlory'");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("+extcensor")) {
			if (command.length() < 13) {
				return new RaptorAliasResult(null, "Invalid command: "
						+ command + "\n" + getUsage());
			}
			String whatsLeft = command.substring(10).trim();

			if (whatsLeft.length() < 3) {
				return new RaptorAliasResult(null, "Invalid username: "
						+ whatsLeft + "\n" + getUsage());
			} else {
				controller.getConnector().addExtendedCensor(whatsLeft);
				return new RaptorAliasResult(null, "Added " + whatsLeft
						+ " to extended censor.");
			}
		} else {
			return null;
		}
	}
}
