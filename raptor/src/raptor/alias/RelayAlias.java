package raptor.alias;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.MessageCallback;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class RelayAlias extends RaptorAlias {

	public static String relayPerson;

	public RelayAlias() {
		super(
				"relay",
				"Relays all tells sent to you to either a channel or to a person.",
				"'relay [userName | channel | remove]'. Example: 'relay CDay' "
						+ "afterwards 'relay remove' to stop relaying.");
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.startsWith("relay")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			tok.nextToken();
			final String param = tok.nextToken();

			if (param == null) {
				return new RaptorAliasResult(null, "Invalid syntax: " + command
						+ " \n" + getUsage());
			} else if (param.equalsIgnoreCase("remove")) {
				if (relayPerson != null) {
					RaptorAliasResult result = new RaptorAliasResult("tell "
							+ relayPerson + " I am no longer relaying direct tells.",
							"You will no longer relay tells to " + relayPerson
									+ ".");
					relayPerson = null;
					return result;
				}
			} else {
				if (relayPerson != null) {
					RaptorAliasResult result = new RaptorAliasResult(
							null,
							"You are already relaying tells to "
									+ relayPerson
									+ ". To relay tells to someone else type \"relay remove\" first.");
					return result;
				} else {
					relayPerson = param;
					controller.getConnector().invokeOnNextMatch(
							".* tells you\\: .*", new MessageCallback() {
								public boolean matchReceived(
										final ChatEvent event) {
									if (event.getType() != ChatType.TELL
											|| controller.isDisposed()
											|| relayPerson == null) {
										return false;
									}
									controller.getConnector().sendMessage(
											"tell " + relayPerson + " "
													+ event.getMessage(), true);
									return true;
								}
							});

					return new RaptorAliasResult(
							"tell "
									+ param
									+ " I am now relaying all direct tells I receive to you.",
							"All direct tells sent to you will now be relayed to "
									+ param + ".");
				}
			}
		}
		return null;
	}
}