package raptor.swt.chat.controller;

import raptor.chat.ChatEvent;
import raptor.swt.chat.ChatConsole;
import raptor.swt.chat.ChatConsoleController;

public class MainConsoleController extends ChatConsoleController {

	public MainConsoleController(ChatConsole console) {
		super(console);
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isPrependable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSearchable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getPrompt() {
		// TODO Auto-generated method stub
		return chatConsole.getConnector().getPrompt();
	}
	
	

}
