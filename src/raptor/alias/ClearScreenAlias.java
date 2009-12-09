package raptor.alias;

import raptor.swt.chat.ChatConsoleController;

public class ClearScreenAlias extends RaptorAlias {
	public ClearScreenAlias() {
		super("cls", "Clears the console screen.", "'cls' " + "Example: 'cls'");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.equals("cls")) {
			controller.getChatConsole().getInputText().setText("");
			return new RaptorAliasResult(null, null);
		} else {
			return null;
		}
	}
}