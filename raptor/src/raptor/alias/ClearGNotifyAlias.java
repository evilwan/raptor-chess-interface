package raptor.alias;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.MessageCallback;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class ClearGNotifyAlias extends RaptorAlias {
	public ClearGNotifyAlias() {
		super("clear gnotify",
				"Removes all of the entries in your gnotify list.",
				"'clear gnotify' " + "Example: 'clear gnotify'");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.equals("clear gnotify")) {
			RaptorAliasResult result = new RaptorAliasResult("=gnotify", null);
			controller.getConnector().invokeOnNextMatch(
					"\\-\\- gnotify list\\:.*", new MessageCallback() {

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
											"-gnotify " + nameTok.nextToken(),
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
																	+ " entries from your gnotify list."));
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