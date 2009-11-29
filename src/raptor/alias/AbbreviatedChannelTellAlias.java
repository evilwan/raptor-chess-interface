package raptor.alias;

import org.apache.commons.lang.math.NumberUtils;

import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class AbbreviatedChannelTellAlias extends RaptorAlias {
	public AbbreviatedChannelTellAlias() {
		super(
				"%###",
				"Expands '%### message' into 'tell channel msg'",
				"'%### message' where ### is a number between 0 and 255. "
						+ "Example: '%37 Why am I here?' will expand out into 'tell 37 Why am I here?'.");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("%") && !command.startsWith("%%")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			String firstWord = tok.nextToken().substring(1);

			if (!firstWord.equals("") && NumberUtils.isDigits(firstWord)) {
				return new RaptorAliasResult("tell " + firstWord + " "
						+ tok.getWhatsLeft(), null);
			}
		}
		return null;
	}

}