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
import raptor.swt.ItemChangedListener;

public class ChessBoardWindowItem implements RaptorWindowItem {
	static final Log LOG = LogFactory.getLog(ChessBoardWindowItem.class);

	ChessBoard board;
	boolean isPassive = true;

	// This is just added as a member variable so it can be stored form the time
	// its constructed until the time init is invoked.
	// It should never be referenced after that. Always use
	// board.getController()
	// so controller swapping can occur.
	ChessBoardController controller;

	public ChessBoardWindowItem(ChessBoardController controller) {
		this.controller = controller;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
		controller.addItemChangedListener(listener);
	}

	public boolean confirmClose() {
		return board.getController().confirmClose();
	}

	public boolean confirmQuadrantMove() {
		return true;
	}

	public void dispose() {
		board.dispose();
	}

	public Composite getControl() {
		return board;
	}

	public Image getImage() {
		return null;
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getCurrentLayoutQuadrant(
				PreferenceKeys.GAME_QUADRANT);
	}

	public String getTitle() {
		return board.getController().getTitle();
	}

	public Control getToolbar(Composite parent) {
		return board.getController().getToolbar(parent);
	}

	public void init(Composite parent) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Initing ChessBoardWindowItem");
		}
		long startTime = System.currentTimeMillis();
		board = new ChessBoard(parent);
		board.setLayoutDeferred(true);
		board.setController(controller);
		controller.setBoard(board);
		board.createControls();
		controller.init();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Inited window item in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	public void onActivate() {
		if (isPassive) {
			board.setLayoutDeferred(false);
			board.layout(true);
			board.getDisplay().asyncExec(new Runnable() {
				public void run() {
					board.getController().onActivate();
				}
			});
			isPassive = false;
		}
	}

	public void onPassivate() {
		if (!isPassive) {
			board.setLayoutDeferred(true);
			board.getDisplay().asyncExec(new Runnable() {
				public void run() {
					board.getController().onPassivate();
				}
			});
			isPassive = true;
		}
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
		board.getController().removeItemChangedListener(listener);
	}
}
