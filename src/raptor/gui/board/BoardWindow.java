package raptor.gui.board;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import quicktime.streaming.SettingsDialog;
import raptor.game.Game;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.pref.PreferencesDialog;
import raptor.service.GameService;
import raptor.service.SWTService;

public class BoardWindow extends ApplicationWindow {
	Log LOG = LogFactory.getLog(BoardWindow.class);

	public static void main(String[] args) {
		final Game game = GameUtils.createStartingPosition();
		game.setId("1");
		GameService.getInstance().addGame(game);
		final BoardWindow window = new BoardWindow("1");
		window.addBoardListener(new BoardListener() {

			private SecureRandom random = new SecureRandom();

			public void moveCancelled(String gameId, int fromSquare,
					boolean isDnd) {
				window.LOG.debug("moveCancelled" + gameId + " " + fromSquare
						+ " " + isDnd);
				window.board.unhighlightAllSquares();
				window.board.updateFromGame();
			}

			public void moveInitiated(String gameId, int square, boolean isDnd) {
				window.LOG.debug("moveInitiated" + gameId + " " + square + " "
						+ isDnd);

				window.board.unhighlightAllSquares();
				window.board.getSquare(square).highlight();
				if (isDnd) {
					window.board.getSquare(square).setPiece(Set.EMPTY);
				}
			}

			public void moveMade(String gameId, int fromSquare, int toSquare) {
				window.LOG.debug("Move made " + gameId + " " + fromSquare + " "
						+ toSquare);
				window.board.unhighlightAllSquares();
				try {
					Move move = game.makeMove(fromSquare, toSquare);
					window.board.getSquare(move.getFrom()).highlight();
					window.board.getSquare(move.getTo()).highlight();
				} catch (IllegalArgumentException iae) {
					window.LOG.info("Move was illegal " + fromSquare + " "
							+ toSquare);
				}
				window.board.updateFromGame();
			}

			public void onMiddleClick(String gameId, int square) {
				window.LOG.debug("On middle click " + gameId + " " + square);
				Move[] moves = game.getLegalMoves().asArray();
				List<Move> foundMoves = new ArrayList<Move>(5);
				for (Move move : moves) {
					if (move.getTo() == square) {
						foundMoves.add(move);
					}
				}

				if (foundMoves.size() > 0) {
					Move move = foundMoves.get(random
							.nextInt(foundMoves.size()));
					game.move(move);
					window.board.unhighlightAllSquares();
					window.board.getSquare(move.getFrom()).highlight();
					window.board.getSquare(move.getTo()).highlight();
					window.board.updateFromGame();
				}
			}

			public void onRightClick(String gameId, int square) {
				window.LOG.debug("On right click " + gameId + " " + square);
			}
		});

		window.getToolBarManager2().add(new Action("Flip", SWT.BORDER) {
			@Override
			public void run() {
				super.run();
				window.board.setWhiteOnTop(!window.board.isWhiteOnTop());
			}
		});
		window.getToolBarManager2().add(new Action("Preferences", SWT.BORDER) {
			@Override
			public void run() {
				PreferencesDialog dialog = new PreferencesDialog();
				dialog.run();
			}
		});

		window.setBlockOnOpen(true);
		window.open();
		Display.getCurrent().dispose();
	}

	Board board = null;
	String gameId;
	List<BoardListener> queuedListeners = new ArrayList<BoardListener>(5);

	public BoardWindow(String gameId) {
		super(null);
		this.gameId = gameId;
		addToolBar(SWT.HORIZONTAL);
	}

	public void addBoardListener(BoardListener listener) {
		if (board == null) {
			queuedListeners.add(listener);
		} else {
			board.addBoardListener(listener);
		}
	}

	public void removeBoardListener(BoardListener listener) {
		if (board == null) {
			queuedListeners.remove(listener);
		} else {
			board.removeBoardListener(listener);
		}
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
		if (board != null) {
			board.setGameId(gameId);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
//		GridLayout gridLayout = new GridLayout();
//		gridLayout.numColumns = 1;
//		parent.setLayout(gridLayout);
		createBoard(parent);
		//board.setLayoutData(GridData.FILL_BOTH);
		parent.pack();
		return parent;
	}

	protected void createBoard(Composite parent) {
		board = new Board(parent, 0);
		for (BoardListener listener : queuedListeners) {
			board.addBoardListener(listener);
		}
		queuedListeners.clear();
		board.setGameId(gameId);
		board.init();
		board.updateFromGame();
		board.updateFromPrefs();
	}

	public void updateFromPrefs() {
		board.updateFromPrefs();
		getShell().setBackground(
				SWTService.getInstance().getColor(
						SWTService.BOARD_BACKGROUND_COLOR));
	}

	public void dispose() {
		board.dispose();
	}

	@Override
	protected void initializeBounds() {
		getShell().setSize(800, 600);
		getShell().setLocation(0, 0);
	}

}
