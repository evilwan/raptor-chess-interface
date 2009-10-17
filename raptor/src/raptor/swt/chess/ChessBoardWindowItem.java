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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.pref.PreferenceKeys;
import raptor.service.ChessBoardCacheService;
import raptor.swt.ItemChangedListener;

/**
 * You should really not create instances of this. Instead use
 * BoardUtils.buildChessBoardWindowItem
 * 
 * @author mindspan
 * 
 */
public class ChessBoardWindowItem implements RaptorWindowItem {
	static final Log LOG = LogFactory.getLog(ChessBoardWindowItem.class);

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.III,
			Quadrant.IV, Quadrant.V, Quadrant.VI, Quadrant.VII };

	ChessBoard board;
	// This is just added as a member variable so it can be stored form the time
	// its constructed until the time init is invoked.
	// It should never be referenced after that. Always use
	// board.getController()
	// so controller swapping can occur.
	ChessBoardController controller;

	boolean isPassive = true;
	boolean isBughouseOtherBoard = false;

	public ChessBoardWindowItem(ChessBoardController controller) {
		this.controller = controller;
	}

	public ChessBoardWindowItem(ChessBoardController controller,
			boolean isBughouseOtherBoard) {
		this(controller);
		this.isBughouseOtherBoard = true;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
		getController().addItemChangedListener(listener);
	}

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {

	}

	public boolean confirmClose() {
		return board.getController().confirmClose();
	}

	public boolean confirmQuadrantMove() {
		return true;
	}

	public void dispose() {
		if (board != null) {
			ChessBoardCacheService.getInstance().recycle(board);
			controller = null;
			board = null;
		}
	}

	public ChessBoard getBoard() {
		return board;
	}

	public Control getControl() {
		return board.getControl();
	}

	public ChessBoardController getController() {
		return board == null || board.getController() == null ? controller
				: board.getController();
	}

	public Image getImage() {
		return null;
	}

	/**
	 * Returns a list of the quadrants this window item can move to.
	 */
	public Quadrant[] getMoveToQuadrants() {
		return MOVE_TO_QUADRANTS;
	}

	public Quadrant getPreferredQuadrant() {

		if (getController().getConnector() == null) {
			if (isBughouseOtherBoard) {
				return Raptor.getInstance().getPreferences().getQuadrant(
						PreferenceKeys.APP_BUGHOUSE_GAME_2_QUADRANT);
			} else {
				return Raptor.getInstance().getPreferences().getQuadrant(
						PreferenceKeys.APP_CHESS_BOARD_QUADRANT);
			}
		} else {
			if (isBughouseOtherBoard) {
				return Raptor.getInstance().getPreferences().getQuadrant(
						getController().getConnector().getShortName() + "-"
								+ PreferenceKeys.BUGHOUSE_GAME_2_QUADRANT);

			} else {
				return Raptor.getInstance().getPreferences().getQuadrant(
						getController().getConnector().getShortName() + "-"
								+ PreferenceKeys.CHESS_BOARD_QUADRANT);
			}
		}
	}

	public String getTitle() {
		return getController().getTitle();
	}

	public Control getToolbar(Composite parent) {
		return getController().getToolbar(parent);
	}

	public void init(Composite parent) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Initing ChessBoardWindowItem");
		}
		long startTime = System.currentTimeMillis();

		board = ChessBoardCacheService.getInstance().getChessBoard();
		if (board == null) {
			board = new ChessBoard();
			board.setController(controller);
			controller.setBoard(board);
			board.createControls(parent);
		} else {
			board.getControl().setParent(parent);
			board.setController(controller);
			controller.setBoard(board);
		}

		// board.getControl().setLayoutDeferred(true);
		board.getController().init();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Inited window item in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	public void onActivate() {
		if (isPassive) {
			board.getControl().layout(true);
			board.getController().onActivate();
			isPassive = false;
		}
	}

	public void onPassivate() {
		if (!isPassive) {
			board.getController().onPassivate();
			isPassive = true;
		}
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
		getController().removeItemChangedListener(listener);
	}
}
