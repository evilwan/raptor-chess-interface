package raptor.swt.chess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import raptor.Raptor;
import raptor.game.GameConstants;
import raptor.game.Game.Result;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.SWTUtils;
import raptor.swt.chess.layout.RightOrientedLayout;

public class ChessBoard extends Composite implements BoardConstants {

	public static final String SETUP_EXECUTE_FEN = "setup-execute-fen";

	static final Log LOG = LogFactory.getLog(ChessBoard.class);

	protected CLabel blackClockLabel;
	protected CLabel blackLagLabel;
	protected CLabel blackNameRatingLabel;

	protected ChessBoardController controller;
	protected CLabel currentPremovesLabel;
	protected CLabel gameDescriptionLabel;

	protected boolean isWhiteOnTop = false;
	protected boolean isWhitePieceJailOnTop = true;

	protected CLabel statusLabel;
	protected ChessBoardLayout chessBoardLayout;

	protected CLabel openingDescriptionLabel;

	// Piece jail is indexed by the colored piece constants in Constants.
	// Some of the indexes will always be null.
	protected LabeledChessSquare[] pieceJailSquares = new LabeledChessSquare[14];
	
	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg0) {
			updateFromPrefs();
		}
	};

	protected ChessSquare[][] squares = new ChessSquare[8][8];
	protected CLabel whiteClockLabel;
	protected CLabel whiteLagLabel;
	protected CLabel whiteToMoveIndicatorLabel;
	protected CLabel blackToMoveIndicatorLabel;

	protected CLabel whiteNameRatingLabel;

	protected int resultFontHeight;

	protected Font resultFont;

	public ChessBoard(Composite parent) {
		super(parent, SWT.DOUBLE_BUFFERED);
	}

	protected void createChessBoardLayout() {
		chessBoardLayout = new RightOrientedLayout(this);
	}

	public void createControls() {
		LOG.info("Creating controls");

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (propertyChangeListener != null) {
					Raptor.getInstance().getPreferences()
							.removePropertyChangeListener(
									propertyChangeListener);
					propertyChangeListener = null;
				}
				if (controller != null) {
					controller.dispose();
					controller = null;
				}
				if (chessBoardLayout != null) {
					chessBoardLayout.dispose();
					chessBoardLayout = null;
				}
				LOG.debug("Disposed chessboard.");
			}
		});

		long startTime = System.currentTimeMillis();
		createChessBoardLayout();

		setLayout(chessBoardLayout);

		initSquares();
		initPieceJail();

		whiteNameRatingLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.NAME_RATING_LABEL));
		blackNameRatingLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.NAME_RATING_LABEL));
		whiteClockLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.CLOCK_LABEL));
		blackClockLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.CLOCK_LABEL));
		whiteLagLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.LAG_LABEL));
		blackLagLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.LAG_LABEL));
		openingDescriptionLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.OPENING_DESCRIPTION_LABEL));
		statusLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.STATUS_LABEL));
		gameDescriptionLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.GAME_DESCRIPTION_LABEL));
		currentPremovesLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.CURRENT_PREMOVE_LABEL));

		whiteToMoveIndicatorLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.TO_MOVE_INDICATOR));
		whiteToMoveIndicatorLabel.setImage(BoardUtils.getToMoveIndicatorImage(
				true, 30));

		blackToMoveIndicatorLabel = new CLabel(this, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.TO_MOVE_INDICATOR));
		blackToMoveIndicatorLabel.setImage(BoardUtils.getToMoveIndicatorImage(
				false, 30));

		Raptor.getInstance().getPreferences().addPropertyChangeListener(
				propertyChangeListener);

		LOG.info("Created controls in "
				+ (System.currentTimeMillis() - startTime));

		updateFromPrefs();
	}

	/**
	 * Draws the result text centered in the specified component.
	 * 
	 * @param e
	 *            The paint event to draw in.
	 * @param text
	 *            The text to draw.
	 */
	protected void drawResultText(PaintEvent e, String text) {
		if (text != null) {
			e.gc.setForeground(Raptor.getInstance().getPreferences().getColor(
					PreferenceKeys.BOARD_RESULT_COLOR));

			e.gc.setFont(getResultFont(e.height));

			Point extent = e.gc.stringExtent(text);
			e.gc.drawString(text, e.width / 2 - extent.x / 2, e.height / 2
					- extent.y / 2, true);
		}
	}

	public CLabel getBlackClockLabel() {
		return blackClockLabel;
	}

	public CLabel getBlackLagLabel() {
		return blackLagLabel;
	}

	public CLabel getBlackNameRatingLabel() {
		return blackNameRatingLabel;
	}

	public CLabel getBlackToMoveIndicatorLabel() {
		return blackToMoveIndicatorLabel;
	}

	public ChessBoardController getController() {
		return controller;
	}

	public CLabel getCurrentPremovesLabel() {
		return currentPremovesLabel;
	}

	public CLabel getGameDescriptionLabel() {
		return gameDescriptionLabel;
	}

	public CLabel getOpeningDescriptionLabel() {
		return openingDescriptionLabel;
	}

	public LabeledChessSquare getPieceJailSquare(int coloredPiece) {
		return pieceJailSquares[coloredPiece];
	}

	public LabeledChessSquare[] getPieceJailSquares() {
		return pieceJailSquares;
	}

	/**
	 * Returns the font to use for the result text drawn over the board. This
	 * font grows and shrinks depending on the size of the square the text is
	 * drawn in.
	 * 
	 * @param width
	 *            The width of the square.
	 * @param height
	 *            The height of the square.
	 */
	protected synchronized Font getResultFont(int height) {
		if (resultFont == null) {
			resultFont = Raptor.getInstance().getPreferences().getFont(
					PreferenceKeys.BOARD_RESULT_FONT);
			resultFont = SWTUtils.getProportionalFont(resultFont, 80, height);
			resultFontHeight = height;
		} else {
			if (resultFontHeight != height) {
				Font newFont = SWTUtils.getProportionalFont(resultFont, 80,
						height);
				resultFontHeight = height;
				resultFont = newFont;
			}
		}
		return resultFont;
	}

	public ChessSquare getSquare(int square) {

		if (BoardUtils.isPieceJailSquare(square)) {
			return pieceJailSquares[BoardUtils.pieceJailSquareToPiece(square)];
		} else {
			int rank = square / 8;
			int file = square % 8;
			return squares[rank][file];
		}
	}

	public ChessSquare getSquare(int rank, int file) {
		return squares[rank][file];
	}

	public ChessSquare[][] getSquares() {
		return squares;
	}

	public CLabel getStatusLabel() {
		return statusLabel;
	}

	public CLabel getWhiteClockLabel() {
		return whiteClockLabel;
	}

	public CLabel getWhiteLagLabel() {
		return whiteLagLabel;
	}

	public CLabel getWhiteNameRatingLabel() {
		return whiteNameRatingLabel;
	}

	public CLabel getWhiteToMoveIndicatorLabel() {
		return whiteToMoveIndicatorLabel;
	}

	void initPieceJail() {
		pieceJailSquares[GameConstants.WP] = new LabeledChessSquare(this,
				GameConstants.WP_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WN] = new LabeledChessSquare(this,
				GameConstants.WN_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WB] = new LabeledChessSquare(this,
				GameConstants.WB_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WR] = new LabeledChessSquare(this,
				GameConstants.WR_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WQ] = new LabeledChessSquare(this,
				GameConstants.WQ_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WK] = new LabeledChessSquare(this,
				GameConstants.WK_DROP_FROM_SQUARE);

		pieceJailSquares[GameConstants.BP] = new LabeledChessSquare(this,
				GameConstants.BP_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BN] = new LabeledChessSquare(this,
				GameConstants.BN_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BB] = new LabeledChessSquare(this,
				GameConstants.BB_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BR] = new LabeledChessSquare(this,
				GameConstants.BR_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BQ] = new LabeledChessSquare(this,
				GameConstants.BQ_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BK] = new LabeledChessSquare(this,
				GameConstants.BK_DROP_FROM_SQUARE);
	}

	void initSquares() {
		boolean isWhiteSquare = true;
		for (int i = 0; i < 8; i++) {
			isWhiteSquare = !isWhiteSquare;
			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j] = new ChessSquare(this, GameUtils
						.rankFileToSquare(i, j), isWhiteSquare);
				isWhiteSquare = !isWhiteSquare;
			}
		}

		// Add paint listeners to the squares to redraw the result.
		squares[GameUtils.getRank(SQUARE_D4)][GameUtils.getFile(SQUARE_D4)]
				.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						if (!isWhiteOnTop
								&& getController().getGame().getResult() != Result.IN_PROGRESS) {
							String text = null;
							if (getController().getGame().getResult() == Result.WHITE_WON) {
								text = "1";
							} else if (getController().getGame().getResult() == Result.BLACK_WON) {
								text = "0";
							} else if (getController().getGame().getResult() == Result.DRAW) {
								text = ".5";
							} else if (getController().getGame().getResult() == Result.UNDETERMINED) {

							}
							drawResultText(e, text);
						}
					}
				});

		squares[GameUtils.getRank(SQUARE_E4)][GameUtils.getFile(SQUARE_E4)]
				.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						if (!isWhiteOnTop
								&& getController().getGame().getResult() != Result.IN_PROGRESS) {

							String text = null;
							if (getController().getGame().getResult() == Result.WHITE_WON) {
								text = "-";
							} else if (getController().getGame().getResult() == Result.BLACK_WON) {
								text = "-";
							} else if (getController().getGame().getResult() == Result.DRAW) {
								text = "-";
							} else if (getController().getGame().getResult() == Result.UNDETERMINED) {
								text = "*";
							}
							drawResultText(e, text);
						}
					}
				});

		squares[GameUtils.getRank(SQUARE_F4)][GameUtils.getFile(SQUARE_F4)]
				.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						if (!isWhiteOnTop
								&& getController().getGame().getResult() != Result.IN_PROGRESS) {

							String text = null;
							if (getController().getGame().getResult() == Result.WHITE_WON) {
								text = "0";
							} else if (getController().getGame().getResult() == Result.BLACK_WON) {
								text = "1";
							} else if (getController().getGame().getResult() == Result.DRAW) {
								text = ".5";
							}
							drawResultText(e, text);
						}
					}
				});

		squares[GameUtils.getRank(SQUARE_E5)][GameUtils.getFile(SQUARE_E5)]
				.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						if (isWhiteOnTop
								&& getController().getGame().getResult() != Result.IN_PROGRESS) {
							String text = null;
							if (getController().getGame().getResult() == Result.WHITE_WON) {
								text = "1";
							} else if (getController().getGame().getResult() == Result.BLACK_WON) {
								text = "0";
							} else if (getController().getGame().getResult() == Result.DRAW) {
								text = ".5";
							} else if (getController().getGame().getResult() == Result.UNDETERMINED) {

							}
							drawResultText(e, text);
						}
					}
				});

		squares[GameUtils.getRank(SQUARE_D5)][GameUtils.getFile(SQUARE_D5)]
				.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						if (isWhiteOnTop
								&& getController().getGame().getResult() != Result.IN_PROGRESS) {

							String text = null;
							if (getController().getGame().getResult() == Result.WHITE_WON) {
								text = "-";
							} else if (getController().getGame().getResult() == Result.BLACK_WON) {
								text = "-";
							} else if (getController().getGame().getResult() == Result.DRAW) {
								text = "-";
							} else if (getController().getGame().getResult() == Result.UNDETERMINED) {
								text = "*";
							}
							drawResultText(e, text);
						}
					}
				});

		squares[GameUtils.getRank(SQUARE_C5)][GameUtils.getFile(SQUARE_C5)]
				.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						if (isWhiteOnTop
								&& getController().getGame().getResult() != Result.IN_PROGRESS) {

							String text = null;
							if (getController().getGame().getResult() == Result.WHITE_WON) {
								text = "0";
							} else if (getController().getGame().getResult() == Result.BLACK_WON) {
								text = "1";
							} else if (getController().getGame().getResult() == Result.DRAW) {
								text = ".5";
							}
							drawResultText(e, text);
						}
					}
				});

	}

	public boolean isWhiteOnTop() {
		return isWhiteOnTop;
	}

	public boolean isWhitePieceJailOnTop() {
		return this.isWhitePieceJailOnTop;
	}

	/**
	 * Forces redraws on all of the squares.
	 */
	public void redrawSquares() {
		for (int i = 0; i < 8; i++) {

			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j].redraw();
			}
		}
	}

	public void setController(ChessBoardController controller) {
		this.controller = controller;
	}

	public void setWhiteOnTop(boolean isWhiteOnTop) {
		this.isWhiteOnTop = isWhiteOnTop;
	}

	public void setWhitePieceJailOnTop(boolean isWhitePieceJailOnTop) {
		this.isWhitePieceJailOnTop = isWhitePieceJailOnTop;
	}

	public void unhighlightAllSquares() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				squares[i][j].unhighlight();
			}
		}
		for (int i = 0; i < pieceJailSquares.length; i++) {
			if (pieceJailSquares[i] != null) {
				pieceJailSquares[i].unhighlight();
			}
		}
	}

	void updateBoardFromPrefs() {
		RaptorPreferenceStore preferences = Raptor.getInstance()
				.getPreferences();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				squares[i][j].setForeground(preferences
						.getColor(BOARD_HIGHLIGHT_COLOR));
				squares[i][j].clearCache();
			}
		}

		for (int i = 0; i < pieceJailSquares.length; i++) {
			if (pieceJailSquares[i] != null) {
				pieceJailSquares[i].setForeground(preferences
						.getColor(BOARD_HIGHLIGHT_COLOR));
				pieceJailSquares[i].setBackground(preferences
						.getColor(BOARD_PIECE_JAIL_BACKGROUND_COLOR));
				pieceJailSquares[i].clearCache();
			}
		}
	}

	public void updateFromPrefs() {
		RaptorPreferenceStore preferences = Raptor.getInstance()
				.getPreferences();

		updateBoardFromPrefs();

		Color background = preferences.getColor(BOARD_BACKGROUND_COLOR);

		whiteNameRatingLabel.setFont(preferences
				.getFont(BOARD_PLAYER_NAME_FONT));
		whiteNameRatingLabel.setForeground(preferences
				.getColor(BOARD_PLAYER_NAME_COLOR));
		whiteNameRatingLabel.setBackground(background);

		blackNameRatingLabel.setFont(preferences
				.getFont(BOARD_PLAYER_NAME_FONT));
		blackNameRatingLabel.setForeground(preferences
				.getColor(BOARD_PLAYER_NAME_COLOR));
		blackNameRatingLabel.setBackground(background);

		whiteToMoveIndicatorLabel.setBackground(background);
		blackToMoveIndicatorLabel.setBackground(background);

		whiteLagLabel.setFont(preferences.getFont(BOARD_LAG_FONT));
		whiteLagLabel.setForeground(preferences.getColor(BOARD_LAG_COLOR));
		whiteLagLabel.setBackground(background);

		blackLagLabel.setFont(preferences.getFont(BOARD_LAG_FONT));
		blackLagLabel.setForeground(preferences.getColor(BOARD_LAG_COLOR));
		blackLagLabel.setBackground(background);

		whiteClockLabel.setFont(preferences.getFont(BOARD_CLOCK_FONT));
		whiteClockLabel.setForeground(preferences
				.getColor(BOARD_INACTIVE_CLOCK_COLOR));
		whiteClockLabel.setBackground(background);

		blackClockLabel.setFont(preferences.getFont(BOARD_CLOCK_FONT));
		blackClockLabel.setForeground(preferences
				.getColor(BOARD_INACTIVE_CLOCK_COLOR));
		blackClockLabel.setBackground(background);

		openingDescriptionLabel.setFont(preferences
				.getFont(BOARD_OPENING_DESC_FONT));
		openingDescriptionLabel.setForeground(preferences
				.getColor(BOARD_OPENING_DESC_COLOR));
		openingDescriptionLabel.setBackground(background);

		statusLabel.setFont(preferences.getFont(BOARD_STATUS_FONT));
		statusLabel.setForeground(preferences.getColor(BOARD_STATUS_COLOR));
		statusLabel.setBackground(background);

		gameDescriptionLabel.setFont(preferences
				.getFont(BOARD_GAME_DESCRIPTION_FONT));
		gameDescriptionLabel.setForeground(preferences
				.getColor(BOARD_GAME_DESCRIPTION_COLOR));
		gameDescriptionLabel.setBackground(background);

		currentPremovesLabel.setFont(preferences.getFont(BOARD_PREMOVES_FONT));
		currentPremovesLabel.setForeground(preferences
				.getColor(BOARD_PREMOVES_COLOR));
		currentPremovesLabel.setBackground(background);

		setBackground(preferences.getColor(BOARD_BACKGROUND_COLOR));
		layout(true, true);
		redraw();
	}

}
