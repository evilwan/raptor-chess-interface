package raptor.swt.chat.controller;

import org.apache.commons.lang.StringUtils;

import raptor.chat.ChatEvent;
import raptor.swt.chat.ChatConsoleController;

public class RegExController extends ChatConsoleController {

	protected String regularExpression;

	public RegExController(String regularExpressions) {
		this.regularExpression = regularExpressions;
	}

	@Override
	public String getPrependText() {
		return "";
	}

	@Override
	public String getPrompt() {
		return chatConsole.getConnector().getPrompt();
	}

	@Override
	public String getTitle() {
		return chatConsole.getConnector().getShortName() + "("
				+ StringUtils.abbreviate(regularExpression, 10) + ")";
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent event) {
		return event.getMessage() != null ? event.getMessage().matches(
				regularExpression) : false;
	}

	@Override
	public boolean isAwayable() {
		return false;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public boolean isPrependable() {
		return true;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}
}