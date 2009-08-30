package raptor.service;

public class ChatService {

	private static final ChatService instance = new ChatService();

	public static interface ChatServiceListener {
		public void chatEventOccured(ChatEvent e);
	}

	public static ChatService getInstance() {
		return instance;
	}

	public void addChatServiceListener(ChatServiceListener listener) {
	}

	public void removeChatServiceListener(ChatServiceListener listener) {
	}
	
	public void publishChatEvent(ChatEvent event) {
	}
}
