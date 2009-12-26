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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.chat.ChatLogger.ChatEventParseListener;
import raptor.service.ThreadService;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorRunnable;

public class ShowTellsAlias extends RaptorAlias {
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat(
			"'['hh:mma']'");

	public ShowTellsAlias() {
		super(
				"=tells",
				"Shows all direct tells, all direct tells sent by a specified person, or all channel tells.",
				"'=tells [personName | channelNumber]'. Example: '=tells' (Shows all direct tells), "
						+ "'=tells johnthegreat' (Shows all direct tells sent by johnthegreat), =tells 24 (Shows all tells in 24)");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		command = command.trim();
		if (StringUtils.startsWith(command,"=tells")) {

			final String whatsLeft = command.length() == 6 ? "" : command
					.substring(7).trim();

			if (whatsLeft.contains(" ")) {
				return new RaptorAliasResult(null, "Invalid command: "
						+ command + ".\n" + getUsage());
			} else if (StringUtils.isBlank(whatsLeft)) {
				ThreadService.getInstance().run(new Runnable() {
					public void run() {
						final StringBuilder builder = new StringBuilder(5000);
						controller.getConnector().getChatService()
								.getChatLogger().parseFile(
										new ChatEventParseListener() {
											public boolean onNewEventParsed(
													ChatEvent event) {
												if (event.getType() == ChatType.TELL) {
													builder
															.append(FORMAT
																	.format(new Date(
																			event
																					.getTime()))
																	+ event
																			.getMessage()
																			.trim()
																	+ "\n");
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
																						"All direct tells sent since you logged in:\n"
																								+ builder
																										.toString()));
																	}
																});
											}
										});

					}
				});
				return new RaptorAliasResult(null,
						"Your request is being processed. This may take a moment");

			} else if (NumberUtils.isDigits(whatsLeft)) {
				ThreadService.getInstance().run(new Runnable() {
					public void run() {
						final StringBuilder builder = new StringBuilder(5000);
						controller.getConnector().getChatService()
								.getChatLogger().parseFile(
										new ChatEventParseListener() {
											public boolean onNewEventParsed(
													ChatEvent event) {
												if (event.getType() == ChatType.CHANNEL_TELL
														&& StringUtils
																.equals(
																		event
																				.getChannel(),
																		whatsLeft)) {
													builder
															.append(FORMAT
																	.format(new Date(
																			event
																					.getTime()))
																	+ event
																			.getMessage()
																			.trim()
																	+ "\n");
												}
												return true;
											}

											public void onParseCompleted() {
												Raptor.getInstance()
														.getDisplay()
														.asyncExec(
																new Runnable() {

																	public void run() {
																		controller
																				.onAppendChatEventToInputText(new ChatEvent(
																						null,
																						ChatType.INTERNAL,
																						"All "
																								+ whatsLeft
																								+ " tells sent since you logged in:\n"
																								+ builder
																										.toString()));
																	}
																});
											}
										});

					}
				});
				return new RaptorAliasResult(null,
						"Your request is being processed. This may take a moment");
			} else {
				ThreadService.getInstance().run(new Runnable() {
					public void run() {
						final StringBuilder builder = new StringBuilder(5000);
						controller.getConnector().getChatService()
								.getChatLogger().parseFile(
										new ChatEventParseListener() {
											public boolean onNewEventParsed(
													ChatEvent event) {
												if (event.getType() == ChatType.TELL
														&& StringUtils
																.startsWithIgnoreCase(
																		event
																				.getSource(),
																		whatsLeft)) {
													builder
															.append(FORMAT
																	.format(new Date(
																			event
																					.getTime()))
																	+ event
																			.getMessage()
																			.trim()
																	+ "\n");
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
																						"All "
																								+ whatsLeft
																								+ " tells sent since you logged in:\n"
																								+ builder
																										.toString()));
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
