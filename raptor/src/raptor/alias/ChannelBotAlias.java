package raptor.alias;

import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class ChannelBotAlias extends RaptorAlias {
	public ChannelBotAlias() {
		super(
				"%%###",
				"Expands '%%Channel message' into 'tell channelbot tell Channel msg'",
				"'%%26 message'. " + "Example: '%%25 What does TAD stand for?");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("%%")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			String firstWord = tok.nextToken().substring(2);
			return new RaptorAliasResult("tell channelbot tell " + firstWord
					+ " " + tok.getWhatsLeft(), null);
		} else {
			return null;
		}
	}
}