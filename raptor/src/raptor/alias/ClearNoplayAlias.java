package raptor.alias;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.MessageCallback;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class ClearNoplayAlias extends RaptorAlias {
	public ClearNoplayAlias() {
		super("clear noplay", "Removes all of the people in your noplay list.",
				"'clear noplay' " + "Example: 'clear noplay'");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.equals("clear noplay")) {
			RaptorAliasResult result = new RaptorAliasResult("=noplay", null);
			controller.getConnector().invokeOnNextMatch(
					"\\-\\- noplay list\\:.*", new MessageCallback() {

						public void matchReceived(ChatEvent event) {
							RaptorStringTokenizer tok = new RaptorStringTokenizer(
									event.getMessage(), "\n", true);
							int itemsRemoved = 0;
							tok.nextToken();
							while (tok.hasMoreTokens()) {
								RaptorStringTokenizer nameTok = new RaptorStringTokenizer(
										tok.nextToken(), " ", true);
								while (nameTok.hasMoreTokens()) {
									controller.getConnector().sendMessage(
											"-noplay " + nameTok.nextToken(),
											true);
									itemsRemoved++;
								}
							}

							final int finalItemsRemoved = itemsRemoved;

							Raptor.getInstance().getDisplay().asyncExec(
									new Runnable() {
										public void run() {
											controller
													.onAppendChatEventToInputText(new ChatEvent(
															null,
															ChatType.INTERNAL,
															"Removed "
																	+ finalItemsRemoved
																	+ " entries from your noplay list."));
										}
									});
						}
					});
			return result;
		} else {
			return null;
		}
	}
}