package raptor.alias;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.MessageCallback;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class ClearChannelsAlias extends RaptorAlias {
	public ClearChannelsAlias() {
		super("clear channels",
				"Removes all of the channels in your channel list.",
				"'clear channels' " + "Example: 'clear channels'");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.equals("clear channels")) {
			RaptorAliasResult result = new RaptorAliasResult("=chan", null);
			controller.getConnector().invokeOnNextMatch(
					"\\-\\- channel list\\:.*", new MessageCallback() {

						public boolean matchReceived(ChatEvent event) {
							RaptorStringTokenizer tok = new RaptorStringTokenizer(
									event.getMessage(), "\n", true);
							int itemsRemoved = 0;
							tok.nextToken();
							while (tok.hasMoreTokens()) {
								RaptorStringTokenizer nameTok = new RaptorStringTokenizer(
										tok.nextToken(), " ", true);
								while (nameTok.hasMoreTokens()) {
									controller.getConnector().sendMessage(
											"-chan " + nameTok.nextToken(),
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
																	+ " entries from your channel list."));
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