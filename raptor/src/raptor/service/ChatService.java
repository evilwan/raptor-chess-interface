package raptor.service;

import java.util.ArrayList;
import java.util.List;

import raptor.chat.ChatEvent;

public class ChatService {

	public static interface ChatListener {
		public void chatEventOccured(ChatEvent e);
	}

	List<ChatListener> listeners = new ArrayList<ChatListener>(5);

	public void dispose() {
		listeners.clear();
		listeners = null;
	}

	public void addChatServiceListener(ChatListener listener) {
		listeners.add(listener);
	}

	public void publishChatEvent(ChatEvent event) {
		for (ChatListener listener : listeners) {
			listener.chatEventOccured(event);
		}
	}

	public void removeChatServiceListener(ChatListener listener) {
		listeners.remove(listener);
	}
}
