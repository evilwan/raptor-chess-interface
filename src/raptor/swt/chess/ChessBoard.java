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
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.game.GameConstants;
import raptor.game.util.GameUtils;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.chess.layout.RightOrientedLayout;

public class ChessBoard extends Composite implements BoardConstants {

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
	public static final String SETUP_DONE = "setup-done";
	public static final String SETUP_START = "setup-start";
	public static final String SETUP_CLEAR = "setup-done";

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
	protected CoolBar coolbar;

	protected Label whiteNameRatingLabel;

	protected Composite boardPanel;

	protected Composite coolbarComposite;

	protected Map<String, Button> coolbarButtonMap = new HashMap<String, Button>();

	public ChessBoard(Composite parent, int style) {
		super(parent, style);
	}

	public void addAutoPromoteRadioGroupToCoolbar() {
		LOG.debug("Adding auto promote radios to coolbar");
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
		queenButton.setImage(BoardUtils.getChessPieceIconImage("XBoard", BQ));
		queenButton.setSelection(true);
		coolbarButtonMap.put(AUTO_QUEEN, queenButton);
		queenButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

		Button knightButton = new Button(composite, SWT.RADIO);
		knightButton.setToolTipText("Set auto promote piece to a knight.");
		knightButton.setImage(BoardUtils.getChessPieceIconImage("XBoard", BN));
		coolbarButtonMap.put(AUTO_KNIGHT, knightButton);
		knightButton
				.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

		Button rookButton = new Button(composite, SWT.RADIO);
		rookButton.setToolTipText("Set auto promote piece to a rook.");
		rookButton.setImage(BoardUtils.getChessPieceIconImage("XBoard", BR));
		coolbarButtonMap.put(AUTO_ROOK, rookButton);
		rookButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

		Button bishopButton = new Button(composite, SWT.RADIO);
		bishopButton.setToolTipText("Set auto promote piece to a bishop.");
		bishopButton.setImage(BoardUtils.getChessPieceIconImage("XBoard", BB));
		coolbarButtonMap.put(AUTO_BISHOP, bishopButton);
		bishopButton
				.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

		CoolItem item = new CoolItem(getCoolbar(), SWT.NONE);
		item.setControl(composite);
	}

	public void addGameActionButtonsToCoolbar() {
		LOG.debug("Adding game action buttons to coolbar");
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

		if (controller.isNavigatable()) {
			Button firstButtonItem = new Button(composite, SWT.FLAT);
			firstButtonItem.setImage(Raptor.getInstance().getIcon("first"));
			firstButtonItem.setToolTipText("Go to the first move played");
			firstButtonItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavFirst();

				}
			});
			coolbarButtonMap.put(FIRST_NAV, firstButtonItem);
		}

		if (controller.isNavigatable()) {
			Button backButton = new Button(composite, SWT.FLAT);
			backButton.setImage(Raptor.getInstance().getIcon("back"));
			backButton.setToolTipText("Go to the previous move played");
			backButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavBack();

				}
			});
			coolbarButtonMap.put(BACK_NAV, backButton);
		}

		if (controller.isRevertable()) {
			Button revertButton = new Button(composite, SWT.FLAT);
			revertButton.setImage(Raptor.getInstance().getIcon(
					"counterClockwise"));
			revertButton.setToolTipText("Revert back to main-variation.");
			revertButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavRevert();

				}
			});
			coolbarButtonMap.put(REVERT_NAV, revertButton);
		}

		if (controller.isCommitable()) {
			Button commitButton = new Button(composite, SWT.FLAT);
			commitButton.setImage(Raptor.getInstance().getIcon("clockwise"));
			commitButton.setToolTipText("Commit sub-variation.");
			commitButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavCommit();

				}
			});

			coolbarButtonMap.put(COMMIT_NAV, commitButton);
		}

		if (controller.isNavigatable()) {
			Button nextButton = new Button(composite, SWT.FLAT);
			nextButton.setImage(Raptor.getInstance().getIcon("next"));
			nextButton.setToolTipText("Go to the next move played");
			nextButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavForward();

				}
			});
			coolbarButtonMap.put(NEXT_NAV, nextButton);
		}

		if (controller.isNavigatable()) {
			Button lastButton = new Button(composite, SWT.FLAT);
			lastButton.setImage(Raptor.getInstance().getIcon("last"));
			lastButton.setToolTipText("Go to the last move played");
			lastButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					controller.onNavLast();

				}
			});
			coolbarButtonMap.put(LAST_NAV, lastButton);
		}

		Button flipButton = new Button(composite, SWT.FLAT);
		flipButton.setImage(Raptor.getInstance().getIcon("flip"));
		flipButton.setToolTipText("Flips the chess board.");
		coolbarButtonMap.put(FLIP, flipButton);
		flipButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onFlip();
			}
		});

		if (controller.isAutoDrawable()) {
			Button autoDrawButton = new Button(composite, SWT.CHECK);
			autoDrawButton.setImage(Raptor.getInstance().getIcon("draw"));
			autoDrawButton
					.setToolTipText("Offer a draw after every move you make.");
			coolbarButtonMap.put(AUTO_DRAW, autoDrawButton);
			autoDrawButton.setSize(flipButton.getSize());
		}

		CoolItem item = new CoolItem(getCoolbar(), SWT.NONE);
		item.setControl(composite);
	}

	public void addScripterCoolbar() {
		LOG.debug("Adding scripter coolbar");
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
		executeButton.setImage(Raptor.getInstance().getIcon("enter"));
		executeButton.setToolTipText("Executes the selected script.");
		executeButton.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		CoolItem item = new CoolItem(getCoolbar(), SWT.NONE);
		item.setControl(composite);
	}

	public void addSetupToCoolbar() {
		LOG.debug("Adding setup to coolbar");
		Composite composite = new Composite(getCoolbar(), SWT.NONE);
		GridLayout gridLayout = new GridLayout(10, false);
		gridLayout.marginLeft = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginBottom = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		Button setupInitial = new Button(composite, SWT.FLAT);
		setupInitial.setText("Start Position");
		setupInitial.setToolTipText("Sets up the initial position.");
		setupInitial.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onSetupStart();

			}
		});
		coolbarButtonMap.put(SETUP_START, setupInitial);

		Button setupClear = new Button(composite, SWT.FLAT);
		setupClear.setText("Clear Position");
		setupClear.setToolTipText("Clears all pieces from the chess board.");
		setupClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onSetupClear();
			}
		});
		coolbarButtonMap.put(SETUP_CLEAR, setupClear);

		Button setupDone = new Button(composite, SWT.FLAT);
		setupDone.setText("Setup Complete");
		setupDone.setToolTipText("Completes setup mode.");
		setupDone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onSetupDone();
			}
		});
		coolbarButtonMap.put(SETUP_DONE, setupDone);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Setup from FEN:");
		final Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button executeButton = new Button(composite, SWT.FLAT);
		executeButton.setImage(Raptor.getInstance().getIcon("enter"));
		executeButton
				.setToolTipText("Sets up the position from the specified FEN.");
		executeButton.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		executeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onSetupFen(text.getText());
			}
		});
		coolbarButtonMap.put(SETUP_EXECUTE_FEN, executeButton);

		CoolItem item = new CoolItem(getCoolbar(), SWT.NONE);
		item.setControl(composite);
	}

	public void clearCoolbar() {

		coolbar.setVisible(false);
		coolbar.dispose();

		coolbar = new CoolBar(coolbarComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.COOLBAR));
		coolbar.setLayoutData(new GridData(GridData.FILL_BOTH));
		// for (int i = 0; i < getCoolbar().getItemCount(); i++) {
		// CoolItem item = getCoolbar().getItem(i);
		// if (item.getControl() != null) {
		// item.getControl().setVisible(false);
		// // item.getControl().setParent(null);
		// item.getControl().dispose();
		// item.setControl(null);
		// item.setMinimumSize(21, 12);
		// item.setPreferredSize(0, 0);
		// item.setSize(0, 0);
		// item.dispose();
		// LOG.debug("Disposed of toolbar item.");
		// }
		// }
	}

	protected void createChessBoardLayout() {
		chessBoardLayout = new RightOrientedLayout(this);
	}

	public void createControls() {
		LOG.info("Creating controls");
		long startTime = System.currentTimeMillis();
		createChessBoardLayout();

		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.marginLeft = 0;
		mainLayout.marginTop = 0;
		mainLayout.marginRight = 0;
		mainLayout.marginBottom = 0;
		mainLayout.horizontalSpacing = 0;
		mainLayout.verticalSpacing = 0;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		setLayout(mainLayout);

		coolbarComposite = new Composite(this, SWT.NONE);
		coolbarComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout coolbarCompositeLayout = new GridLayout(1, false);
		coolbarCompositeLayout.marginLeft = 0;
		coolbarCompositeLayout.marginTop = 0;
		coolbarCompositeLayout.marginRight = 0;
		coolbarCompositeLayout.marginBottom = 0;
		coolbarCompositeLayout.horizontalSpacing = 0;
		coolbarCompositeLayout.verticalSpacing = 0;
		coolbarCompositeLayout.marginHeight = 0;
		coolbarCompositeLayout.marginWidth = 0;
		coolbarComposite.setLayout(coolbarCompositeLayout);

		coolbar = new CoolBar(coolbarComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.COOLBAR));
		coolbar.setLayoutData(new GridData(GridData.FILL_BOTH));

		boardPanel = new Composite(this, SWT.NONE);
		boardPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
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
		Raptor.getInstance().getPreferences().removePropertyChangeListener(
				propertyChangeListener);
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

		super.dispose();
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

	public int getAutoPromoteSelection() {
		int result = EMPTY;

		if (getCoolBarButton(AUTO_QUEEN) != null) {
			result = getCoolBarButton(AUTO_QUEEN).getSelection() ? QUEEN
					: EMPTY;
			if (result == EMPTY) {
				result = getCoolBarButton(AUTO_KNIGHT).getSelection() ? KNIGHT
						: EMPTY;
				if (result == EMPTY) {
					result = getCoolBarButton(AUTO_BISHOP).getSelection() ? BISHOP
							: EMPTY;
					if (result == EMPTY) {
						result = ROOK;
					}
				}
			}
		}
		return result;
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

	public CoolBar getCoolbar() {
		return coolbar;
	}

	public Button getCoolBarButton(String buttonKey) {
		return coolbarButtonMap.get(buttonKey);
	}

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
				this, BoardConstants.WP_PIECE_JAIL_SQUARE);
		pieceJailSquares[GameConstants.WN] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.WN_PIECE_JAIL_SQUARE);
		pieceJailSquares[GameConstants.WB] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.WB_PIECE_JAIL_SQUARE);
		pieceJailSquares[GameConstants.WR] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.WR_PIECE_JAIL_SQUARE);
		pieceJailSquares[GameConstants.WQ] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.WQ_PIECE_JAIL_SQUARE);
		pieceJailSquares[GameConstants.WK] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.WK_PIECE_JAIL_SQUARE);

		pieceJailSquares[GameConstants.BP] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.BP_PIECE_JAIL_SQUARE);
		pieceJailSquares[GameConstants.BN] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.BN_PIECE_JAIL_SQUARE);
		pieceJailSquares[GameConstants.BB] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.BB_PIECE_JAIL_SQUARE);
		pieceJailSquares[GameConstants.BR] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.BR_PIECE_JAIL_SQUARE);
		pieceJailSquares[GameConstants.BQ] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.BQ_PIECE_JAIL_SQUARE);
		pieceJailSquares[GameConstants.BK] = new LabeledChessSquare(boardPanel,
				this, BoardConstants.BK_PIECE_JAIL_SQUARE);
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
	}

	public boolean isWhiteOnTop() {
		return isWhiteOnTop;
	}

	public boolean isWhitePieceJailOnTop() {
		return this.isWhitePieceJailOnTop;
	}

	public void packCoolbar() {
		for (int i = 0; i < getCoolbar().getItemCount(); i++) {
			CoolItem item = getCoolbar().getItem(i);
			if (!item.isDisposed() && item.getControl() != null
					&& item.getControl().isVisible()
					&& item.getMinimumSize() != null
					&& !item.getMinimumSize().equals(new Point(21, 12))) {
				item.getControl().pack();
				Point point = item.getControl().computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				item.setSize(item.computeSize(point.x, point.y));
				LOG.debug("New item preferred size " + point.x + " " + point.y);
			} else {
				LOG.debug("found item to dispose. settings its size to 0");
				item.setSize(0, 0);
			}
		}
		coolbarComposite.layout();
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

	public void setCoolbar(CoolBar coolbar) {
		this.coolbar = coolbar;
	}

	public void setCoolBarButtonEnabled(boolean isEnabled, String buttonKey) {
		Button button = getCoolBarButton(buttonKey);
		if (button != null) {
			button.setEnabled(isEnabled);
		}
	}

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
