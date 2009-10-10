/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import raptor.Raptor;
import raptor.chess.GameConstants;
import raptor.chess.util.GameUtils;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.chess.layout.RightOrientedLayout;
import raptor.swt.chess.movelist.SimpleMoveList;

/**
 * A GUI representation of a chess board, and all controls associated with it
 * (e.g. labels,piece jail, etc).. All ChessBoards should have a
 * ChessBoardController which manages it.
 */
public class ChessBoard implements BoardConstants {

	static final Log LOG = LogFactory.getLog(ChessBoard.class);

	protected CLabel blackClockLabel;
	protected CLabel blackLagLabel;
	protected CLabel blackNameRatingLabel;
	protected CLabel blackToMoveIndicatorLabel;
	protected ChessBoardLayout chessBoardLayout;
	protected ChessBoardController controller;
	protected CLabel currentPremovesLabel;
	protected CLabel gameDescriptionLabel;
	protected boolean isWhiteOnTop = false;
	protected boolean isWhitePieceJailOnTop = true;
	protected CLabel openingDescriptionLabel;
	protected ChessBoardMoveList moveList;
	protected Composite boardComposite;
	protected SashForm sashForm;

	// Piece jail is indexed by the colored piece constants in Constants.
	// The 0th index will always be null. (for the empty piece).
	protected LabeledChessSquare[] pieceJailSquares = new LabeledChessSquare[13];

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg0) {
			updateFromPrefs();
		}
	};

	protected ResultChessBoardDecoration resultDecoration;
	protected ChessSquare[][] squares = new ChessSquare[8][8];
	protected CLabel statusLabel;
	protected CLabel whiteClockLabel;
	protected CLabel whiteLagLabel;

	protected CLabel whiteNameRatingLabel;

	protected CLabel whiteToMoveIndicatorLabel;

	public ChessBoard() {
	}

	/**
	 * Creates the chess board layout to use for this chess board.
	 */
	protected void createChessBoardLayout() {
		chessBoardLayout = new RightOrientedLayout(this);
	}

	/**
	 * Creates the chess board with the specified parent.
	 */
	public Composite createControls(Composite parent) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Creating controls");
		}
		long startTime = System.currentTimeMillis();

		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		boardComposite = new Composite(sashForm, SWT.NONE);
		createMoveList();
		moveList.getControl(sashForm);
		int preferredWeight = moveList.getPreferredWeight();
		sashForm
				.setWeights(new int[] { 100 - preferredWeight, preferredWeight });
		sashForm.setMaximizedControl(boardComposite);

		sashForm.addDisposeListener(new DisposeListener() {
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
				if (LOG.isDebugEnabled()) {
					LOG.debug("Disposed chessboard.");
				}
			}
		});

		createChessBoardLayout();
		boardComposite.setLayout(chessBoardLayout);

		createSquares();
		createPieceJailControls();

		whiteNameRatingLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.NAME_RATING_LABEL));
		blackNameRatingLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.NAME_RATING_LABEL));
		whiteClockLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.CLOCK_LABEL));
		blackClockLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.CLOCK_LABEL));
		whiteLagLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.LAG_LABEL));
		blackLagLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.LAG_LABEL));
		openingDescriptionLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.OPENING_DESCRIPTION_LABEL));
		statusLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.STATUS_LABEL));
		gameDescriptionLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.GAME_DESCRIPTION_LABEL));
		currentPremovesLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.CURRENT_PREMOVE_LABEL));

		whiteToMoveIndicatorLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.TO_MOVE_INDICATOR));
		whiteToMoveIndicatorLabel.setImage(BoardUtils.getToMoveIndicatorImage(
				true, 30));

		blackToMoveIndicatorLabel = new CLabel(boardComposite, chessBoardLayout
				.getStyle(ChessBoardLayout.Field.TO_MOVE_INDICATOR));
		blackToMoveIndicatorLabel.setImage(BoardUtils.getToMoveIndicatorImage(
				false, 30));

		Raptor.getInstance().getPreferences().addPropertyChangeListener(
				propertyChangeListener);

		resultDecoration = new ResultChessBoardDecoration(this);

		updateFromPrefs();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Created controls in "
					+ (System.currentTimeMillis() - startTime));
		}
		return getControl();
	}

	protected void createMoveList() {
		moveList = new SimpleMoveList();
		moveList.setController(controller);
	}

	protected void createPieceJailControls() {
		pieceJailSquares[GameConstants.WP] = new LabeledChessSquare(
				boardComposite, this, GameConstants.WP_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WN] = new LabeledChessSquare(
				boardComposite, this, GameConstants.WN_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WB] = new LabeledChessSquare(
				boardComposite, this, GameConstants.WB_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WR] = new LabeledChessSquare(
				boardComposite, this, GameConstants.WR_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WQ] = new LabeledChessSquare(
				boardComposite, this, GameConstants.WQ_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.WK] = new LabeledChessSquare(
				boardComposite, this, GameConstants.WK_DROP_FROM_SQUARE);

		pieceJailSquares[GameConstants.BP] = new LabeledChessSquare(
				boardComposite, this, GameConstants.BP_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BN] = new LabeledChessSquare(
				boardComposite, this, GameConstants.BN_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BB] = new LabeledChessSquare(
				boardComposite, this, GameConstants.BB_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BR] = new LabeledChessSquare(
				boardComposite, this, GameConstants.BR_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BQ] = new LabeledChessSquare(
				boardComposite, this, GameConstants.BQ_DROP_FROM_SQUARE);
		pieceJailSquares[GameConstants.BK] = new LabeledChessSquare(
				boardComposite, this, GameConstants.BK_DROP_FROM_SQUARE);
	}

	protected void createSquares() {
		boolean isWhiteSquare = true;
		for (int i = 0; i < 8; i++) {
			isWhiteSquare = !isWhiteSquare;
			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j] = new ChessSquare(boardComposite, this, GameUtils
						.rankFileToSquare(i, j), isWhiteSquare);
				isWhiteSquare = !isWhiteSquare;
			}
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

	/**
	 * Returns the panel containing the chess board. Should really be not be
	 * used by anything outside of the swt.chess package.
	 */
	public Composite getBoardComposite() {
		return boardComposite;
	}

	/**
	 * Returns the control representing this chess board.
	 */
	public Composite getControl() {
		return sashForm;
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

	public ChessBoardMoveList getMoveList() {
		return moveList;
	}

	public CLabel getOpeningDescriptionLabel() {
		return openingDescriptionLabel;
	}

	public LabeledChessSquare getPieceJailSquare(int coloredPiece) {
		return pieceJailSquares[coloredPiece];
	}

	/**
	 * Returns the array of LabeledChessSquares representing the piece jail
	 * squares.
	 * 
	 * @return
	 */
	public LabeledChessSquare[] getPieceJailSquares() {
		return pieceJailSquares;
	}

	/**
	 * Returns the chess board result decoration for this chess board.
	 */
	public ResultChessBoardDecoration getResultDecoration() {
		return resultDecoration;
	}

	/**
	 * Returns the ChessSquare at the specified square. Drop constants in
	 * GameConstants are also supported.
	 */
	public ChessSquare getSquare(int square) {

		if (BoardUtils.isPieceJailSquare(square)) {
			return pieceJailSquares[BoardUtils.pieceJailSquareToPiece(square)];
		} else {
			int rank = square / 8;
			int file = square % 8;
			return squares[rank][file];
		}
	}

	/**
	 * Returns the ChessSquare at the specified 0 based rank and 0 based file.
	 */
	public ChessSquare getSquare(int rank, int file) {
		return squares[rank][file];
	}

	/**
	 * Returns the ChessSquares being managed in 0 based rank file order.
	 * square[rank][file].
	 */
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

	public void hideMoveList() {
		sashForm.setMaximizedControl(boardComposite);
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
	 */
	public void redrawSquares() {
		for (int i = 0; i < 8; i++) {

			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j].redraw();
			}

		}

		for (int i = 1; i < pieceJailSquares.length; i++) {
			pieceJailSquares[i].redraw();
		}

	}

	/**
	 * Sets the controller managing this ChessBoard.
	 */
	public void setController(ChessBoardController controller) {
		this.controller = controller;
		if (moveList != null) {
			moveList.setController(controller);
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

	public void showMoveList() {
		sashForm.setMaximizedControl(null);
	}

	/**
	 * Unhighlights all the squares. This method does not redraw them, that is
	 * left to the caller.
	 */
	public void unhighlightAllSquares() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				squares[i][j].unhighlight();
			}
		}
		for (LabeledChessSquare pieceJailSquare : pieceJailSquares) {
			if (pieceJailSquare != null) {
				pieceJailSquare.unhighlight();
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
				squares[i][j].setForeground(preferences
						.getColor(BOARD_HIGHLIGHT_COLOR));
				squares[i][j].clearCache();
			}
		}

		for (LabeledChessSquare pieceJailSquare : pieceJailSquares) {
			if (pieceJailSquare != null) {
				pieceJailSquare.setForeground(preferences
						.getColor(BOARD_HIGHLIGHT_COLOR));
				pieceJailSquare.setBackground(preferences
						.getColor(BOARD_PIECE_JAIL_BACKGROUND_COLOR));
				pieceJailSquare.clearCache();
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

		boardComposite.setBackground(preferences
				.getColor(BOARD_BACKGROUND_COLOR));
		sashForm.layout(true, true);
		sashForm.redraw();
	}

}
