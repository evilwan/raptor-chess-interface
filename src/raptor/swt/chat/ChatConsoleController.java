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
package raptor.swt.chat;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.action.ActionUtils;
import raptor.action.RaptorAction;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.Connector;
import raptor.connector.ConnectorListener;
import raptor.connector.fics.FicsConnector;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.script.ChatScript;
import raptor.script.ChatScript.ChatScriptType;
import raptor.service.ActionService;
import raptor.service.ScriptService;
import raptor.service.SoundService;
import raptor.service.ChatService.ChatListener;
import raptor.swt.ItemChangedListener;
import raptor.swt.SWTUtils;
import raptor.swt.chat.controller.ChannelController;
import raptor.swt.chat.controller.PersonController;
import raptor.swt.chat.controller.ToolBarItemKey;
import raptor.util.BrowserUtils;
import raptor.util.RaptorStringUtils;

public abstract class ChatConsoleController implements PreferenceKeys {
	public static final double CLEAN_PERCENTAGE = .33;
	private static final Log LOG = LogFactory
			.getLog(ChatConsoleController.class);
	public static final int TEXT_CHUNK_SIZE = 1000;

	protected List<ChatEvent> awayList = new ArrayList<ChatEvent>(100);
	protected ChatConsole chatConsole;
	protected ChatListener chatServiceListener = new ChatListener() {
		public void chatEventOccured(final ChatEvent event) {
			if (!isDisposed && chatConsole != null && !chatConsole.isDisposed()) {
				chatConsole.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							onChatEvent(event);
						} catch (Throwable t) {
							connector.onError("onChatEvent", t);
						}
					}
				});
			} else {
				eventsWhileBeingReparented.add(event);
			}

		}
	};
	protected Connector connector;

	protected ConnectorListener connectorListener = new ConnectorListener() {
		public void onConnect() {
			fireItemChanged();
		}

		public void onConnecting() {
			fireItemChanged();
		}

		public void onDisconnect() {
			fireItemChanged();
		}
	};

	protected SelectionListener verticalScrollbarListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			smartScroll();
		}

	};

	protected KeyListener consoleInputTextKeyListener = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent event) {
			processKeystroke(event);
		}
	};

	protected KeyListener consoleOutputTextKeyListener = new KeyAdapter() {

		@Override
		public void keyReleased(KeyEvent event) {
			processKeystroke(event);
		}
	};
	protected List<ChatEvent> eventsWhileBeingReparented = Collections
			.synchronizedList(new ArrayList<ChatEvent>(100));
	protected boolean hasUnseenText;
	protected boolean ignoreAwayList;
	protected boolean isActive;
	protected MouseListener inputTextClickListener = new MouseAdapter() {

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			onInputTextDoubleClick(e);
		}

		@Override
		public void mouseUp(MouseEvent e) {
			if (e.button == 3) {
				onInputTextRightClick(e);
			}
		}

	};

	protected MouseListener outputTextClickListener = new MouseAdapter() {
		@Override
		public void mouseUp(MouseEvent e) {
			if (e.button == 3) {
				onOutputTextRightClick(e);
			}
		}
	};

	protected boolean isDisposed;
	protected boolean isDirty;
	protected boolean isSoundDisabled = false;
	protected boolean isAutoScrolling = true;
	protected List<ItemChangedListener> itemChangedListeners = new ArrayList<ItemChangedListener>(
			5);
	protected List<String> sentText = new ArrayList<String>(50);

	protected int sentTextIndex = 0;

	protected String sourceOfLastTellReceived;

	protected ToolBar toolbar;

	protected Map<ToolBarItemKey, ToolItem> toolItemMap = new HashMap<ToolBarItemKey, ToolItem>();

	public ChatConsoleController(Connector connector) {
		this.connector = connector;
		connector.addConnectorListener(connectorListener);
	}

	public void addItemChangedListener(ItemChangedListener listener) {
		itemChangedListeners.add(listener);
	}

	public void addScrollBarListeners() {
		chatConsole.getInputText().getVerticalBar().addSelectionListener(
				verticalScrollbarListener);
	}

	public void addToolItem(ToolBarItemKey key, ToolItem item) {
		toolItemMap.put(key, item);
	}

	public boolean confirmClose() {
		return true;
	}

	public void dispose() {
		isDisposed = true;
		if (connector != null) {
			connector.getChatService().removeChatServiceListener(
					chatServiceListener);
			connectorListener = null;
			connector = null;
		}

		if (toolbar != null) {
			toolbar.setVisible(false);
			// DO NOT dispose the toolbar. It causes all kinds of issues in
			// RaptorWindow.
			SWTUtils.clearToolbar(toolbar);
			toolbar = null;
		}

		removeListenersTiedToChatConsole();

		if (itemChangedListeners != null) {
			itemChangedListeners.clear();
			itemChangedListeners = null;
		}

		if (awayList != null) {
			awayList.clear();
			awayList = null;
		}

		if (eventsWhileBeingReparented != null) {
			eventsWhileBeingReparented.clear();
			eventsWhileBeingReparented = null;
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Disposed ChatConsoleController");
		}
	}

	/**
	 * A method that allows subclasses to filter the text before it is appended
	 * to the chatConsole.inputText. The default implementation just returns
	 * what is passed in.
	 * 
	 * @param text
	 *            The incomming text
	 * @return The text to append.
	 */
	public String filterText(String text) {
		return text;
	}

	public ChatConsole getChatConsole() {
		return chatConsole;
	}

	public Connector getConnector() {
		return connector;
	}

	/**
	 * Returns an Image icon that can be used to represent this controller.
	 */
	public Image getIconImage() {
		if (!isActive && hasUnseenText) {
			return Raptor.getInstance().getIcon("chat2");
		} else {
			return null;
		}
	}

	public List<ItemChangedListener> getItemChangedListeners() {
		return itemChangedListeners;
	}

	public abstract String getName();

	public RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	public abstract Quadrant getPreferredQuadrant();

	public String getPrependText(boolean useButtonState) {
		return "";
	}

	public abstract String getPrompt();

	public String getSourceOfLastTellReceived() {
		return sourceOfLastTellReceived;
	}

	/**
	 * Returns the title. THe current format is connector.shortName([CONNECTOR
	 * STATUS IF NOT CONNECTED]getName()).
	 */
	public String getTitle() {
		if (connector == null) {
			return "Error";
		} else if (connector.isConnecting()) {
			return connector.getShortName() + "(Connecting-" + getName() + ")";
		} else if (connector.isConnected()) {
			return connector.getShortName() + "(" + getName() + ")";
		} else {
			return connector.getShortName() + "(Disconnected-" + getName()
					+ ")";
		}
	}

	public abstract Control getToolbar(Composite parent);

	public ToolItem getToolItem(ToolBarItemKey key) {
		return toolItemMap.get(key);
	}

	public boolean hasUnseenText() {
		return hasUnseenText;
	}

	public void init() {
		addScrollBarListeners();
		addInputTextKeyListeners();
		addMouseListeners();
		registerForChatEvents();
		adjustAwayButtonEnabled();
		chatConsole.getOutputText().setText(getPrependText(false));
		setCaretToOutputTextEnd();
	}

	public abstract boolean isAcceptingChatEvent(ChatEvent inboundEvent);

	public boolean isAutoScrolling() {
		return isAutoScrolling;
	}

	public abstract boolean isAwayable();

	public abstract boolean isCloseable();

	public boolean isIgnoringActions() {
		boolean result = false;
		if (isDisposed || chatConsole == null || chatConsole.isDisposed()) {
			LOG
					.debug(
							"isBeingReparented invoked. The exception is thrown just to debug the stack trace.",
							new Exception());
			result = true;
		}
		return result;
	}

	public abstract boolean isPrependable();

	public abstract boolean isSearchable();

	public boolean isSoundDisabled() {
		return isSoundDisabled;
	}

	public boolean isToolItemSelected(ToolBarItemKey key) {
		boolean result = false;
		ToolItem item = getToolItem(key);
		if (item != null) {
			return item.getSelection();
		}
		return result;
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			hasUnseenText = false;
			fireItemChanged();
			chatConsole.getDisplay().timerExec(100, new Runnable() {
				public void run() {

					onForceAutoScroll();
				}
			});
		}
	}

	public void onAppendChatEventToInputText(ChatEvent event) {
		if (!ignoreAwayList && event.getType() == ChatType.TELL
				|| event.getType() == ChatType.PARTNER_TELL) {
			awayList.add(event);
			adjustAwayButtonEnabled();
		}

		String appendText = null;
		int startIndex = 0;

		// synchronize on chatConsole so the scrolling will be handled
		// appropriately if there are multiple events being
		// published at the same time.
		synchronized (chatConsole) {
			if (chatConsole.isDisposed()) {
				return;
			}

			String messageText = filterText(event.getMessage());
			String date = "";
			if (Raptor.getInstance().getPreferences().getBoolean(
					CHAT_TIMESTAMP_CONSOLE)) {
				SimpleDateFormat format = new SimpleDateFormat(Raptor
						.getInstance().getPreferences().getString(
								CHAT_TIMESTAMP_CONSOLE_FORMAT));
				date = format.format(new Date(event.getTime()));
			} else {
				messageText = RaptorStringUtils
						.removeBeginingNewlines(messageText);
			}
			// There use to be an else {messageText = messateText.trim();} here.
			// It was removed to fix Issue 48.
			// This comment is here just in case someone ever adds it back.

			appendText = (chatConsole.inputText.getCharCount() == 0 ? "" : "\n")
					+ date + messageText;

			chatConsole.inputText.append(appendText);
			startIndex = chatConsole.inputText.getCharCount()
					- appendText.length();

			if (isAutoScrolling) {
				onForceAutoScroll();
			}
		}

		onDecorateInputText(event, appendText, startIndex);
		reduceInputTextIfNeeded();
	}

	public void onAppendOutputText(String string) {
		chatConsole.outputText.append(string);
	}

	public void onAway() {
		if (isAwayable()) {
			ignoreAwayList = true;
			onAppendChatEventToInputText(new ChatEvent(null, ChatType.OUTBOUND,
					"Direct tells you missed while you were away:"));
			for (ChatEvent event : awayList) {
				onAppendChatEventToInputText(event);
			}
			awayList.clear();
			ignoreAwayList = false;
			adjustAwayButtonEnabled();
		}
	}

	public void onChatEvent(ChatEvent event) {

		if (event.getType() == ChatType.TELL) {
			sourceOfLastTellReceived = event.getSource();
		}

		if (isAcceptingChatEvent(event)) {
			onAppendChatEventToInputText(event);
			if (!isIgnoringActions()) {
				playSounds(event);
				updateImageIcon(event);
			}
		}
	}

	public void onForceAutoScroll() {
		if (isIgnoringActions()) {
			return;
		}

		chatConsole.inputText.setCaretOffset(chatConsole.inputText
				.getCharCount());
		chatConsole.inputText.setSelection(new Point(chatConsole.inputText
				.getCharCount(), chatConsole.inputText.getCharCount()));
	}

	public void onPassivate() {
		if (isActive) {
			isActive = false;
			hasUnseenText = false;
		}
	}

	public void onSave() {
		if (isIgnoringActions()) {
			return;
		}
		FileDialog fd = new FileDialog(chatConsole.getShell(), SWT.SAVE);
		fd.setText("Save Console Output.");
		fd.setFilterPath("");
		String[] filterExt = { "*.txt", "*.*" };
		fd.setFilterExtensions(filterExt);
		final String selected = fd.open();

		if (selected != null) {
			chatConsole.getDisplay().asyncExec(new Runnable() {
				public void run() {
					FileWriter writer = null;
					try {
						writer = new FileWriter(selected);
						writer.append("Raptor console log created on "
								+ new Date() + "\n");
						int i = 0;
						while (i < chatConsole.getInputText().getCharCount() - 1) {
							int endIndex = i + TEXT_CHUNK_SIZE;
							if (endIndex >= chatConsole.getInputText()
									.getCharCount()) {
								endIndex = i
										+ chatConsole.getInputText()
												.getCharCount() - i - 1;
							}
							String string = chatConsole.getInputText().getText(
									i, endIndex);
							writer.append(string);
							i = endIndex;
						}
						writer.flush();
					} catch (Throwable t) {
						LOG.error("Error writing file: " + selected, t);
					} finally {
						if (writer != null) {
							try {
								writer.close();
							} catch (IOException ioe) {
							}
						}
					}
				}
			});
		}
	}

	public void onSearch() {
		if (!isIgnoringActions()) {

			chatConsole.getDisplay().asyncExec(new Runnable() {
				public void run() {
					String searchString = chatConsole.outputText.getText();
					if (StringUtils.isBlank(searchString)) {
						MessageBox box = new MessageBox(chatConsole.getShell(),
								SWT.ICON_INFORMATION | SWT.OK);
						box
								.setMessage("You must enter text in the input field to search on.");
						box.setText("Alert");
						box.open();
					} else {
						boolean foundText = false;
						searchString = searchString.toUpperCase();
						int start = chatConsole.inputText.getCaretOffset();

						if (start >= chatConsole.inputText.getCharCount()) {
							start = chatConsole.inputText.getCharCount() - 1;
						} else if (start - searchString.length() + 1 >= 0) {
							String text = chatConsole.inputText.getText(start
									- searchString.length(), start - 1);
							if (text.equalsIgnoreCase(searchString)) {
								start -= searchString.length();
							}
						}

						while (start > 0) {
							int charsBack = 0;
							if (start - TEXT_CHUNK_SIZE > 0) {
								charsBack = TEXT_CHUNK_SIZE;
							} else {
								charsBack = start;
							}

							String stringToSearch = chatConsole.inputText
									.getText(start - charsBack, start)
									.toUpperCase();
							int index = stringToSearch
									.lastIndexOf(searchString);
							if (index != -1) {
								int textStart = start - charsBack + index;
								chatConsole.inputText.setSelection(textStart,
										textStart + searchString.length());
								foundText = true;
								break;
							}
							start -= charsBack;
						}

						if (!foundText) {
							MessageBox box = new MessageBox(chatConsole
									.getShell(), SWT.ICON_INFORMATION | SWT.OK);
							box.setMessage("Could not find any occurances of '"
									+ searchString + "'.");
							box.setText("Alert");
							box.open();
						}
					}
				}
			});
		}
	}

	public void onSendOutputText() {
		connector.sendMessage(chatConsole.outputText.getText());
		chatConsole.outputText.setText(getPrependText(true));
		setCaretToOutputTextEnd();
		awayList.clear();
		adjustAwayButtonEnabled();
	}

	public void processKeystroke(KeyEvent event) {
		boolean isConsoleOutputText = event.widget == chatConsole.outputText;

		if (ActionUtils.isValidModifier(event.stateMask)) {
			RaptorAction action = ActionService.getInstance().getAction(
					event.stateMask, event.keyCode);
			if (action != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Executing action from keybinding: "
							+ action.getName());
				}
				action.setChatConsoleControllerSource(this);
				action.run();

			}
		} else if (ActionUtils.isValidKeyCodeWithoutModifier(event.keyCode)) {
			RaptorAction action = ActionService.getInstance().getAction(
					event.stateMask, event.keyCode);
			if (action != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Executing action from keybinding: "
							+ action.getName());
				}
				action.setChatConsoleControllerSource(this);
				action.run();

			}
		} else if (event.keyCode == SWT.PAGE_DOWN
				|| event.keyCode == SWT.PAGE_UP) {
			smartScroll();
		} else if (event.keyCode == SWT.ARROW_UP) {
			if (sentTextIndex >= 0) {
				if (sentTextIndex > 0) {
					sentTextIndex--;
				}
				if (!sentText.isEmpty()) {
					chatConsole.outputText.setText("");
					chatConsole.outputText.setText(sentText.get(sentTextIndex));
				}
			}
		} else if (event.keyCode == SWT.ARROW_DOWN) {
			if (sentTextIndex < sentText.size() - 1) {
				sentTextIndex++;
				chatConsole.outputText.setText("");
				chatConsole.outputText.setText(sentText.get(sentTextIndex));
			} else {
				chatConsole.outputText.setText("");
			}
		} else if (event.character == '\r') {
			if (sentText.size() > 50) {
				sentText.remove(0);
			}
			sentText.add(chatConsole.outputText.getText().substring(0,
					chatConsole.outputText.getText().length()));
			sentTextIndex = sentText.size();

			onSendOutputText();

			if (!isConsoleOutputText) {
				chatConsole.getDisplay().asyncExec(new Runnable() {
					public void run() {
						chatConsole.outputText.forceFocus();
					}
				});
			}
		} else if (event.stateMask != 0) {
			// eat it.
			// This prevents control c, control v from getting forwarded.
		} else if (!isConsoleOutputText && event.character == '\b') {
			if (chatConsole.outputText.getCharCount() > 0) {
				chatConsole.outputText.setText(chatConsole.outputText.getText()
						.substring(0,
								chatConsole.outputText.getText().length() - 1));
			}
		} else if (!isConsoleOutputText
				&& ChatUtils.FORWARD_CHAR.indexOf(event.character) != -1) {
			onAppendOutputText("" + event.character);
		} else if (!isConsoleOutputText) {
			chatConsole.getDisplay().asyncExec(new Runnable() {
				public void run() {
					chatConsole.outputText.forceFocus();
					chatConsole.outputText.setSelection(chatConsole.outputText
							.getCharCount());
				}
			});
		}
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
		itemChangedListeners.remove(listener);
	}

	public void setAutoScrolling(boolean isAutoScrolling) {
		if (isAutoScrolling != this.isAutoScrolling) {
			this.isAutoScrolling = isAutoScrolling;
			if (isAutoScrolling) {
				ToolItem item = toolItemMap
						.get(ToolBarItemKey.AUTO_SCROLL_BUTTON);
				if (item != null) {
					item.setSelection(true);
					item.setImage(Raptor.getInstance().getIcon("locked"));
					item.setToolTipText("Scroll lock enabled.");
				}
				onForceAutoScroll();
			} else {
				ToolItem item = toolItemMap
						.get(ToolBarItemKey.AUTO_SCROLL_BUTTON);
				if (item != null) {
					item.setSelection(false);
					item.setImage(Raptor.getInstance().getIcon("unlocked"));
					item.setToolTipText("Scroll lock disabled.");
				}
			}
		}
	}

	public void setChatConsole(ChatConsole chatConsole) {
		this.chatConsole = chatConsole;
	}

	public void setHasUnseenText(boolean hasUnseenText) {
		this.hasUnseenText = hasUnseenText;
	}

	public void setInputToLastTell() {
		if (sourceOfLastTellReceived != null) {
			chatConsole.outputText.setText(connector
					.getTellToString(sourceOfLastTellReceived));
			chatConsole.outputText.setSelection(chatConsole.outputText
					.getCharCount() + 1);
		}
	}

	public void setItemChangedListeners(
			List<ItemChangedListener> itemChangedListeners) {
		this.itemChangedListeners = itemChangedListeners;
	}

	public void setSoundDisabled(boolean isSoundDisabled) {
		this.isSoundDisabled = isSoundDisabled;
	}

	public void setSourceOfLastTellReceived(String sourceOfLastTellReceived) {
		this.sourceOfLastTellReceived = sourceOfLastTellReceived;
	}

	public void setToolItemEnabled(ToolBarItemKey key, boolean isEnabled) {
		ToolItem item = getToolItem(key);
		if (item != null) {
			item.setEnabled(isEnabled);
		}
	}

	public void setToolItemSelected(ToolBarItemKey key, boolean isSelected) {
		ToolItem item = getToolItem(key);
		if (item != null) {
			item.setSelection(isSelected);
		}
	}

	protected void addChannelMenuItems(Menu menu, String word) {
		if (connector.isLikelyChannel(word)) {
			if (menu.getItemCount() > 0) {
				new MenuItem(menu, SWT.SEPARATOR);
			}
			final String channel = connector.parseChannel(word);

			MenuItem item = null;
			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Add a tab for channel: " + channel);
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					if (!Raptor.getInstance().getWindow().containsChannelItem(
							connector, channel)) {
						ChatConsoleWindowItem windowItem = new ChatConsoleWindowItem(
								new ChannelController(connector, channel));
						Raptor.getInstance().getWindow().addRaptorWindowItem(
								windowItem, false);
						ChatUtils
								.appendPreviousChatsToController(windowItem.console);
					}
				}
			});

			final String[][] connectorChannelItems = connector
					.getChannelActions(channel);
			if (connectorChannelItems != null) {
				for (int i = 0; i < connectorChannelItems.length; i++) {
					item = new MenuItem(menu, SWT.PUSH);
					item.setText(connectorChannelItems[i][0]);
					final int index = i;
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							connector
									.sendMessage(connectorChannelItems[index][1]);
						}
					});
				}
			}
		}
	}

	protected void addCommandMenuItems(Menu menu, final String message) {
		if (message != null && message.length() <= 200) {
			if (menu.getItemCount() > 0) {
				new MenuItem(menu, SWT.SEPARATOR);
			}

			if (message.startsWith("http")) {
				MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
				menuItem.setText("Open in external browser: '" + message + "'");
				menuItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						BrowserUtils.openExternalUrl(message);
					}
				});
			}

			ChatScript[] scripts = ScriptService.getInstance().getChatScripts(
					getConnector(), ChatScriptType.RightClickOneShot);

			for (final ChatScript script : scripts) {
				MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
				menuItem.setText(script.getName() + ": '" + message + "'");
				menuItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						script.execute(connector.getChatScriptContext(message));
					}
				});
			}
		}
	}

	protected void addGameIdMenuItems(Menu menu, String word) {
		if (connector.isLikelyGameId(word)) {
			if (menu.getItemCount() > 0) {
				new MenuItem(menu, SWT.SEPARATOR);
			}
			MenuItem item = null;
			String gameId = connector.parseGameId(word);

			final String[][] gameIdItems = connector.getGameIdActions(gameId);
			if (gameIdItems != null) {
				for (int i = 0; i < gameIdItems.length; i++) {
					item = new MenuItem(menu, SWT.PUSH);
					item.setText(gameIdItems[i][0]);
					final int index = i;
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							connector.sendMessage(gameIdItems[index][1]);
						}
					});
				}
			}
		}
	}

	protected void addInputTextKeyListeners() {
		if (!isIgnoringActions()) {
			chatConsole.outputText.addKeyListener(consoleOutputTextKeyListener);
			chatConsole.inputText.addKeyListener(consoleInputTextKeyListener);
		}
	}

	protected void addMouseListeners() {
		if (!isIgnoringActions()) {
			chatConsole.inputText.addMouseListener(inputTextClickListener);
			chatConsole.outputText.addMouseListener(outputTextClickListener);
		}
	}

	protected void addPersonMenuItems(Menu menu, String word) {
		if (connector.isLikelyPerson(word)) {
			if (menu.getItemCount() > 0) {
				new MenuItem(menu, SWT.SEPARATOR);
			}
			final String person = connector.parsePerson(word);
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText("Add a tab for person: " + person);
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					if (!Raptor.getInstance().getWindow()
							.containsPersonalTellItem(connector, person)) {
						ChatConsoleWindowItem windowItem = new ChatConsoleWindowItem(
								new PersonController(connector, person));
						Raptor.getInstance().getWindow().addRaptorWindowItem(
								windowItem, false);
						ChatUtils
								.appendPreviousChatsToController(windowItem.console);
					}
				}
			});

			if (connector instanceof FicsConnector) {

				MenuItem ficsGamesHistory = new MenuItem(menu, SWT.PUSH);
				ficsGamesHistory.setText("ficsgames.com History for person: "
						+ person);
				ficsGamesHistory.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						BrowserUtils
								.openUrl("http://www.ficsgames.com/cgi-bin/search.cgi?player="
										+ person + "&showhistory=showhistory");
					}
				});

				MenuItem ficsGamesStats = new MenuItem(menu, SWT.PUSH);
				ficsGamesStats.setText("ficsgames.com Stats for person: "
						+ person);
				ficsGamesStats.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						BrowserUtils
								.openUrl("http://www.ficsgames.com/cgi-bin/search.cgi?player="
										+ person + "&showstats=showstats");
					}
				});

				MenuItem watchBotStats = new MenuItem(menu, SWT.PUSH);
				watchBotStats.setText("WatchBot history for person: " + person);
				watchBotStats.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						BrowserUtils.openHtml(BrowserUtils
								.getWatchBotJavascript(person));
					}
				});

				new MenuItem(menu, SWT.SEPARATOR);
			}

			final String[][] connectorPersonItems = connector
					.getPersonActions(person);
			if (connectorPersonItems != null) {
				for (int i = 0; i < connectorPersonItems.length; i++) {
					item = new MenuItem(menu, SWT.PUSH);
					item.setText(connectorPersonItems[i][0]);
					final int index = i;
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							connector
									.sendMessage(connectorPersonItems[index][1]);
						}
					});
				}
			}
		}
	}

	protected void adjustAwayButtonEnabled() {
		setToolItemEnabled(ToolBarItemKey.AWAY_BUTTON, !awayList.isEmpty());
	}

	protected void decorateForegroundColor(ChatEvent event, String message,
			int textStartPosition) {
		Color color = getPreferences().getColor(event);
		if (color == null) {
			color = chatConsole.inputText.getForeground();
		}

		String prompt = connector.getPrompt();
		if (message.endsWith(prompt)) {
			message = message.substring(0, message.length() - prompt.length());
		}

		chatConsole.inputText
				.setStyleRange(new StyleRange(textStartPosition, message
						.length(), color, chatConsole.inputText.getBackground()));
	}

	protected void decorateLinks(ChatEvent event, String message,
			int textStartPosition) {
		if (event.getType() != ChatType.OUTBOUND) {
			List<int[]> linkRanges = new ArrayList<int[]>(5);

			// First check http://,https://,www.
			int startIndex = message.indexOf("http://");
			if (startIndex == -1) {
				startIndex = message.indexOf("https://");
				if (startIndex == -1) {
					startIndex = message.indexOf("www.");
				}
			}
			while (startIndex != -1 && startIndex < message.length()) {
				int endIndex = startIndex + 1;
				while (endIndex < message.length()) {

					// On ICS servers line breaks follow the convention \n\\
					// This code underlines links that have line breaks in
					// them.
					if (message.charAt(endIndex) == '\n'
							&& message.length() > endIndex + 1
							&& message.charAt(endIndex + 1) == '\\') {
						endIndex += 2;

						// Move past the white space and then continue on
						// with
						// the
						// main loop.
						while (endIndex < message.length()
								&& Character.isWhitespace(message
										.charAt(endIndex))) {
							endIndex++;
						}
						continue;
					} else if (Character.isWhitespace(message.charAt(endIndex))) {
						break;
					}
					endIndex++;
				}

				if (message.charAt(endIndex - 1) == '.') {
					endIndex--;
				}

				linkRanges.add(new int[] { startIndex, endIndex });

				startIndex = message.indexOf("http://", endIndex + 1);
				if (startIndex == -1) {
					startIndex = message.indexOf("https://", endIndex + 1);
					if (startIndex == -1) {
						startIndex = message.indexOf("www.", endIndex + 1);
					}
				}
			}

			// Next check ending with .com,.org,.edu
			int endIndex = message.indexOf(".com");
			if (endIndex == -1 || isInRanges(startIndex, linkRanges)) {
				endIndex = message.indexOf(".org");
				if (endIndex == -1 || isInRanges(startIndex, linkRanges)) {
					endIndex = message.indexOf(".edu");
				}
			}
			if (endIndex != -1 && isInRanges(startIndex, linkRanges)) {
				endIndex = -1;
			}
			int linkEnd = endIndex + 4;
			while (endIndex != -1) {
				startIndex = endIndex--;
				while (startIndex >= 0) {
					// On ICS servers line breaks follow the convention \n\\
					// This code underlines links that have line breaks in
					// them.
					if (Character.isWhitespace(message.charAt(startIndex))) {
						break;
					}
					startIndex--;
				}

				// Filter out emails.
				int atIndex = message.indexOf("@", startIndex);
				if (atIndex == -1 || atIndex > linkEnd) {
					linkRanges.add(new int[] { startIndex + 1, linkEnd });
				}

				endIndex = message.indexOf(".com", linkEnd + 1);
				if (endIndex == -1 || isInRanges(startIndex, linkRanges)) {
					endIndex = message.indexOf(".org", linkEnd + 1);
					if (endIndex == -1 || isInRanges(startIndex, linkRanges)) {
						endIndex = message.indexOf(".com", linkEnd + 1);
					}
				}
				if (endIndex != -1 && isInRanges(startIndex, linkRanges)) {
					endIndex = -1;
				}
				linkEnd = endIndex + 4;
			}

			// add all the ranges that were found.
			for (int[] linkRange : linkRanges) {
				Color underlineColor = chatConsole.getPreferences().getColor(
						CHAT_LINK_UNDERLINE_COLOR);
				StyleRange range = new StyleRange(textStartPosition
						+ linkRange[0], linkRange[1] - linkRange[0],
						underlineColor, chatConsole.inputText.getBackground());
				range.underline = true;
				chatConsole.inputText.setStyleRange(range);
			}
		}
	}

	protected void decorateQuotes(ChatEvent event, String message,
			int textStartPosition) {
		if (event.getType() != ChatType.OUTBOUND) {
			boolean isUnderliningSingleQuotes = getPreferences().getBoolean(
					CHAT_UNDERLINE_SINGLE_QUOTES);
			List<int[]> quotedRanges = new ArrayList<int[]>(5);

			int quoteIndex = message.indexOf("\"");
			if (quoteIndex == -1 && isUnderliningSingleQuotes) {
				quoteIndex = message.indexOf("'");
			}

			while (quoteIndex != -1) {
				int endQuote = message.indexOf("\"", quoteIndex + 1);
				if (endQuote == -1 && isUnderliningSingleQuotes) {
					endQuote = message.indexOf("'", quoteIndex + 1);
				}

				if (endQuote == -1) {
					break;
				} else {
					if (quoteIndex + 1 != endQuote) {

						// If there is a newline between the quotes ignore
						// it.
						int newLine = message.indexOf("\n", quoteIndex);

						// If there is just one character and the a space
						// after
						// the
						// first quote ignore it.
						boolean isASpaceTwoCharsAfterQuote = message
								.charAt(quoteIndex + 2) == ' ';

						// If the quotes dont match ignore it.
						boolean doQuotesMatch = message.charAt(quoteIndex) == message
								.charAt(endQuote);

						if (!(newLine > quoteIndex && newLine < endQuote)
								&& !isASpaceTwoCharsAfterQuote && doQuotesMatch) {
							quotedRanges.add(new int[] { quoteIndex + 1,
									endQuote });

						}
					}
				}

				quoteIndex = message.indexOf("\"", endQuote + 1);
				if (quoteIndex == -1 && isUnderliningSingleQuotes) {
					quoteIndex = message.indexOf("'", endQuote + 1);
				}
			}

			for (int[] quotedRange : quotedRanges) {
				Color underlineColor = chatConsole.getPreferences().getColor(
						CHAT_QUOTE_UNDERLINE_COLOR);
				StyleRange range = new StyleRange(textStartPosition
						+ quotedRange[0], quotedRange[1] - quotedRange[0],
						underlineColor, chatConsole.inputText.getBackground());
				range.underline = true;
				chatConsole.inputText.setStyleRange(range);
			}
		}
	}

	protected void decoreateNext(ChatEvent event, String message,
			int textStartPosition) {
		if (event.getType() != ChatType.OUTBOUND) {
			List<int[]> nextRanges = new ArrayList<int[]>(5);

			int nextIndex = message.indexOf("[next]");
			while (nextIndex != -1) {
				nextRanges.add(new int[] { nextIndex, nextIndex + 6 });
				nextIndex = message.indexOf("[next]", nextIndex + 1);
			}

			for (int[] nextRange : nextRanges) {
				Color underlineColor = chatConsole.getPreferences().getColor(
						CHAT_QUOTE_UNDERLINE_COLOR);
				StyleRange range = new StyleRange(textStartPosition
						+ nextRange[0], nextRange[1] - nextRange[0],
						underlineColor, chatConsole.inputText.getBackground());
				range.underline = true;
				chatConsole.inputText.setStyleRange(range);
			}
		}
	}

	/**
	 * Should be invoked when the title or closeability changes.
	 */
	protected void fireItemChanged() {
		if (!isDisposed) {
			for (ItemChangedListener listener : itemChangedListeners) {
				listener.itemStateChanged();
			}
		}
	}

	protected boolean isInRanges(int location, List<int[]> ranges) {
		boolean result = false;
		for (int[] range : ranges) {
			if (location >= range[0] && location <= range[1]) {
				result = true;
				break;
			}
		}
		return result;
	}

	protected void onDecorateInputText(final ChatEvent event,
			final String message, final int textStartPosition) {

		decorateForegroundColor(event, message, textStartPosition);
		decorateQuotes(event, message, textStartPosition);
		decorateLinks(event, message, textStartPosition);
		decoreateNext(event, message, textStartPosition);

	}

	protected void onInputTextDoubleClick(MouseEvent e) {
		int caretPosition = chatConsole.inputText.getCaretOffset();

		if (chatConsole.inputText.getSelectionText().equals("next")) {
			connector.sendMessage("next", true);
			return;
		}

		String url = ChatUtils.getUrl(chatConsole.inputText, caretPosition);

		if (StringUtils.isNotBlank(url)) {
			BrowserUtils.openUrl(url);
			return;
		}

		String quotedText = ChatUtils.getQuotedText(chatConsole.inputText,
				caretPosition);
		if (StringUtils.isNotBlank(quotedText)) {
			url = ChatUtils.getUrl(quotedText);
			if (url == null) {
				connector.sendMessage(quotedText);
				onForceAutoScroll();
			} else {
				BrowserUtils.openUrl(url);
			}
		}
	}

	protected void onInputTextRightClick(MouseEvent e) {
		int caretPosition = 0;
		try {
			caretPosition = chatConsole.inputText
					.getOffsetAtLocation(new Point(e.x, e.y));
		} catch (IllegalArgumentException iae) {
			return;
		}

		String word = chatConsole.inputText.getSelectionText();
		boolean wasSelectedText = true;

		if (StringUtils.isBlank(word)) {
			word = ChatUtils.getWord(chatConsole.inputText, caretPosition);
			wasSelectedText = false;
		} else {
			word = connector.removeLineBreaks(word);
		}

		Menu menu = new Menu(chatConsole.getShell(), SWT.POP_UP);
		if (wasSelectedText) {
			MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
			copyItem.setText("copy");
			copyItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					chatConsole.inputText.copy();
				}
			});
		}
		addCommandMenuItems(menu, word);
		addPersonMenuItems(menu, word);
		addChannelMenuItems(menu, word);
		addGameIdMenuItems(menu, word);

		if (menu.getItemCount() > 0) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Showing popup with " + menu.getItemCount()
						+ " items. "
						+ chatConsole.inputText.toDisplay(e.x, e.y));
			}
			menu.setLocation(chatConsole.inputText.toDisplay(e.x, e.y));
			menu.setVisible(true);
			while (!menu.isDisposed() && menu.isVisible()) {
				if (!chatConsole.getDisplay().readAndDispatch()) {
					chatConsole.getDisplay().sleep();
				}
			}
		}
		menu.dispose();
	}

	protected void onOutputTextRightClick(MouseEvent e) {
		String word = chatConsole.outputText.getSelectionText();
		boolean wasSelectedText = true;

		if (StringUtils.isBlank(word)) {
			wasSelectedText = false;
		}

		Menu menu = new Menu(chatConsole.getShell(), SWT.POP_UP);
		if (wasSelectedText) {
			MenuItem copyItem = new MenuItem(menu, SWT.POP_UP);
			copyItem.setText("copy");
			copyItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					chatConsole.outputText.copy();
				}
			});
		}

		MenuItem pasteItem = new MenuItem(menu, SWT.POP_UP);
		pasteItem.setText("paste");
		pasteItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				chatConsole.outputText.paste();
			}
		});

		if (menu.getItemCount() > 0) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Showing popup with " + menu.getItemCount()
						+ " items. "
						+ chatConsole.inputText.toDisplay(e.x, e.y));
			}
			menu.setLocation(chatConsole.outputText.toDisplay(e.x, e.y));
			menu.setVisible(true);
			while (!menu.isDisposed() && menu.isVisible()) {
				if (!chatConsole.getDisplay().readAndDispatch()) {
					chatConsole.getDisplay().sleep();
				}
			}
		}
		menu.dispose();
	}

	protected void playSounds(ChatEvent event) {
		if (!isSoundDisabled && !event.hasBeenHandled()) {
			if (event.getType() == ChatType.TELL
					&& getPreferences().getBoolean(
							PreferenceKeys.CHAT_IS_PLAYING_CHAT_ON_PERSON_TELL)) {
				SoundService.getInstance().playSound("chat");
			} else if (event.getType() == ChatType.PARTNER_TELL
					&& getPreferences().getBoolean(
							PreferenceKeys.CHAT_IS_PLAYING_CHAT_ON_PTELL)) {
				SoundService.getInstance().playSound("chat");
			} else if (event.getType() == ChatType.CHALLENGE
					&& getPreferences().getBoolean(
							PreferenceKeys.BOARD_PLAY_CHALLENGE_SOUND)) {
				SoundService.getInstance().playSound("challenge");
			} else if (event.getType() == ChatType.ABORT_REQUEST
					&& getPreferences().getBoolean(
							PreferenceKeys.BOARD_PLAY_ABORT_REQUEST_SOUND)) {
				SoundService.getInstance().playSound("abortRequested");
			} else if (event.getType() == ChatType.DRAW_REQUEST
					&& getPreferences().getBoolean(
							PreferenceKeys.BOARD_PLAY_CHALLENGE_SOUND)) {
				SoundService.getInstance().playSound("drawRequested");
			}
		}
	}

	protected void reduceInputTextIfNeeded() {

		int charCount = chatConsole.inputText.getCharCount();
		if (charCount > Raptor.getInstance().getPreferences().getInt(
				CHAT_MAX_CONSOLE_CHARS)) {
			LOG.info("Cleaning chat console");
			long startTime = System.currentTimeMillis();
			int cleanTo = (int) (charCount * CLEAN_PERCENTAGE);
			chatConsole.inputText.getContent().replaceTextRange(0, cleanTo, "");
			LOG.info("Cleaned console in "
					+ (System.currentTimeMillis() - startTime));
		}

	}

	protected void registerForChatEvents() {
		connector.getChatService().addChatServiceListener(chatServiceListener);
	}

	protected void removeListenersTiedToChatConsole() {
		if (!chatConsole.isDisposed()) {
			chatConsole.outputText
					.removeKeyListener(consoleOutputTextKeyListener);
			chatConsole.outputText.removeMouseListener(outputTextClickListener);
			chatConsole.inputText
					.removeKeyListener(consoleInputTextKeyListener);
			chatConsole.inputText.removeMouseListener(inputTextClickListener);
			chatConsole.inputText.getVerticalBar().removeSelectionListener(
					verticalScrollbarListener);
		}
	}

	protected void setCaretToOutputTextEnd() {
		if (!isIgnoringActions()) {
			getChatConsole().getOutputText().setSelection(
					getChatConsole().getOutputText().getCharCount());
		}
	}

	protected void smartScroll() {
		ScrollBar scrollbar = chatConsole.inputText.getVerticalBar();
		if (scrollbar != null
				&& scrollbar.isVisible()
				&& getPreferences().getBoolean(
						PreferenceKeys.CHAT_IS_SMART_SCROLL_ENABLED)) {

			if (scrollbar.getMaximum() == scrollbar.getSelection()
					+ scrollbar.getThumb()) {
				setAutoScrolling(true);
			} else {
				setAutoScrolling(false);
			}
		}
	}

	protected void updateImageIcon(ChatEvent event) {
		if (!isActive && !hasUnseenText) {
			hasUnseenText = true;
			fireItemChanged();
		}
	}
}
