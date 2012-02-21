/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.chat.ChatLogger.ChatEventParseListener;
import raptor.service.ThreadService;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorRunnable;
import raptor.util.RegExUtils;

public class ShowRegexAlias extends RaptorAlias {
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat(
			"'['hh:mma']'");

	public ShowRegexAlias() {
		super(
				"=regex",
				"Shows all messages matching the specified regular expression.",
				"'=regex regularExpression'. Example: '=regex .*raptor.*' (Shows all messages containing raptor).");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		command = command.trim();
		if (StringUtils.startsWith(command, "=regex")) {

			final String whatsLeft = command.length() == 7 ? "" : command
					.substring(7).trim();

			if (whatsLeft.contains(" ")) {
				return new RaptorAliasResult(null, "Invalid command: "
						+ command + ".\n" + getUsage());
			} else {
				final Pattern regExPattern = RegExUtils.getPattern(whatsLeft);
				ThreadService.getInstance().run(new Runnable() {
					public void run() {
						final StringBuilder builder = new StringBuilder(5000);
						controller.getConnector().getChatService()
								.getChatLogger().parseFile(
										new ChatEventParseListener() {
											public boolean onNewEventParsed(
													ChatEvent event) {
												if (event.getType() != ChatType.INTERNAL
														&& event.getType() != ChatType.BUGWHO_AVAILABLE_TEAMS
														&& event.getType() != ChatType.BUGWHO_GAMES
														&& event.getType() != ChatType.BUGWHO_UNPARTNERED_BUGGERS
														&& event.getType() != ChatType.SEEKS
														&& event.getType() != ChatType.CHALLENGE
														&& event.getType() != ChatType.OUTBOUND
														&& event.getType() != ChatType.MOVES
														&& event.getType() != ChatType.PLAYING_STATISTICS
														&& event.getType() != ChatType.UNKNOWN
														&& RegExUtils
																.matches(
																		regExPattern,
																		event
																				.getMessage())) {
                                                    builder.append(FORMAT
                                                            .format(new Date(
                                                                    event
                                                                            .getTime()))).append(event
                                                            .getMessage()
                                                            .trim()).append("\n");
												}
												return true;
											}

											public void onParseCompleted() {
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
																						"All messages matching "
																								+ whatsLeft
																								+ " since you logged in:\n"
																								+ builder));
																	}
																});
											}
										});

					}
				});
				return new RaptorAliasResult(null,
						"Your request is being processed. This may take a moment");
			}
		}
		return null;
	}
}
