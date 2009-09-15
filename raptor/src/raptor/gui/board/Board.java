package raptor.gui.board;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

import raptor.game.Game;
import raptor.game.util.GameUtils;
import raptor.service.GameService;
import raptor.service.SWTService;

class Board extends Composite {
	class ChessBoardLayout extends Layout {

		@Override
		protected Point computeSize(Composite composite, int hint, int hint2,
				boolean flushCache) {
			return new Point(600, 400);
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {
			int width = composite.getSize().x;
			int height = composite.getSize().y;

			@SuppressWarnings("unused")
			int x, y, xInit, coordinatesHeight, squareSide;

			if (isShowingCoordinates) {
				squareSide = width > height ? height / 8 : width / 8;
				if (squareSide % 2 != 0) {
					squareSide -= 1;
				}

				GC gc = new GC(Board.this);
				gc.setFont(rankLabels[0].getFont());
				int charWidth = gc.getFontMetrics().getAverageCharWidth() + 5;
				int charHeight = gc.getFontMetrics().getAscent()
						+ gc.getFontMetrics().getDescent() + 6;
				gc.dispose();

				squareSide -= Math.round(charWidth / 8.0);

				x = charWidth;
				xInit = charWidth;
				y = 0;

				for (int i = 0; i < rankLabels.length; i++) {
					int multiplier = (isWhiteOnTop ? 7 - i : i);
					rankLabels[i].setLocation(0, (int) (squareSide * multiplier
							+ squareSide / 2 - .4 * charHeight));
					rankLabels[i].setSize(charWidth, charHeight);
				}

				for (int i = 0; i < Board.this.fileLabels.length; i++) {
					int multiplier = (isWhiteOnTop ? 7 - i : i);
					fileLabels[i].setLocation((int) (charHeight * .4
							+ squareSide * multiplier + squareSide / 2),
							(squareSide * 8));
					fileLabels[i].setSize(charWidth, charHeight);
				}

				coordinatesHeight = charHeight;

			} else {
				squareSide = width > height ? height / 8 : width / 8;
				if (squareSide % 2 != 0) {
					squareSide -= 1;
				}

				x = 0;
				xInit = 0;
				y = 0;
				coordinatesHeight = 0;
			}

			if (!isWhiteOnTop) {
				for (int i = 7; i > -1; i--) {
					for (int j = 0; j < 8; j++) {
						squares[i][j].setBounds(x, y, squareSide, squareSide);

						x += squareSide;
					}
					x = xInit;
					y += squareSide;

				}
			} else {
				for (int i = 0; i < 8; i++) {
					for (int j = 7; j > -1; j--) {
						squares[i][j].setBounds(x, y, squareSide, squareSide);

						x += squareSide;
					}
					x = xInit;
					y += squareSide;
				}
			}

		}
	}

	static final String CLICK_INITIATOR = "CLICK_INITIATOR";
	static final String DRAG_INITIATOR = "DRAG_INITIATOR";
	List<BoardListener> boardListeners = new ArrayList<BoardListener>(3);
	Image dragIcon = null;
	Label[] fileLabels = new Label[8];
	String gameId;
	boolean isShowingCoordinates = true;
	boolean isWhiteOnTop = false;
	long lastDropTime;;

	Log LOG = LogFactory.getLog(Board.class);

	public void addBoardListener(BoardListener listener) {
		boardListeners.add(listener);

	}

	public void removeBoardListener(BoardListener listener) {
		boardListeners.remove(listener);
	}

	Label[] rankLabels = new Label[8];
	BoardSquare[][] squares = new BoardSquare[8][8];
	int storedPiece = Set.EMPTY;

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg0) {
			updateFromPrefs();
			redraw();
		}
	};

	public Board(Composite parent, int style) {
		super(parent, style);
		setLayout(new ChessBoardLayout());
		init();
		initLabels();
		SWTService.getInstance().getStore().addPropertyChangeListener(
				propertyChangeListener);
	}

	@Override
	public void dispose() {
		for (int i = 0; i < squares.length; i++) {
			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j].dispose();
			}
		}

		for (int i = 0; i < rankLabels.length; i++) {
			rankLabels[i].dispose();
			fileLabels[i].dispose();
		}
		boardListeners.clear();
		SWTService.getInstance().getStore().removePropertyChangeListener(
				propertyChangeListener);
		super.dispose();
	}

	public void fireMoveCancelled(int fromSquare, boolean isDnd) {
		for (BoardListener listener : boardListeners) {
			listener.moveCancelled(gameId, fromSquare, isDnd);
		}
	}

	public void fireMoveInitiated(int square, boolean isDnd) {
		for (BoardListener listener : boardListeners) {
			listener.moveInitiated(gameId, square, isDnd);
		}
	}

	public void fireMoveMade(int fromSquare, int toSquare) {
		for (BoardListener listener : boardListeners) {
			listener.moveMade(gameId, fromSquare, toSquare);
		}
	}

	public void fireOnMiddleClick(int square) {
		for (BoardListener listener : boardListeners) {
			listener.onMiddleClick(gameId, square);
		}
	}

	public void fireOnRightClick(int square) {
		for (BoardListener listener : boardListeners) {
			listener.onRightClick(gameId, square);
		}
	}

	public void init() {
		boolean isWhiteSquare = true;
		for (int i = 0; i < 8; i++) {
			isWhiteSquare = !isWhiteSquare;
			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j] = new BoardSquare(this, this, GameUtils
						.rankFileToSquare(i, j), isWhiteSquare);
				isWhiteSquare = !isWhiteSquare;
			}
		}
	}

	void initLabels() {
		int labelStyle = SWT.CENTER | SWT.SHADOW_NONE;
		for (int i = 8; i > 0; i--) {
			rankLabels[i - 1] = new Label(this, labelStyle);
			rankLabels[i - 1].setText("" + i);
		}

		rankLabels[0] = new Label(this, labelStyle);
		rankLabels[0].setText("8");
		rankLabels[1] = new Label(this, labelStyle);
		rankLabels[1].setText("7");
		rankLabels[2] = new Label(this, labelStyle);
		rankLabels[2].setText("6");
		rankLabels[3] = new Label(this, labelStyle);
		rankLabels[3].setText("5");
		rankLabels[4] = new Label(this, labelStyle);
		rankLabels[4].setText("4");
		rankLabels[5] = new Label(this, labelStyle);
		rankLabels[5].setText("3");
		rankLabels[6] = new Label(this, labelStyle);
		rankLabels[6].setText("2");
		rankLabels[7] = new Label(this, labelStyle);
		rankLabels[7].setText("1");

		fileLabels[0] = new Label(this, labelStyle);
		fileLabels[0].setText("a");
		fileLabels[1] = new Label(this, labelStyle);
		fileLabels[1].setText("b");
		fileLabels[2] = new Label(this, labelStyle);
		fileLabels[2].setText("c");
		fileLabels[3] = new Label(this, labelStyle);
		fileLabels[3].setText("d");
		fileLabels[4] = new Label(this, labelStyle);
		fileLabels[4].setText("e");
		fileLabels[5] = new Label(this, labelStyle);
		fileLabels[5].setText("f");
		fileLabels[6] = new Label(this, labelStyle);
		fileLabels[6].setText("g");
		fileLabels[7] = new Label(this, labelStyle);
		fileLabels[7].setText("h");
	}

	public void setShowingCoordinates(boolean isShowingCoordinates) {
		if (this.isShowingCoordinates != isShowingCoordinates) {
			this.isShowingCoordinates = isShowingCoordinates;
			layout();
		}
	}

	public void setWhiteOnTop(boolean isWhiteOnTop) {
		if (this.isWhiteOnTop != isWhiteOnTop) {
			this.isWhiteOnTop = isWhiteOnTop;
			layout();
		}
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public BoardSquare[][] getSquares() {
		return squares;
	}

	public void setSquares(BoardSquare[][] squares) {
		this.squares = squares;
	}

	public boolean isShowingCoordinates() {
		return isShowingCoordinates;
	}

	public boolean isWhiteOnTop() {
		return isWhiteOnTop;
	}

	public void unhighlightAllSquares() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				squares[i][j].unhighlight();
			}
		}
	}

	public BoardSquare getSquare(int square) {
		int rank = square / 8;
		int file = square % 8;
		return squares[rank][file];
	}

	public BoardSquare getSquare(int rank, int file) {
		return squares[rank][file];
	}

	public void updateFromPrefs() {
		LOG.info("Updating prefs " + gameId);
		long startTime = System.currentTimeMillis();
		setShowingCoordinates(SWTService.getInstance().getStore().getBoolean(
				SWTService.BOARD_IS_SHOW_COORDINATES));

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				squares[i][j].setForeground(SWTService.getInstance().getColor(
						SWTService.BOARD_HIGHLIGHT_COLOR));
				squares[i][j].forceLayout();
			}
		}

		for (Label label : rankLabels) {

			if (isShowingCoordinates) {

				label.setFont(SWTService.getInstance().getFont(
						SWTService.BOARD_COORDINATES_FONT));
				label.setForeground(SWTService.getInstance().getColor(
						SWTService.BOARD_COORDINATES_COLOR));
				label.setBackground(SWTService.getInstance().getColor(
						SWTService.BOARD_BACKGROUND_COLOR));
				label.setVisible(true);
			} else {
				label.setVisible(false);
			}
		}

		for (Label label : fileLabels) {

			if (isShowingCoordinates) {

				label.setFont(SWTService.getInstance().getFont(
						SWTService.BOARD_COORDINATES_FONT));
				label.setForeground(SWTService.getInstance().getColor(
						SWTService.BOARD_COORDINATES_COLOR));
				label.setBackground(SWTService.getInstance().getColor(
						SWTService.BOARD_BACKGROUND_COLOR));
				label.setVisible(true);
			} else {
				label.setVisible(false);
			}
		}

		setBackground(SWTService.getInstance().getColor(
				SWTService.BOARD_BACKGROUND_COLOR));
		LOG
				.info("Updated prefs in "
						+ (System.currentTimeMillis() - startTime));
	}

	public Game getGame() {
		Game game = GameService.getInstance().getGame(getGameId());
		if (game == null) {
			LOG.error("Could not find game with id " + getGameId());
			throw new IllegalStateException("Game not found " + getGameId());
		}
		return game;
	}

	public void updateFromGame() {
		Game game = getGame();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				squares[i][j].setPiece(Set.getSetPieceFromGamePiece(GameUtils
						.rankFileToSquare(i, j), game));
			}
		}
	}

}
