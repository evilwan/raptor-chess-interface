package raptor.swt.chat.controller;

import raptor.chat.ChatEvent;
import raptor.swt.chat.ChatConsoleController;

public class MainController extends ChatConsoleController {

	public MainController() {
	}

	@Override
	public String getPrompt() {
		return chatConsole.getConnector().getPrompt();
	}

	@Override
	public String getTitle() {
		return chatConsole.getConnector().getShortName() + "(Main)";
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return true;
	}

	@Override
	public boolean isCloseable() {
		return false;
	}

	@Override
	public boolean isPrependable() {
		return false;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}
}
