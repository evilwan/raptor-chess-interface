/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.service;

import java.util.ArrayList;
import java.util.List;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatLogger;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;

/**
 * A service which invokes chatEventOccured on added ChatListeners when a
 * ChatEvents arrive on a connector.
 */
public class ChatService {

	public static interface ChatListener {
		public void chatEventOccured(ChatEvent e);

		public boolean isHandling(ChatEvent e);
	}

	protected Connector connector = null;
	protected List<ChatListener> listeners = new ArrayList<ChatListener>(5);
	protected List<ChatListener> mainConsoleListeners = new ArrayList<ChatListener>(
			5);
	protected ChatLogger logger = null;

	/**
	 * Constructs a chat service for the specified connector.
	 * 
	 * @param connector
	 */
	public ChatService(Connector connector) {
		this.connector = null;
		logger = new ChatLogger(Raptor.USER_RAPTOR_HOME_PATH + "/chatcache/"
				+ connector.getShortName() + ".txt");
	}

	/**
	 * Adds a ChatServiceListener to the chat service. Please remove the
	 * listener when you no longer need the ChatService to avoid memory leaks.
	 */
	public void addChatServiceListener(ChatListener listener) {
		listeners.add(listener);
	}

	public void addMainConsoleListener(ChatListener listener) {
		mainConsoleListeners.add(listener);
	}

	/**
	 * Disposes all resources the ChatService is using.
	 */
	public void dispose() {
		listeners.clear();
		if (logger != null) {
			logger.delete();
		}
		listeners = null;
		logger = null;
		connector = null;
	}

	/**
	 * Returns the Chat Services Chat Logger.
	 */
	public ChatLogger getChatLogger() {
		return logger;
	}

	/**
	 * Returns the Connector backing this ChatService.
	 */
	public Connector getConnector() {
		return connector;
	}

	/**
	 * Chat events are published asynchronously.
	 */
	public void publishChatEvent(final ChatEvent event) {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				if (listeners == null) {
					return;
				}
				boolean wasHandled = false;
				for (ChatListener listener : listeners) {
					if (listener.isHandling(event)) {
						listener.chatEventOccured(event);
						wasHandled = true;
					}
				}

				if (!wasHandled
						|| !Raptor
								.getInstance()
								.getPreferences()
								.getBoolean(
										PreferenceKeys.CHAT_REMOVE_SUB_TAB_MESSAGES_FROM_MAIN_TAB)) {
					for (ChatListener listener : mainConsoleListeners) {
						if (listener.isHandling(event)) {
							listener.chatEventOccured(event);
							wasHandled = true;
						}
					}
				}
				logger.write(event);
			}
		});
	}

	/**
	 * Removes a listener from the ChatService.
	 */
	public void removeChatServiceListener(ChatListener listener) {
		listeners.remove(listener);
		mainConsoleListeners.remove(listener);
	}

}
