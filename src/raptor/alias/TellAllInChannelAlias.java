package raptor.alias;

import org.apache.commons.lang.math.NumberUtils;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.MessageCallback;
import raptor.connector.ics.IcsUtils;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class TellAllInChannelAlias extends RaptorAlias {
	public TellAllInChannelAlias() {
		super(
				"tellall",
				"Sends everyone in the channel a direct tell instead of telling directly to the channel.",
				"'tellall ### message'. Example: 'tellall 24 Partner?' "
						+ "will send 'tell person Partner?' to everyone in channel 24.");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.startsWith("tellall")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			tok.nextToken();
			final String channel = tok.nextToken();
			final String restOfMessage = tok.getWhatsLeft();

			if (NumberUtils.isDigits(channel)) {
				RaptorAliasResult result = new RaptorAliasResult("in "
						+ channel,
						"Direct tell will be sent. However due to quotas it is throttled and "
								+ "one tell will be sent every 2 seconds.");
				controller.getConnector().invokeOnNextMatch(
						"Channel " + channel + ".*", new MessageCallback() {
							public boolean matchReceived(ChatEvent event) {
								RaptorStringTokenizer tok = new RaptorStringTokenizer(
										event.getMessage(), "\\\n{} ", true);
								tok.nextToken();
								tok.nextToken();

								int itemsRemoved = 0;
								while (tok.hasMoreTokens()) {
									String token = IcsUtils.stripTitles(tok
											.nextToken());

									if (token.startsWith("\"")) {
										continue;
									}

									if (NumberUtils.isDigits(token)) {
										break;
									}

									controller.getConnector().sendMessage(
											"tell " + token + " "
													+ restOfMessage, true);
									itemsRemoved++;
									try {
										Thread.sleep(2000);
									} catch (InterruptedException ie) {
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
																"Direct tell will be sent to "
																		+ finalItemsRemoved
																		+ " people."));
											}
										});
								return false;

							}
						});
				return result;
			}
		}
		return null;
	}
}
