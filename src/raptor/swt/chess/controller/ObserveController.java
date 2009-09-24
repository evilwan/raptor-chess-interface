package raptor.swt.chess.controller;

import raptor.game.Game;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.chess.ChessBoardController;

public class ObserveController extends ChessBoardController {

	protected GameServiceListener listener = new GameServiceListener() {

		public void gameCreated(Game game) {
		}

		public void gameInactive(Game game) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						adjustToGameChangeNotInvolvingMove();
					}
				});
			}
		}

		public void gameStateChanged(Game game,final boolean isNewMove) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (isNewMove) {
							adjustToGameMove();
						}
						else {
							adjustToGameChangeNotInvolvingMove();
						}
					}
				});
			}
		}
	};

	protected void adjustPremoveLabel() {
		board.getCurrentPremovesLabel().setText("");
	}

	@Override
	protected void onPlayMoveSound() {
		if (board.getPreferences().getBoolean(
				PreferenceKeys.BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING)) {
			SoundService.getInstance().playSound("obsMove");
		}
	}

	@Override
	protected void onPlayGameEndSound() {
		SoundService.getInstance().playSound("obsGameEnd");
	}

	@Override
	protected void onPlayGameStartSound() {
		SoundService.getInstance().playSound("gameStart");
	}

	public void init() {
		super.init();
		board.getConnector().getGameService().addGameServiceListener(listener);
	}

	public void dispose() {
		board.getConnector().getGameService().removeGameServiceListener(
				listener);
		super.dispose();
	}

	@Override
	protected void adjustCoolbarToInitial() {
		board.addGameActionButtonsToCoolbar();
		board.addScripterCoolbar();
		board.packCoolbar();
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		return false;
	}

	@Override
	public boolean isAbortable() {
		return false;
	}

	@Override
	public boolean isAdjournable() {
		return false;
	}

	@Override
	public boolean isAutoDrawable() {
		return false;
	}

	@Override
	public boolean isCommitable() {
		return false;
	}

	@Override
	public boolean isDrawable() {
		return false;
	}

	@Override
	public boolean isExaminable() {
		return false;
	}

	@Override
	public boolean isMoveListTraversable() {
		return true;
	}

	@Override
	public boolean isPausable() {
		return false;
	}

	@Override
	public boolean isRematchable() {
		return false;
	}

	@Override
	public boolean isResignable() {
		return false;
	}

	@Override
	public boolean isRevertable() {
		return false;
	}

	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
	}

	@Override
	public void userMiddleClicked(int square) {
	}

	@Override
	public void userRightClicked(int square) {
	}

}
