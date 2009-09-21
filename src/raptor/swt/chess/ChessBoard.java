package raptor.swt.chess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Label;

import raptor.connector.Connector;
import raptor.game.Game;
import raptor.game.util.GameUtils;
import raptor.pref.RaptorPreferenceStore;

public class ChessBoard extends Composite implements Constants {
	public static final String LAST_NAV = "last_nav";
	public static final String NEXT_NAV = "forward_nav";
	public static final String BACK_NAV = "next_nav";
	public static final String FIRST_NAV = "first_nav";
	public static final String COMMIT_NAV = "commit_nav";
	public static final String REVERT_NAV = "revert_nav";
	public static final String AUTO_QUEEN = "auto-queen";
	public static final String AUTO_ROOK = "auto-rook";
	public static final String AUTO_BISHOP = "auto-bishop";
	public static final String AUTO_KNIGHT = "auto-knight";
	public static final String AUTO_DRAW = "auto-draw";
	public static final String FLIP = "flip";

	static final Log LOG = LogFactory.getLog(ChessBoard.class);

	protected Label blackClockLabel;
	protected Label blackLagLabel;
	protected Label blackNameRatingLabel;
	protected Connector connector;

	protected ChessBoardController controller;
	protected Label currentPremovesLabel;
	protected Label gameDescriptionLabel;

	protected Label[] fileLabels = new Label[8];

	protected Game game;
	protected boolean isWhiteOnTop = false;
	protected boolean isWhitePieceJailOnTop = true;

	protected Label statusLabel;
	protected ChessBoardLayout layout;

	protected Label openingDescriptionLabel;

	// Piece jail is indexed by the colored piece constants in Constants.
	// Some of the indexes will always be null.
	protected LabeledChessSquare[] pieceJailSquares = new LabeledChessSquare[14];
	protected RaptorPreferenceStore preferences;
	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg0) {
			updateFromPrefs();
			redraw();
		}
	};
	protected Label[] rankLabels = new Label[8];

	protected ChessBoardResources resources;

	protected ChessSquare[][] squares = new ChessSquare[8][8];
	protected Label whiteClockLabel;
	protected Label whiteLagLabel;
	protected Label whiteToMoveIndicatorLabel;
	protected Label blackToMoveIndicatorLabel;
	protected CoolBar coolbar;

	protected Label whiteNameRatingLabel;

	protected Composite boardPanel;

	protected Map<String, Button> coolbarButtonMap = new HashMap<String, Button>();

	public ChessBoard(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void dispose() {
		preferences.removePropertyChangeListener(propertyChangeListener);
		super.dispose();
		pieceJailSquares = null;
		if (controller != null) {
			controller.dispose();
			controller = null;
		}
		if (layout != null) {
			layout.dispose();
			layout = null;
		}
		if (resources != null) {
			resources.dispose();
			resources = null;
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

	public ChessBoardLayout getBoardLayout() {
		return layout;
	}

	public Connector getConnector() {
		return connector;
	}

	public ChessBoardController getController() {
		return controller;
	}

	public Label getCurrentPremovesLabel() {
		return currentPremovesLabel;
	}

	public Label[] getFileLabels() {
		return fileLabels;
	}

	public Game getGame() {
		return game;
	}

	public Label getOpeningDescriptionLabel() {
		return openingDescriptionLabel;
	}

	public ChessSquare getPieceJailSquare(int coloredPiece) {
		return pieceJailSquares[coloredPiece];
	}

	public LabeledChessSquare[] getPieceJailSquares() {
		return pieceJailSquares;
	}

	public RaptorPreferenceStore getPreferences() {
		return preferences;
	}

	public Label[] getRankLabels() {
		return rankLabels;
	}

	public ChessBoardResources getResources() {
		return resources;
	}

	public ChessSquare getSquare(int square) {

		if (Utils.isPieceJailSquare(square)) {
			return pieceJailSquares[Utils.pieceJailSquareToPiece(square)];
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

	public Label getWhiteClockLabel() {
		return whiteClockLabel;
	}

	public Label getWhiteLagLabel() {
		return whiteLagLabel;
	}

	public Label getWhiteNameRatingLabel() {
		return whiteNameRatingLabel;
	}

	public void createControls() {
		LOG.info("Creating controls");
		long startTime = System.currentTimeMillis();

		setLayout(new GridLayout(1, false));
		coolbar = new CoolBar(this, layout.getStyle(ChessBoardLayout.COOLBAR));
		coolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		boardPanel = new Composite(this, SWT.NONE);
		boardPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		boardPanel.setLayout(layout);

		initSquares();
		// initSquareLabels();
		initPieceJail();

		whiteNameRatingLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.NAME_RATING_LABEL));
		blackNameRatingLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.NAME_RATING_LABEL));
		whiteClockLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.CLOCK_LABEL));
		blackClockLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.CLOCK_LABEL));
		whiteLagLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.LAG_LABEL));
		blackLagLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.LAG_LABEL));
		openingDescriptionLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.OPENING_DESCRIPTION_LABEL));
		statusLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.STATUS_LABEL));
		gameDescriptionLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.GAME_DESCRIPTION_LABEL));
		currentPremovesLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.CURRENT_PREMOVE_LABEL));

		whiteToMoveIndicatorLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.TO_MOVE_INDICATOR));
		whiteToMoveIndicatorLabel.setImage(preferences
				.getIcon("circle_green30x30"));

		blackToMoveIndicatorLabel = new Label(boardPanel, layout
				.getStyle(ChessBoardLayout.TO_MOVE_INDICATOR));
		blackToMoveIndicatorLabel.setImage(preferences
				.getIcon("circle_gray30x30"));

		preferences.addPropertyChangeListener(propertyChangeListener);

		LOG.info("Created controls in "
				+ (System.currentTimeMillis() - startTime));

		updateFromPrefs();
	}

	void initPieceJail() {
		pieceJailSquares[Constants.WP] = new LabeledChessSquare(boardPanel,
				this, Constants.WP_PIECE_JAIL_SQUARE);
		pieceJailSquares[Constants.WN] = new LabeledChessSquare(boardPanel,
				this, Constants.WN_PIECE_JAIL_SQUARE);
		pieceJailSquares[Constants.WB] = new LabeledChessSquare(boardPanel,
				this, Constants.WB_PIECE_JAIL_SQUARE);
		pieceJailSquares[Constants.WR] = new LabeledChessSquare(boardPanel,
				this, Constants.WR_PIECE_JAIL_SQUARE);
		pieceJailSquares[Constants.WQ] = new LabeledChessSquare(boardPanel,
				this, Constants.WQ_PIECE_JAIL_SQUARE);
		pieceJailSquares[Constants.WK] = new LabeledChessSquare(boardPanel,
				this, Constants.WK_PIECE_JAIL_SQUARE);

		pieceJailSquares[Constants.BP] = new LabeledChessSquare(boardPanel,
				this, Constants.BP_PIECE_JAIL_SQUARE);
		pieceJailSquares[Constants.BN] = new LabeledChessSquare(boardPanel,
				this, Constants.BN_PIECE_JAIL_SQUARE);
		pieceJailSquares[Constants.BB] = new LabeledChessSquare(boardPanel,
				this, Constants.BB_PIECE_JAIL_SQUARE);
		pieceJailSquares[Constants.BR] = new LabeledChessSquare(boardPanel,
				this, Constants.BR_PIECE_JAIL_SQUARE);
		pieceJailSquares[Constants.BQ] = new LabeledChessSquare(boardPanel,
				this, Constants.BQ_PIECE_JAIL_SQUARE);
		pieceJailSquares[Constants.BK] = new LabeledChessSquare(boardPanel,
				this, Constants.WK_PIECE_JAIL_SQUARE);
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
	}

	public boolean isWhiteOnTop() {
		return isWhiteOnTop;
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

	public void setBoardLayout(ChessBoardLayout layout) {
		if (this.layout != layout) {
			this.layout = layout;
			layout.setBoard(this);
		}
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public void setController(ChessBoardController controller) {
		this.controller = controller;
	}

	public void setCurrentPremovesLabel(Label currentPremovesLabel) {
		this.currentPremovesLabel = currentPremovesLabel;
	}

	public void setFileLabels(Label[] fileLabels) {
		this.fileLabels = fileLabels;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public void setPreferences(RaptorPreferenceStore preferences) {
		this.preferences = preferences;
	}

	public void setResources(ChessBoardResources resources) {
		this.resources = resources;
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

	public boolean isWhitePieceJailOnTop() {
		return this.isWhitePieceJailOnTop;
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

	public Label getGameDescriptionLabel() {
		return gameDescriptionLabel;
	}

	public Label getStatusLabel() {
		return statusLabel;
	}

	void updateBoardFromPrefs() {
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

	public CoolBar getCoolbar() {
		return coolbar;
	}

	public void setCoolbar(CoolBar coolbar) {
		this.coolbar = coolbar;
	}

	public Button getCoolBarButton(String buttonKey) {
		return coolbarButtonMap.get(buttonKey);
	}

	public void setCoolBarButtonEnabled(boolean isEnabled, String buttonKey) {
		Button button = getCoolBarButton(buttonKey);
		if (button != null) {
			button.setEnabled(isEnabled);
		}
	}

	public Composite getBoardPanel() {
		return boardPanel;
	}

	public void setBoardPanel(Composite boardPanel) {
		this.boardPanel = boardPanel;
	}

	public void forceUpdate() {
		boardPanel.layout();
		boardPanel.redraw();
	}

	public void addAutoPromoteRadioGroupToCoolbar() {

		Composite composite = new Composite(getCoolbar(), SWT.NONE);
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginLeft = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginBottom = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		Button queenButton = new Button(composite, SWT.RADIO);
		queenButton.setToolTipText("Set auto promote piece to a queen.");
		queenButton.setImage(getResources()
				.getChessPieceIconImage("XBoard", BQ));
		queenButton.setSelection(true);
		coolbarButtonMap.put(AUTO_QUEEN, queenButton);
		queenButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

		Button knightButton = new Button(composite, SWT.RADIO);
		knightButton.setToolTipText("Set auto promote piece to a knight.");
		knightButton.setImage(getResources().getChessPieceIconImage("XBoard",
				BN));
		coolbarButtonMap.put(AUTO_KNIGHT, knightButton);
		knightButton
				.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

		Button rookButton = new Button(composite, SWT.RADIO);
		rookButton.setToolTipText("Set auto promote piece to a rook.");
		rookButton
				.setImage(getResources().getChessPieceIconImage("XBoard", BR));
		coolbarButtonMap.put(AUTO_ROOK, rookButton);
		rookButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

		Button bishopButton = new Button(composite, SWT.RADIO);
		bishopButton.setToolTipText("Set auto promote piece to a bishop.");
		bishopButton.setImage(getResources().getChessPieceIconImage("XBoard",
				BB));
		coolbarButtonMap.put(AUTO_BISHOP, bishopButton);
		bishopButton
				.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

		CoolItem item = new CoolItem(getCoolbar(), SWT.NONE);
		item.setControl(composite);
	}

	public void addScripterCoolbar() {
		Composite composite = new Composite(getCoolbar(), SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginLeft = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginBottom = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.add("Resign");
		combo.add("Draw");
		combo.add("Match Winner");
		combo.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

		Button executeButton = new Button(composite, SWT.FLAT);
		executeButton.setImage(getPreferences().getIcon("southWest"));
		executeButton.setToolTipText("Executes the selected script.");
		executeButton.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		CoolItem item = new CoolItem(getCoolbar(), SWT.NONE);
		item.setControl(composite);
	}

	public void addGameActionButtonsToCoolbar() {
		Composite composite = new Composite(getCoolbar(), SWT.NONE);

		GridLayout gridLayout = new GridLayout(8, false);
		gridLayout.marginLeft = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginBottom = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		Button firstButtonItem = new Button(composite, SWT.FLAT);
		firstButtonItem.setImage(getPreferences().getIcon("first"));
		firstButtonItem.setToolTipText("Go to the first move played");
		firstButtonItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				controller.onNavFirst();

			}
		});
		coolbarButtonMap.put(FIRST_NAV, firstButtonItem);

		Button backButton = new Button(composite, SWT.FLAT);
		backButton.setImage(getPreferences().getIcon("back"));
		backButton.setToolTipText("Go to the previous move played");
		backButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				controller.onNavBack();

			}
		});
		coolbarButtonMap.put(BACK_NAV, backButton);

		if (controller.isRevertable()) {
			Button revertButton = new Button(composite, SWT.FLAT);
			revertButton.setImage(getPreferences().getIcon("counterClockwise"));
			revertButton.setToolTipText("Revert back to main-variation.");
			revertButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavRevert();

				}
			});
			coolbarButtonMap.put(REVERT_NAV, revertButton);
		}

		if (controller.isCommitable()) {
			Button commitButton = new Button(composite, SWT.FLAT);
			commitButton.setImage(getPreferences().getIcon("commit"));
			commitButton.setToolTipText("Commit sub-variation.");
			commitButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavCommit();

				}
			});

			coolbarButtonMap.put(COMMIT_NAV, commitButton);
		}

		Button nextButton = new Button(composite, SWT.FLAT);
		nextButton.setImage(getPreferences().getIcon("next"));
		nextButton.setToolTipText("Go to the next move played");
		nextButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				controller.onNavForward();

			}
		});
		coolbarButtonMap.put(NEXT_NAV, nextButton);

		Button lastButton = new Button(composite, SWT.FLAT);
		lastButton.setImage(getPreferences().getIcon("last"));
		lastButton.setToolTipText("Go to the last move played");
		lastButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				controller.onNavLast();

			}
		});
		coolbarButtonMap.put(LAST_NAV, lastButton);

		Button flipButton = new Button(composite, SWT.FLAT);
		flipButton.setImage(getPreferences().getIcon("flip"));
		flipButton.setToolTipText("Flips the chess board.");
		coolbarButtonMap.put(FLIP, flipButton);
		flipButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				controller.onFlip();
			}
		});

		if (controller.isAutoDrawable()) {
			Button autoDrawButton = new Button(composite, SWT.CHECK);
			autoDrawButton.setImage(getPreferences().getIcon("draw"));
			autoDrawButton
					.setToolTipText("Offer a draw after every move you make.");
			coolbarButtonMap.put(AUTO_DRAW, autoDrawButton);
			autoDrawButton.setSize(flipButton.getSize());
		}

		CoolItem item = new CoolItem(getCoolbar(), SWT.NONE);
		item.setControl(composite);
	}

	public void packCoolbar() {
		for (int i = 0; i < getCoolbar().getItemCount(); i++) {
			CoolItem item = getCoolbar().getItem(i);
			item.getControl().pack();
			Point point = item.getControl().computeSize(SWT.DEFAULT,
					SWT.DEFAULT);
			item.setSize(item.computeSize(point.x, point.y));
			LOG.debug("New item preferred size " + point.x + " " + point.y);
		}
	}

	public Label getWhiteToMoveIndicatorLabel() {
		return whiteToMoveIndicatorLabel;
	}

	public Label getBlackToMoveIndicatorLabel() {
		return blackToMoveIndicatorLabel;
	}

}
