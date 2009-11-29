package raptor.alias;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.MessageCallback;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class ClearCensorAlias extends RaptorAlias {
	public ClearCensorAlias() {
		super("clear censor",
				"Removes everyone currently in your censor list.",
				"'clear censor' " + "Example: 'clear censor'");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.equals("clear censor")) {
			RaptorAliasResult result = new RaptorAliasResult("=censor", null);
			controller.getConnector().invokeOnNextMatch(
					"\\-\\- censor list\\:.*", new MessageCallback() {

						public boolean matchReceived(ChatEvent event) {
							RaptorStringTokenizer tok = new RaptorStringTokenizer(
									event.getMessage(), "\n", true);
							int censorsRemoved = 0;
							tok.nextToken();
							while (tok.hasMoreTokens()) {
								RaptorStringTokenizer nameTok = new RaptorStringTokenizer(
										tok.nextToken(), " ", true);
								while (nameTok.hasMoreTokens()) {
									controller.getConnector().sendMessage(
											"-censor " + nameTok.nextToken(),
											true);
									censorsRemoved++;
								}
							}

							final int finalItemsRemoved = censorsRemoved;

							Raptor.getInstance().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											controller
													.onAppendChatEventToInputText(new ChatEvent(
															null,
															ChatType.INTERNAL,
															"Removed "
																	+ finalItemsRemoved
																	+ " entries from your censor list."));
										}
									});
							return false;
						}
					});
			return result;
		} else {
			return null;
		}
	}
}