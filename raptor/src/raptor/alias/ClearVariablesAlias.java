package raptor.alias;

import raptor.swt.chat.ChatConsoleController;

public class ClearVariablesAlias extends RaptorAlias {
	public ClearVariablesAlias() {
		super("clear vars", "Removes all of your variables", "'clear vars' "
				+ "Example: 'clear vars'");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.equals("clear vars")) {
			RaptorAliasResult result = new RaptorAliasResult(null,
					"Clearing your variables.");
			controller.getConnector().sendMessage("set f9", true);
			controller.getConnector().sendMessage("set f8", true);
			controller.getConnector().sendMessage("set f7", true);
			controller.getConnector().sendMessage("set f6", true);
			controller.getConnector().sendMessage("set f5", true);
			controller.getConnector().sendMessage("set f4", true);
			controller.getConnector().sendMessage("set f3", true);
			controller.getConnector().sendMessage("set f2", true);
			controller.getConnector().sendMessage("set f1", true);
			return result;
		} else {
			return null;
		}
	}
}