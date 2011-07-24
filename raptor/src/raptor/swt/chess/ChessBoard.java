/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chess;

import org.apache.commons.lang.StringUtils;
import raptor.util.RaptorLogger;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;

import raptor.Raptor;
import raptor.chess.GameConstants;
import raptor.chess.Variant;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.RaptorLabel;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chess.ChessBoardLayout.Field;
import raptor.swt.chess.analysis.UciAnalysisWidget;
import raptor.swt.chess.analysis.XboardAnalysisWidget;

/**
 * A GUI representation of a chess board, and all controls associated with it
 * (e.g. labels,piece jail, etc).
 * 
 * ChessBoards have a ChessBoardLayout which lays out their
 * components.ChessBoards have a ChessBoardController which manages user
 * adjustments and events from a controller. ChessBoards have a move list which
 * can be shown or hidden. ChessBoards have a piece jail which can be shown or
 * hidden.
 */
public class ChessBoard implements BoardConstants {

	static final RaptorLogger LOG = RaptorLogger.getLog(ChessBoard.class);

	protected RaptorLabel blackClockLabel;
	protected RaptorLabel blackLagLabel;
	protected RaptorLabel blackNameRatingLabel;
	protected Composite boardComposite;
	protected ChessBoardLayout chessBoardLayout;
	protected ChessBoardController controller;
	protected RaptorLabel currentPremovesLabel;
	protected RaptorLabel gameDescriptionLabel;
	protected boolean isWhiteOnTop = false;
	protected boolean isWhitePieceJailOnTop = true;
	protected ChessBoardMoveList moveList;
	protected EngineAnalysisWidget engineAnalysisWidget;
	protected RaptorLabel openingDescriptionLabel;
	protected CoolBar coolbar;

	/**
	 * Piece jail is indexed by the colored piece constants in Constants. The
	 * 0th index will always be null. (for the empty piece).
	 */
	protected PieceJailChessSquare[] pieceJailSquares = new PieceJailChessSquare[13];

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg) {
			if (arg.getProperty().startsWith("board")
					|| arg.getProperty().equals(APP_ZOOM_FACTOR)) {
				updateFromPrefs();
			}
		}
	};

	protected SquareHighlighter squareHighlighter;
	protected ResultDecorator resultDecorator;
	protected ArrowDecorator arrowDecorator;

	protected Composite componentComposite;
	protected SashForm boardMoveListSash;
	protected SashForm analysisSash;
	protected ChessSquare[][] squares = new ChessSquare[8][8];
	protected RaptorLabel statusLabel;
	protected RaptorLabel whiteClockLabel;
	protected RaptorLabel whiteLagLabel;
	protected RaptorLabel whiteNameRatingLabel;

	public ChessBoard() {
	}

	/**
	 * Creates the chess board with the specified parent.
	 */
	public Composite createControls(Composite parent) {
		synchronized (this) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Creating controls");
			}
			long startTime = System.currentTimeMillis();

			componentComposite = new Composite(parent, SWT.DOUBLE_BUFFERED
					| SWT.NO_BACKGROUND);
			componentComposite.setLayout(SWTUtils.createMarginlessGridLayout(1,
					true));
			if (Raptor.getInstance().getPreferences()
					.getBoolean(PreferenceKeys.BOARD_COOLBAR_ON_TOP)) {
				coolbar = new CoolBar(componentComposite, SWT.FLAT
						| SWT.HORIZONTAL);
				coolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
						false));
			}
			analysisSash = new SashForm(componentComposite, SWT.VERTICAL);
			analysisSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					true));

			if (!Raptor.getInstance().getPreferences()
					.getBoolean(PreferenceKeys.BOARD_COOLBAR_ON_TOP)) {
				coolbar = new CoolBar(componentComposite, SWT.FLAT
						| SWT.HORIZONTAL);
				coolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
						false));
			}

			boardMoveListSash = new SashForm(analysisSash, SWT.HORIZONTAL);
			boardComposite = new Composite(boardMoveListSash, SWT.NONE);
			adjustMoveList();

			createEngineAnalysisWidget();
			engineAnalysisWidget.create(analysisSash);
			analysisSash.setWeights(new int[] { 70, 30 });
			analysisSash.setMaximizedControl(boardMoveListSash);
			engineAnalysisWidget.getControl().setVisible(false);

			boardMoveListSash.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (propertyChangeListener != null) {
						Raptor.getInstance()
								.getPreferences()
								.removePropertyChangeListener(
										propertyChangeListener);
						propertyChangeListener = null;
					}
					if (engineAnalysisWidget != null) {
						engineAnalysisWidget.quit();
						engineAnalysisWidget = null;
					}
					if (controller != null) {
						controller.dispose();
						controller = null;
					}
					if (chessBoardLayout != null) {
						chessBoardLayout.dispose();
						chessBoardLayout = null;
					}
					if (arrowDecorator != null) {
						arrowDecorator.dispose();
						arrowDecorator = null;
					}
					if (squareHighlighter != null) {
						squareHighlighter.dispose();
						squareHighlighter = null;
					}
					if (resultDecorator != null) {
						resultDecorator.dispose();
						resultDecorator = null;
					}
					if (LOG.isInfoEnabled()) {
						LOG.info("Disposed ChessBoard");
					}
				}
			});

			createSquares();
			createPieceJailControls();

			whiteNameRatingLabel = new RaptorLabel(boardComposite, SWT.NONE);
			whiteNameRatingLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (SWTUtils.isRightClick(e)) {
						onNameLabelRightClick(e, whiteNameRatingLabel);
					}
				}
			});
			blackNameRatingLabel = new RaptorLabel(boardComposite, SWT.NONE);
			blackNameRatingLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (SWTUtils.isRightClick(e)) {
						onNameLabelRightClick(e, blackNameRatingLabel);
					}
				}
			});
			whiteClockLabel = new RaptorLabel(boardComposite, SWT.NONE);
			blackClockLabel = new RaptorLabel(boardComposite, SWT.NONE);
			whiteLagLabel = new RaptorLabel(boardComposite, SWT.NONE);
			blackLagLabel = new RaptorLabel(boardComposite, SWT.NONE);
			openingDescriptionLabel = new RaptorLabel(boardComposite, SWT.NONE);
			statusLabel = new RaptorLabel(boardComposite, SWT.NONE);
			gameDescriptionLabel = new RaptorLabel(boardComposite, SWT.NONE);
			currentPremovesLabel = new RaptorLabel(boardComposite, SWT.NONE);

			Raptor.getInstance().getPreferences()
					.addPropertyChangeListener(propertyChangeListener);

			// order is important here.
			squareHighlighter = new SquareHighlighter(this);
			arrowDecorator = new ArrowDecorator(this);
			resultDecorator = new ResultDecorator(this);

			adjustChessBoardLayout();

			updateFromPrefs();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Created controls in "
						+ (System.currentTimeMillis() - startTime));
			}
			return getControl();
		}
	}

	public void dispose() {
		boardMoveListSash.dispose();
	}

	public synchronized ArrowDecorator getArrowDecorator() {
		return arrowDecorator;
	}

	public synchronized RaptorLabel getBlackClockLabel() {
		return blackClockLabel;
	}

	public synchronized RaptorLabel getBlackLagLabel() {
		return blackLagLabel;
	}

	public synchronized RaptorLabel getBlackNameRatingLabel() {
		return blackNameRatingLabel;
	}

	/**
	 * Returns the panel containing the chess board. Should really be not be
	 * used by anything outside of the swt.chess package.
	 */
	public synchronized Composite getBoardComposite() {
		return boardComposite;
	}

	/**
	 * Returns the control representing this chess board.
	 */
	public synchronized Composite getControl() {
		return componentComposite;
	}

	public synchronized ChessBoardController getController() {
		return controller;
	}

	public CoolBar getCoolbar() {
		return coolbar;
	}

	public synchronized RaptorLabel getCurrentPremovesLabel() {
		return currentPremovesLabel;
	}

	public EngineAnalysisWidget getEngineAnalysisWidget() {
		return engineAnalysisWidget;
	}

	public synchronized RaptorLabel getGameDescriptionLabel() {
		return gameDescriptionLabel;
	}

	public synchronized ChessBoardMoveList getMoveList() {
		return moveList;
	}

	public synchronized RaptorLabel getOpeningDescriptionLabel() {
		return openingDescriptionLabel;
	}

	public synchronized PieceJailChessSquare getPieceJailSquare(int coloredPiece) {
		return pieceJailSquares[coloredPiece];
	}

	/**
	 * Returns the array of LabeledChessSquares representing the piece jail
	 * squares.
	 * 
	 * @return
	 */
	public synchronized PieceJailChessSquare[] getPieceJailSquares() {
		return pieceJailSquares;
	}

	/**
	 * Returns the result decorator. Used to decorate a games results over the
	 * chess board..
	 */
	public synchronized ResultDecorator getResultDecorator() {
		return resultDecorator;
	}

	/**
	 * Returns the ChessSquare at the specified square. Drop constants in
	 * GameConstants are also supported.
	 */
	public synchronized ChessSquare getSquare(int square) {

		if (ChessBoardUtils.isPieceJailSquare(square)) {
			return pieceJailSquares[ChessBoardUtils
					.pieceJailSquareToPiece(square)];
		} else {
			int rank = square / 8;
			int file = square % 8;
			return squares[rank][file];
		}
	}

	/**
	 * Returns the ChessSquare at the specified 0 based rank and 0 based file.
	 */
	public synchronized ChessSquare getSquare(int rank, int file) {
		return squares[rank][file];
	}

	/**
	 * Returns the square highlighter, used to highlight squares on the chess
	 * board.
	 */
	public synchronized SquareHighlighter getSquareHighlighter() {
		return squareHighlighter;
	}

	/**
	 * Returns the ChessSquares being managed in 0 based rank file order.
	 * square[rank][file].
	 */
	public synchronized ChessSquare[][] getSquares() {
		return squares;
	}

	public synchronized RaptorLabel getStatusLabel() {
		return statusLabel;
	}

	public synchronized RaptorLabel getWhiteClockLabel() {
		return whiteClockLabel;
	}

	public synchronized RaptorLabel getWhiteLagLabel() {
		return whiteLagLabel;
	}

	public synchronized RaptorLabel getWhiteNameRatingLabel() {
		return whiteNameRatingLabel;
	}

	public void hideEngineAnalysisWidget() {
		analysisSash.setMaximizedControl(boardMoveListSash);
		engineAnalysisWidget.getControl().setVisible(false);
		engineAnalysisWidget.quit();
	}

	public void hideMoveList() {
		if (moveList != null && moveList.getControl() != null
				&& moveList.getControl().isVisible()) {
			boardMoveListSash.setMaximizedControl(boardComposite);
			moveList.getControl().setVisible(false);
		}
	}

	/**
	 * Hides the piece jail.
	 */
	public void hidePieceJail() {
		for (PieceJailChessSquare pieceJailSquare : pieceJailSquares) {
			if (pieceJailSquare != null) {
				pieceJailSquare.setVisible(false);
			}
		}
	}

	public boolean isDisposed() {
		return boardMoveListSash.isDisposed();
	}

	public boolean isShowingEngineAnaylsis() {
		return engineAnalysisWidget.getControl().isVisible();
	}

	/**
	 * Returns true if white is on top, false if white is on botton.
	 */
	public boolean isWhiteOnTop() {
		return isWhiteOnTop;
	}

	/**
	 * Returns true if the white pieces piece jail is on top. False if it is on
	 * bottom.
	 */
	public boolean isWhitePieceJailOnTop() {
		return isWhitePieceJailOnTop;
	}

	/**
	 * Forces redraws on all of the squares and all of the pieceJailSquares.
	 * @param forceRedraw True if every square should be redrawn. False if only dirty squares should be redrawn.
	 */
	public void redrawPiecesAndArtifacts(boolean forceRedraw) {
		if (!forceRedraw) {
			for (int i = 0; i < 8; i++) {

				for (int j = 0; j < squares[i].length; j++) {
					if (squares[i][j].isDirty) {
						squares[i][j].redraw();
					}
				}

			}

			for (int i = 1; i < pieceJailSquares.length; i++) {
				if (pieceJailSquares[i].isDirty) {
					pieceJailSquares[i].redraw();
				}
			}
		} else {
			for (int i = 0; i < 8; i++) {

				for (int j = 0; j < squares[i].length; j++) {
					squares[i][j].redraw();
				}

			}

			for (int i = 1; i < pieceJailSquares.length; i++) {
				pieceJailSquares[i].redraw();
			}
		}
	}

	/**
	 * Sets the controller managing this ChessBoard.
	 */
	public synchronized void setController(ChessBoardController controller) {
		this.controller = controller;
		if (moveList != null) {
			moveList.setController(controller);
		}
		if (engineAnalysisWidget != null) {
			engineAnalysisWidget.setController(controller);
		}
	}

	/**
	 * Sets the white on top flag. This method does not redraw or relayout. That
	 * is left to the caller.
	 */
	public void setWhiteOnTop(boolean isWhiteOnTop) {
		this.isWhiteOnTop = isWhiteOnTop;
	}

	/**
	 * Sets the piece jail on top flag. This method does not redraw or relayout.
	 * That is left to the caller.
	 */
	public void setWhitePieceJailOnTop(boolean isWhitePieceJailOnTop) {
		this.isWhitePieceJailOnTop = isWhitePieceJailOnTop;
	}

	public synchronized void showEngineAnalysisWidget() {
		engineAnalysisWidget.getControl().setVisible(true);
		analysisSash.setMaximizedControl(null);
		engineAnalysisWidget.onShow();
	}

	public synchronized void showMoveList() {

		int width = moveList.getControl().computeSize(150, SWT.DEFAULT).x;
		boardMoveListSash.setWeights(new int[] {
				boardMoveListSash.getSize().x - width, width });
		moveList.getControl().setVisible(true);
		boardMoveListSash.setMaximizedControl(null);
		moveList.forceRedraw();
	}

	/**
	 * Shows the piece jail.
	 */
	public void showPieceJail() {
		for (PieceJailChessSquare pieceJailSquare : pieceJailSquares) {
			if (pieceJailSquare != null) {
				pieceJailSquare.setVisible(true);
			}
		}
	}

	/**
	 * Unhides the pieces on all of the squares.
	 */
	public synchronized void unhidePieces() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				squares[i][j].setHidingPiece(false);
			}
		}
		for (PieceJailChessSquare pieceJailSquare : pieceJailSquares) {
			if (pieceJailSquare != null) {
				pieceJailSquare.setHidingPiece(false);
			}
		}
	}

	/**
	 * Updates all control settings to those in
	 * Raptor.getInstance().getPreferences().
	 */
	public void updateFromPrefs() {
		RaptorPreferenceStore preferences = Raptor.getInstance()
				.getPreferences();
		updateBoardFromPrefs();

		Color background = preferences.getColor(BOARD_BACKGROUND_COLOR);

		whiteNameRatingLabel.setFont(preferences
				.getFont(BOARD_PLAYER_NAME_FONT));
		whiteNameRatingLabel.setForeground(preferences
				.getColor(BOARD_CONTROL_COLOR));
		whiteNameRatingLabel.setBackground(background);

		blackNameRatingLabel.setFont(preferences
				.getFont(BOARD_PLAYER_NAME_FONT));
		blackNameRatingLabel.setForeground(preferences
				.getColor(BOARD_CONTROL_COLOR));
		blackNameRatingLabel.setBackground(background);

		whiteLagLabel.setFont(preferences.getFont(BOARD_LAG_FONT));
		whiteLagLabel.setForeground(preferences.getColor(BOARD_CONTROL_COLOR));
		whiteLagLabel.setBackground(background);

		blackLagLabel.setFont(preferences.getFont(BOARD_LAG_FONT));
		blackLagLabel.setForeground(preferences.getColor(BOARD_CONTROL_COLOR));
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
				.getColor(BOARD_CONTROL_COLOR));
		openingDescriptionLabel.setBackground(background);

		statusLabel.setFont(preferences.getFont(BOARD_STATUS_FONT));
		statusLabel.setForeground(preferences.getColor(BOARD_CONTROL_COLOR));
		statusLabel.setBackground(background);

		gameDescriptionLabel.setFont(preferences
				.getFont(BOARD_GAME_DESCRIPTION_FONT));
		gameDescriptionLabel.setForeground(preferences
				.getColor(BOARD_CONTROL_COLOR));
		gameDescriptionLabel.setBackground(background);

		currentPremovesLabel.setFont(preferences.getFont(BOARD_PREMOVES_FONT));
		currentPremovesLabel.setForeground(preferences
				.getColor(BOARD_CONTROL_COLOR));
		currentPremovesLabel.setBackground(background);

		adjustMoveList();
		adjustChessBoardLayout();

		boardComposite.setBackground(preferences
				.getColor(BOARD_BACKGROUND_COLOR));
		controller.refresh();
		boardMoveListSash.layout(true, true);
		boardMoveListSash.redraw();

		engineAnalysisWidget.updateFromPrefs();
	}

	protected void addPersonMenuItems(Menu menu, String person) {
		if (controller != null && controller.getConnector() != null) {
			ChatUtils.addPersonMenuItems(menu, controller.getConnector(),
					person);
		}
	}

	protected void adjustMoveList() {
		String moveListClassName = Raptor.getInstance().getPreferences()
				.getString(PreferenceKeys.BOARD_MOVE_LIST_CLASS);

		ChessBoardMoveList oldMoveList = moveList;

		if (oldMoveList == null
				|| !moveListClassName.equals(chessBoardLayout.getClass()
						.getName())) {

			try {
				moveList = (ChessBoardMoveList) Class
						.forName(moveListClassName).getConstructor()
						.newInstance();

				boolean wasVisible = false;
				if (oldMoveList != null && oldMoveList.getControl() != null
						&& !oldMoveList.getControl().isDisposed()) {
					wasVisible = oldMoveList.getControl().getVisible();
					oldMoveList.getControl().setVisible(false);
					oldMoveList.getControl().dispose();
					hideMoveList();
				}

				moveList.create(boardMoveListSash);
				moveList.setController(getController());

				if (wasVisible) {
					boardMoveListSash.setMaximizedControl(null);
					moveList.getControl().setVisible(true);
				} else {
					boardMoveListSash.setMaximizedControl(boardComposite);
					moveList.getControl().setVisible(false);
				}
			} catch (Throwable t) {
				Raptor.getInstance().onError("Error creating move list.", t);
			}
		}
	}

	/**
	 * Creates the chess board layout to use for this chess board.
	 */
	protected void adjustChessBoardLayout() {
		String layoutClassName = Raptor.getInstance().getPreferences()
				.getString(PreferenceKeys.BOARD_LAYOUT);
		if (chessBoardLayout == null
				|| !layoutClassName.equals(chessBoardLayout.getClass()
						.getName())) {
			ChessBoardLayout oldLayout = chessBoardLayout;

			try {
				chessBoardLayout = (ChessBoardLayout) Class
						.forName(layoutClassName)
						.getConstructor(ChessBoard.class).newInstance(this);

				if (oldLayout != null) {
					boardComposite.setLayout(null);
					oldLayout.dispose();
				}
				boardComposite.setLayout(chessBoardLayout);

				whiteNameRatingLabel.setAlignment(chessBoardLayout
						.getAlignment(Field.NAME_RATING_LABEL));
				blackNameRatingLabel.setAlignment(chessBoardLayout
						.getAlignment(Field.NAME_RATING_LABEL));

				whiteLagLabel.setAlignment(chessBoardLayout
						.getAlignment(Field.LAG_LABEL));
				blackLagLabel.setAlignment(chessBoardLayout
						.getAlignment(Field.LAG_LABEL));

				whiteClockLabel.setAlignment(chessBoardLayout
						.getAlignment(Field.CLOCK_LABEL));
				blackClockLabel.setAlignment(chessBoardLayout
						.getAlignment(Field.CLOCK_LABEL));

				gameDescriptionLabel.setAlignment(chessBoardLayout
						.getAlignment(Field.GAME_DESCRIPTION_LABEL));
				statusLabel.setAlignment(chessBoardLayout
						.getAlignment(Field.STATUS_LABEL));
				currentPremovesLabel.setAlignment(chessBoardLayout
						.getAlignment(Field.CURRENT_PREMOVE_LABEL));
				openingDescriptionLabel.setAlignment(chessBoardLayout
						.getAlignment(Field.OPENING_DESCRIPTION_LABEL));
				chessBoardLayout.adjustFontSizes();

			} catch (Throwable t) {
				throw new RuntimeException("Error creating chessBoardLayout "
						+ layoutClassName, t);
			}
		}
	}

	protected void createEngineAnalysisWidget() {
		if (!Variant.isClassic(controller.getGame().getVariant())
				&& controller.getGame().getVariant() != Variant.fischerRandom) {
			engineAnalysisWidget = new XboardAnalysisWidget();
			engineAnalysisWidget.setController(controller);
		} else {
			engineAnalysisWidget = new UciAnalysisWidget();
			engineAnalysisWidget.setController(controller);
		}
	}

	protected void createPieceJailControls() {
		pieceJailSquares[GameConstants.WP] = new PieceJailChessSquare(
				boardComposite, this, WP, GameConstants.WP_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WN] = new PieceJailChessSquare(
				boardComposite, this, WN, GameConstants.WN_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WB] = new PieceJailChessSquare(
				boardComposite, this, WB, GameConstants.WB_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WR] = new PieceJailChessSquare(
				boardComposite, this, WR, GameConstants.WR_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WQ] = new PieceJailChessSquare(
				boardComposite, this, WQ, GameConstants.WQ_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WK] = new PieceJailChessSquare(
				boardComposite, this, WK, GameConstants.WK_DROP_FROM_SQUARE);

		pieceJailSquares[GameConstants.BP] = new PieceJailChessSquare(
				boardComposite, this, BP, GameConstants.BP_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BN] = new PieceJailChessSquare(
				boardComposite, this, BN, GameConstants.BN_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BB] = new PieceJailChessSquare(
				boardComposite, this, BB, GameConstants.BB_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BR] = new PieceJailChessSquare(
				boardComposite, this, BR, GameConstants.BR_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BQ] = new PieceJailChessSquare(
				boardComposite, this, BQ, GameConstants.BQ_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BK] = new PieceJailChessSquare(
				boardComposite, this, BK, GameConstants.BK_DROP_FROM_SQUARE);
	}

	protected void createSquares() {
		boolean isWhiteSquare = true;
		for (int i = 0; i < 8; i++) {
			isWhiteSquare = !isWhiteSquare;
			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j] = new ChessSquare(boardComposite, this,
						GameUtils.getSquare(i, j), isWhiteSquare);
				isWhiteSquare = !isWhiteSquare;
			}
		}
	}

	protected void onNameLabelRightClick(MouseEvent e, RaptorLabel label) {
		if (StringUtils.isNotBlank(label.getText()) && getController() != null
				&& getController().getConnector() != null) {

			String name = label.getText().split(" ")[0];

			if (StringUtils.isNotBlank(name)) {
				Menu menu = new Menu(componentComposite.getShell(), SWT.POP_UP);
				addPersonMenuItems(menu, name);
				if (menu.getItemCount() > 0) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Showing popup with " + menu.getItemCount()
								+ " items. " + label.toDisplay(e.x, e.y));
					}
					menu.setLocation(label.toDisplay(e.x, e.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!componentComposite.getDisplay().readAndDispatch()) {
							componentComposite.getDisplay().sleep();
						}
					}
				}
				menu.dispose();
			}
		}
	}

	/**
	 * Updates only the board and piece jails from
	 * Raptor.getInstance().getPreferences().
	 */
	protected void updateBoardFromPrefs() {
		RaptorPreferenceStore preferences = Raptor.getInstance()
				.getPreferences();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				squares[i][j].clearCache();
			}
		}

		for (PieceJailChessSquare pieceJailSquare : pieceJailSquares) {
			if (pieceJailSquare != null) {
				pieceJailSquare.setBackground(preferences
						.getColor(BOARD_PIECE_JAIL_BACKGROUND_COLOR));
				pieceJailSquare.clearCache();
			}
		}
	}

}
