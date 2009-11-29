package raptor.alias;

import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class TellLastPersonWhoToldYouAlias extends RaptorAlias {
	public TellLastPersonWhoToldYouAlias() {
		super(
				"%",
				"Similar to '.' on ICS servers, however instead of telling the last person you told a message to it tells the last person who sent you a message.",
				"'% message'. Example: '% hello' will send tell XXX hello "
						+ "where XXX was the last person who sent me a direct tell.");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("%") && !command.startsWith("%%")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			String firstWord = tok.nextToken().substring(1);

			if (firstWord.equals("")) {
				return new RaptorAliasResult("tell "
						+ controller.getSourceOfLastTellReceived() + " "
						+ tok.getWhatsLeft(), null);
			}
		}
		return null;
	}
}