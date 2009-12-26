package raptor.alias;

import org.apache.commons.lang.StringUtils;

import raptor.swt.chat.ChatConsoleController;

public class ShowExtendedCensor extends RaptorAlias {
	public ShowExtendedCensor() {
		super("=extcensor", "Displays all of the people on extended censor. ",
				"'=extcensor'" + "Example: '=extcensor'");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWith(command, "=extcensor")) {
			String[] users = controller.getConnector()
					.getPeopleOnExtendedCensor();

			StringBuilder output = new StringBuilder(2000);
			output.append("Extended Censor List: (" + users.length
					+ " user(s)):\n");
			int count = 0;
			for (int i = 0; i < users.length; i++) {
				output.append(StringUtils.rightPad(users[i], 20));
				count++;
				if (count == 3) {
					output.append("\n");
					count = 0;
				}
			}
			return new RaptorAliasResult(null, output.toString());

		} else {
			return null;
		}
	}
}
