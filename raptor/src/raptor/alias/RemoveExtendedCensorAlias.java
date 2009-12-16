package raptor.alias;

import raptor.swt.chat.ChatConsoleController;

public class RemoveExtendedCensorAlias extends RaptorAlias {
	public RemoveExtendedCensorAlias() {
		super("-extcensor", "Removes a user to extended censor. ",
				"'-extcensor userName'" + "Example: '-extcensor TheTactician'");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("-extcensor")) {
			if (command.length() < 13) {
				return new RaptorAliasResult(null, "Invalid command: "
						+ command + "\n" + getUsage());
			}
			String whatsLeft = command.substring(10).trim();

			if (whatsLeft.length() < 3) {
				return new RaptorAliasResult(null, "Invalid username: "
						+ whatsLeft + "\n" + getUsage());
			} else {
				if (controller.getConnector().removeExtendedCensor(whatsLeft)) {
					return new RaptorAliasResult(null, "Removed " + whatsLeft
							+ " from extended censor.");
				} else {
					return new RaptorAliasResult(null, "User " + whatsLeft
							+ " is not on extended censor.");
				}
			}
		} else {
			return null;
		}
	}
}
