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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.MessageCallback;
import raptor.connector.ics.IcsUtils;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorRunnable;
import raptor.util.RaptorStringTokenizer;

public class TellAllInChannelAlias extends RaptorAlias {
	public TellAllInChannelAlias() {
		super(
				"tellall",
				"Sends everyone in the channel a direct tell instead of telling directly to the channel.",
				"'tellall ### message'. Example: 'tellall 24 Partner?' "
						+ "will send 'tell person Partner?' to everyone in channel 24.");
		setHidden(true);
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWith(command, "tellall")) {
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
										new RaptorRunnable(controller
												.getConnector()) {
											@Override
											public void execute() {
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
