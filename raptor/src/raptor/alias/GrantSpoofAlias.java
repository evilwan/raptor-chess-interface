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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.MessageCallback;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorRunnable;
import raptor.util.RaptorStringTokenizer;

public class GrantSpoofAlias extends RaptorAlias {

	public static List<String> usersWithControl = new ArrayList<String>(10);

	public GrantSpoofAlias() {
		super(
				"grantspoof",
				"Allows another user to spoof commands on your account. Use with care! "
						+ "A user could be very disruptive with your account by fooling you into typing this. "
						+ "After you give another user control every tell the user sends you will be "
						+ "executed as a command to the server. If the user sends you a command that "
						+ "starts with / or \\ the command will be ignored."
						+ "To remove the users access type 'grantspoof remove'.",
				"'grantspoof [remove | kill | userName]'. Example: 'grantspoof CDay' "
						+ "afterwards 'givecontrol remove' to remove control.");
		setHidden(true);
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.startsWith("grantspoof")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			tok.nextToken();
			final String param = tok.nextToken();

			if (param == null) {
				return new RaptorAliasResult(null, "Invalid syntax: " + command
						+ " \n" + getUsage());
			} else if (param.equalsIgnoreCase("remove")
					|| param.equalsIgnoreCase("kill")) {
				for (String user : usersWithControl) {
					controller.getConnector().sendMessage(
							"tell " + user
									+ " I have removed your spoof access.");
				}
				usersWithControl.clear();
				return new RaptorAliasResult(null,
						"All spoof access has been removed.");
			} else {
				usersWithControl.add(param);
				controller.getConnector().invokeOnNextMatch(
						param + " tells you\\: .*", new MessageCallback() {
							public boolean matchReceived(final ChatEvent event) {
								if (event.getType() != ChatType.TELL
										|| controller.isDisposed()) {
									return false;
								}
								boolean hasAccess = false;
								for (String user : usersWithControl) {
									if (user
											.equalsIgnoreCase(event.getSource())) {
										hasAccess = true;
										break;
									}
								}
								if (hasAccess) {
									String message = event.getMessage();
									RaptorStringTokenizer messageTok = new RaptorStringTokenizer(
											message, " ", true);
									messageTok.nextToken();
									messageTok.nextToken();
									messageTok.nextToken();

									message = messageTok.getWhatsLeft().trim();
									if (!message.startsWith("/")
											&& !message.startsWith("\\")) {
										final String finalMessage = StringUtils
												.replaceChars(message, "\\\n",
														"");
										Raptor
												.getInstance()
												.getDisplay()
												.asyncExec(
														new RaptorRunnable(
																controller
																		.getConnector()) {
															@Override
															public void execute() {
																controller
																		.onAppendChatEventToInputText(new ChatEvent(
																				null,
																				ChatType.INTERNAL,
																				event
																						.getSource()
																						+ " is spoofing: '"
																						+ finalMessage
																						+ "'. To kill his/her access type 'grantspoof remove'."));
															}
														});
										controller.getConnector().sendMessage(
												finalMessage, true);
									}
									return true;
								} else {
									return false;
								}
							}
						});

				return new RaptorAliasResult(
						"tell "
								+ param
								+ " You now have spoof access to my account. Every tell you send me will be executed "
								+ "as a command. If you send me a tell that starts with / or \\ it will not be exuected as a command.",
						"All tells sent to you from "
								+ param
								+ " will now be executed as commands. You can stop this at any time by "
								+ "typing 'grantspoof remove'. If the user sends you a tell that starts "
								+ "with / or \\ the command will not be executed.");
			}

		}
		return null;
	}
}