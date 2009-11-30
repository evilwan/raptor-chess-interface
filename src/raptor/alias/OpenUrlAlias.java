package raptor.alias;

import raptor.swt.chat.ChatConsoleController;
import raptor.util.BrowserUtils;
import raptor.util.RaptorStringTokenizer;

public class OpenUrlAlias extends RaptorAlias {
	public OpenUrlAlias() {
		super("openurl", "Opens the specified url.", "'openurl [url]'. "
				+ "Example: 'openurl http://google.com'");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("openurl")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			tok.nextToken();
			String whatsLeft = tok.getWhatsLeft();
			if (whatsLeft != null) {
				BrowserUtils.openUrl(whatsLeft);
				return new RaptorAliasResult(null, "Opened " + whatsLeft);
			}
		}
		return null;
	}
}