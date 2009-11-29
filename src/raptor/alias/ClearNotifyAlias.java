package raptor.alias;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.MessageCallback;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class ClearNotifyAlias extends RaptorAlias {
	public ClearNotifyAlias() {
		super("clear notify", "Removes all of the people in your notify list.",
				"'clear notify' " + "Example: 'clear notify'");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.equals("clear notify")) {
			RaptorAliasResult result = new RaptorAliasResult("=notify", null);
			controller.getConnector().invokeOnNextMatch(
					"\\-\\- notify list\\:.*", new MessageCallback() {

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
											"-notify " + nameTok.nextToken(),
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
																	+ " entries from your notify list."));
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