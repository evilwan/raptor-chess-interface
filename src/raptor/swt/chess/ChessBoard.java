package raptor.swt.chess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

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

	protected Label blackClockLabel;
	protected Label blackLagLabel;
	protected Label blackNameRatingLabel;

	protected ChessBoardController controller;
	protected Label currentPremovesLabel;
	protected Label gameDescriptionLabel;

	protected Label[] fileLabels = new Label[8];

	protected boolean isWhiteOnTop = false;
	protected boolean isWhitePieceJailOnTop = true;

	protected Label statusLabel;
	protected ChessBoardLayout chessBoardLayout;

	protected Label openingDescriptionLabel;

	// Piece jail is indexed by the colored piece constants in Constants.
	// Some of the indexes will always be null.
	protected LabeledChessSquare[] pieceJailSquares = new LabeledChessSquare[14];
	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg0) {
			updateFromPrefs();
			redraw();
		}
	};
	protected Label[] rankLabels = new Label[8];

	protected ChessSquare[][] squares = new ChessSquare[8][8];
	protected Label whiteClockLabel;
	protected Label whiteLagLabel;
	protected Label whiteToMoveIndicatorLabel;
	protected Label blackToMoveIndicatorLabel;

	protected Label whiteNameRatingLabel;

	protected Composite boardPanel;

	protected int resultFontHeight;

	protected Font resultFont;

	public ChessBoard(Composite parent, int style) {
		super(parent, style);
	}

	protected void createChessBoardLayout() {
		chessBoardLayout = new RightOrientedLayout(this);
	}

	public void createControls() {
		LOG.info("Creating controls");
		long startTime = System.currentTimeMillis();
		createChessBoardLayout();

		setLayout(SWTUtils.createMarginlessGridLayout(1, false));

		// coolbarComposite = new Composite(this,SWT.NONE);
		// coolbarComposite.setLayout(SWTUtils.createMarginlessGridLayout(1,
		// false));
		// coolbar = new CoolBar(this, chessBoardLayout
		// .getStyle(ChessBoardLayout.Field.COOLBAR));
		// coolbar
		// .setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
		// false));

		boardPanel = new Composite(this, SWT.NONE);
		boardPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		boardPanel.setLayout(chessBoardLayout);

		initSquares();
		// initSquareLabels();
		initPieceJail();

		whiteNameRatingLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.NAME_RATING_LABEL));
		blackNameRatingLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.NAME_RATING_LABEL));
		whiteClockLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.CLOCK_LABEL));
		blackClockLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.CLOCK_LABEL));
		whiteLagLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.LAG_LABEL));
		blackLagLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.LAG_LABEL));
		openingDescriptionLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.OPENING_DESCRIPTION_LABEL));
		statusLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.STATUS_LABEL));
		gameDescriptionLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.GAME_DESCRIPTION_LABEL));
		currentPremovesLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.CURRENT_PREMOVE_LABEL));

		whiteToMoveIndicatorLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.TO_MOVE_INDICATOR));
		whiteToMoveIndicatorLabel.setImage(Raptor.getInstance().getIcon(
				"circle_green30x30"));

		blackToMoveIndicatorLabel = new Label(boardPanel, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.TO_MOVE_INDICATOR));
		blackToMoveIndicatorLabel.setImage(Raptor.getInstance().getIcon(
				"circle_gray30x30"));

		Raptor.getInstance().getPreferences().addPropertyChangeListener(
				propertyChangeListener);

		LOG.info("Created controls in "
				+ (System.currentTimeMillis() - startTime));

		updateFromPrefs();
	}

	/**
	 * If a controller is set on the chess board it will be disposed on this
	 * method call.
	 */
	@Override
	public void dispose() {
		if (propertyChangeListener != null) {
			Raptor.getInstance().getPreferences().removePropertyChangeListener(
					propertyChangeListener);
			propertyChangeListener = null;
		}
		pieceJailSquares = null;
		if (controller != null) {
			controller.dispose();
			controller = null;
		}
		if (chessBoardLayout != null) {
			chessBoardLayout.dispose();
			chessBoardLayout = null;
		}

		LOG.debug("Disposed chessboard.");

		if (!isDisposed()) {
			super.dispose();
		}
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

	public void forceUpdate() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering force update.");
		}
		long startTime = System.currentTimeMillis();

		if (isVisible()) {
			// boardPanel.layout(true);
			// coolbarComposite.layout(true);
			layout(true, true);
			// redraw();
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("forceUpdate in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	public Label getBlackClockLabel() {
		return blackClockLabel;
	}

	public Label getBlackLagLabel() {
		return blackLagLabel;
	}

	public Label getBlackNameRatingLabel() {
		return blackNameRatingLabel;
	}

	public Label getBlackToMoveIndicatorLabel() {
		return blackToMoveIndicatorLabel;
	}

	public Composite getBoardPanel() {
		return boardPanel;
	}

	public ChessBoardController getController() {
		return controller;
	}

	// public CoolBar getCoolbar() {
	// return coolbar;
	// }
	//
	// public Button getCoolBarButton(String buttonKey) {
	// return coolbarButtonMap.get(buttonKey);
	// }

	public Label getCurrentPremovesLabel() {
		return currentPremovesLabel;
	}

	public Label[] getFileLabels() {
		return fileLabels;
	}

	public Label getGameDescriptionLabel() {
		return gameDescriptionLabel;
	}

	public Label getOpeningDescriptionLabel() {
		return openingDescriptionLabel;
	}

	public LabeledChessSquare getPieceJailSquare(int coloredPiece) {
		return pieceJailSquares[coloredPiece];
	}

	// void initSquareLabels() {
	// int labelStyle = SWT.CENTER | SWT.SHADOW_NONE;
	// for (int i = 8; i > 0; i--) {
	// rankLabels[i - 1] = new Label(this, labelStyle);
	// rankLabels[i - 1].setText("" + i);
	// }
	//
	// rankLabels[0] = new Label(this, labelStyle);
	// rankLabels[0].setText("8");
	// rankLabels[1] = new Label(this, labelStyle);
	// rankLabels[1].setText("7");
	// rankLabels[2] = new Label(this, labelStyle);
	// rankLabels[2].setText("6");
	// rankLabels[3] = new Label(this, labelStyle);
	// rankLabels[3].setText("5");
	// rankLabels[4] = new Label(this, labelStyle);
	// rankLabels[4].setText("4");
	// rankLabels[5] = new Label(this, labelStyle);
	// rankLabels[5].setText("3");
	// rankLabels[6] = new Label(this, labelStyle);
	// rankLabels[6].setText("2");
	// rankLabels[7] = new Label(this, labelStyle);
	// rankLabels[7].setText("1");
	//
	// fileLabels[0] = new Label(this, labelStyle);
	// fileLabels[0].setText("a");
	// fileLabels[1] = new Label(this, labelStyle);
	// fileLabels[1].setText("b");
	// fileLabels[2] = new Label(this, labelStyle);
	// fileLabels[2].setText("c");
	// fileLabels[3] = new Label(this, labelStyle);
	// fileLabels[3].setText("d");
	// fileLabels[4] = new Label(this, labelStyle);
	// fileLabels[4].setText("e");
	// fileLabels[5] = new Label(this, labelStyle);
	// fileLabels[5].setText("f");
	// fileLabels[6] = new Label(this, labelStyle);
	// fileLabels[6].setText("g");
	// fileLabels[7] = new Label(this, labelStyle);
	// fileLabels[7].setText("h");
	// }

	public LabeledChessSquare[] getPieceJailSquares() {
		return pieceJailSquares;
	}

	public Label[] getRankLabels() {
		return rankLabels;
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
			resultFont = SWTUtils.getProportionalFont(getDisplay(), resultFont,
					80, height);
			resultFontHeight = height;
		} else {
			if (resultFontHeight != height) {
				Font newFont = SWTUtils.getProportionalFont(getDisplay(),
						resultFont, 80, height);
				resultFontHeight = height;
				resultFont.dispose();
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

	public Label getStatusLabel() {
		return statusLabel;
	}

	public Label getWhiteClockLabel() {
		return whiteClockLabel;
	}

	public Label getWhiteLagLabel() {
		return whiteLagLabel;
	}

	public Label getWhiteNameRatingLabel() {
		return whiteNameRatingLabel;
	}

	public Label getWhiteToMoveIndicatorLabel() {
		return whiteToMoveIndicatorLabel;
	}

	void initPieceJail() {
		pieceJailSquares[GameConstants.WP] = new LabeledChessSquare(boardPanel,
				this, GameConstants.WP_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WN] = new LabeledChessSquare(boardPanel,
				this, GameConstants.WN_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WB] = new LabeledChessSquare(boardPanel,
				this, GameConstants.WB_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WR] = new LabeledChessSquare(boardPanel,
				this, GameConstants.WR_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WQ] = new LabeledChessSquare(boardPanel,
				this, GameConstants.WQ_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WK] = new LabeledChessSquare(boardPanel,
				this, GameConstants.WK_DROP_FROM_SQUARE);

		pieceJailSquares[GameConstants.BP] = new LabeledChessSquare(boardPanel,
				this, GameConstants.BP_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BN] = new LabeledChessSquare(boardPanel,
				this, GameConstants.BN_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BB] = new LabeledChessSquare(boardPanel,
				this, GameConstants.BB_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BR] = new LabeledChessSquare(boardPanel,
				this, GameConstants.BR_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BQ] = new LabeledChessSquare(boardPanel,
				this, GameConstants.BQ_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BK] = new LabeledChessSquare(boardPanel,
				this, GameConstants.BK_DROP_FROM_SQUARE);
	}

	void initSquares() {
		boolean isWhiteSquare = true;
		for (int i = 0; i < 8; i++) {
			isWhiteSquare = !isWhiteSquare;
			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j] = new ChessSquare(boardPanel, this, GameUtils
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

	/**
	 * Returns true if the specified button is selected, false otherwise.
	 */
	// public boolean isCoolbarButtonSelectd(String buttonKey) {
	// boolean result = false;
	// Button button = coolbarButtonMap.get(buttonKey);
	// if (button != null) {
	// result = button.getSelection();
	// }
	// return result;
	// }
	public boolean isWhiteOnTop() {
		return isWhiteOnTop;
	}

	public boolean isWhitePieceJailOnTop() {
		return this.isWhitePieceJailOnTop;
	}

	// public void packCoolbar() {
	// LOG.error("PPPAAACCCKKKIIIINNNG DA COOLBAR!!!");
	// for (int i = 0; i < getCoolbar().getItemCount(); i++) {
	// CoolItem item = getCoolbar().getItem(i);
	// if (!item.isDisposed() && item.getControl() != null){
	// //&& !item.getMinimumSize().equals(new Point(21, 12))) {
	// item.getControl().pack();
	// Point point = item.getControl().computeSize(SWT.DEFAULT,
	// SWT.DEFAULT);
	// item.setSize(item.computeSize(point.x, point.y));
	// LOG.debug("New item preferred size " + point.x + " " + point.y);
	// }
	// // } else {
	// // LOG.debug("found item to dispose. settings its size to 0");
	// // item.setSize(0, 0);
	// // }
	// }
	// }

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

	public void setBlackClockLabel(Label blackClockLabel) {
		this.blackClockLabel = blackClockLabel;
	}

	public void setBlackLagLabel(Label blackLagLabel) {
		this.blackLagLabel = blackLagLabel;
	}

	public void setBlackNameRatingLabel(Label blackNameRatingLabel) {
		this.blackNameRatingLabel = blackNameRatingLabel;
	}

	public void setBoardPanel(Composite boardPanel) {
		this.boardPanel = boardPanel;
	}

	public void setController(ChessBoardController controller) {
		this.controller = controller;
	}

	// public void setCoolbar(CoolBar coolbar) {
	// this.coolbar = coolbar;
	// }
	//
	// public void setCoolBarButtonEnabled(boolean isEnabled, String buttonKey)
	// {
	// Button button = getCoolBarButton(buttonKey);
	// if (button != null) {
	// button.setEnabled(isEnabled);
	// }
	// }

	public void setCurrentPremovesLabel(Label currentPremovesLabel) {
		this.currentPremovesLabel = currentPremovesLabel;
	}

	public void setFileLabels(Label[] fileLabels) {
		this.fileLabels = fileLabels;
	}

	public void setSquares(ChessSquare[][] squares) {
		this.squares = squares;
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
				squares[i][j].forceLayout();
			}
		}

		for (int i = 0; i < pieceJailSquares.length; i++) {
			if (pieceJailSquares[i] != null) {
				pieceJailSquares[i].setForeground(preferences
						.getColor(BOARD_HIGHLIGHT_COLOR));
				pieceJailSquares[i].setBackground(preferences
						.getColor(BOARD_PIECE_JAIL_BACKGROUND_COLOR));
				pieceJailSquares[i].forceLayout();
			}
		}
	}

	void updateCoordinateLabelsFromPrefs() {
		// LOG.info("Updating prefs " + game.getId());
		// long startTime = System.currentTimeMillis();
		//
		// for (Label label : rankLabels) {
		//
		// if (preferences.getBoolean(BOARD_IS_SHOW_COORDINATES)) {
		//
		// label.setFont(preferences.getFont(BOARD_COORDINATES_FONT));
		// label.setForeground(preferences
		// .getColor(BOARD_COORDINATES_COLOR));
		// label.setBackground(preferences
		// .getColor(BOARD_BACKGROUND_COLOR));
		// label.setVisible(true);
		// } else {
		// label.setVisible(false);
		// }
		// }
		//
		// for (Label label : fileLabels) {
		//
		// if (preferences.getBoolean(BOARD_IS_SHOW_COORDINATES)) {
		//
		// label.setFont(preferences.getFont(BOARD_COORDINATES_FONT));
		// label.setForeground(preferences
		// .getColor(BOARD_COORDINATES_COLOR));
		// label.setBackground(preferences
		// .getColor(BOARD_BACKGROUND_COLOR));
		// label.setVisible(true);
		// } else {
		// label.setVisible(false);
		// }
		// }
		// LOG
		// .info("Updated prefs in "
		// + (System.currentTimeMillis() - startTime));
	}

	public void updateFromPrefs() {
		RaptorPreferenceStore preferences = Raptor.getInstance()
				.getPreferences();

		updateBoardFromPrefs();
		updateCoordinateLabelsFromPrefs();

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

		boardPanel.setBackground(preferences.getColor(BOARD_BACKGROUND_COLOR));
		setBackground(preferences.getColor(BOARD_BACKGROUND_COLOR));

		forceUpdate();
	}

}
