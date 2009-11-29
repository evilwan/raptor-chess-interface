package raptor.alias;

import org.apache.commons.lang.math.NumberUtils;

import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class AbbreviatedPersonTellAlias extends RaptorAlias {
	public AbbreviatedPersonTellAlias() {
		super(
				"%[name]",
				"Expands '%name message' into 'tell name msg'",
				"'%name message' where name is the person to tell the message to. "
						+ "Example: '%cday Why do you spend all your time writing interfaces?' will expand out into 'tell cday Why do you spend...'");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("%") && !command.startsWith("%%")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			String firstWord = tok.nextToken().substring(1);

			if (!firstWord.equals("") && !NumberUtils.isDigits(firstWord)) {
				return new RaptorAliasResult("tell " + firstWord + " "
						+ tok.getWhatsLeft(), null);
			}
		}
		return null;
	}
}