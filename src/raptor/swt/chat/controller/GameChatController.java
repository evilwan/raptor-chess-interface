package raptor.swt.chat.controller;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chat.ChatEvent;
import raptor.connector.Connector;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chat.ChatUtils;

public class GameChatController extends ChatConsoleController {

	protected String gameId;

	public GameChatController(Connector connector, String gameId) {
		super(connector);
		this.gameId = gameId;
	}

	public String getGameId() {
		return gameId;
	}

	@Override
	public String getName() {
		return "Game:" + gameId;
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				getConnector().getShortName() + "-" + GAME_CHAT_TAB_QUADRANT);
	}

	@Override
	public String getPrependText(boolean checkButton) {
		if (isIgnoringActions()) {
			return "";
		}

		if (checkButton
				&& isToolItemSelected(ToolBarItemKey.PREPEND_TEXT_BUTTON)) {
			return connector.getGameChatTabPrefix(gameId);
		} else if (!checkButton) {
			return connector.getGameChatTabPrefix(gameId);
		} else {
			return "";
		}
	}

	@Override
	public String getPrompt() {
		return connector.getPrompt();
	}

	@Override
	public Control getToolbar(Composite parent) {
		if (toolbar == null) {
			toolbar = new ToolBar(parent, SWT.FLAT);
			toolbar.setLayout(SWTUtils
					.createMarginlessRowLayout(SWT.HORIZONTAL));
			ChatUtils.addActionsToToolbar(this,
					RaptorActionContainer.ChannelChatConsole, toolbar);
			adjustAwayButtonEnabled();
		} else {
			toolbar.setParent(parent);
		}
		return toolbar;
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return StringUtils.isNotEmpty(inboundEvent.getGameId())
				&& gameId.equalsIgnoreCase(inboundEvent.getGameId());
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