package raptor.service;

public class ChatService {

	public static interface ChatServiceListener {
		public void chatEventOccured(ChatEvent e);
	}

	private static final ChatService instance = new ChatService();

	public static ChatService getInstance() {
		return instance;
	}

	public void addChatServiceListener(ChatServiceListener listener) {
	}

	public void publishChatEvent(ChatEvent event) {
	}

	public void removeChatServiceListener(ChatServiceListener listener) {
	}
}
