/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
				"'relay [userName | channel | remove | kill]'. Example: 'relay CDay' "
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
			} else if (param.equalsIgnoreCase("remove")
					|| param.equalsIgnoreCase("kill")) {
				if (relayPerson != null) {
					RaptorAliasResult result = new RaptorAliasResult("tell "
							+ relayPerson
							+ " I am no longer relaying direct tells.",
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