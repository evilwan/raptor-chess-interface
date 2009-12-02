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
							return false;
						}
					});
			return result;
		} else {
			return null;
		}
	}
}