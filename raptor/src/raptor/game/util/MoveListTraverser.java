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
package raptor.game.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.game.Game;

public class MoveListTraverser {
	static final Log LOG = LogFactory.getLog(MoveListTraverser.class);

	Game sourceGame;
	Game traversrState;
	int traverserHalfMoveIndex;

	public MoveListTraverser(Game sourceGame) {
		this.sourceGame = sourceGame;
		adjustHalfMoveIndex();
	}

	public void adjustHalfMoveIndex() {
		traverserHalfMoveIndex = sourceGame.getHalfMoveCount();
	}

	public void back() {
		if (hasBack()) {
			traverserHalfMoveIndex--;
			synch();
		}
	}

	public void dispose() {
		sourceGame = null;
		traversrState = null;
	}

	public void first() {
		if (hasFirst()) {
			traverserHalfMoveIndex = 0;
			synch();
		}
	}

	public Game getAdjustedGame() {
		return traversrState;
	}

	public Game getSource() {
		return sourceGame;
	}

	public int getTraverserHalfMoveIndex() {
		return traverserHalfMoveIndex;
	}

	public void gotoHalfMove(int moveNumber) {
		if (moveNumber >= 0 && moveNumber <= sourceGame.getHalfMoveCount()) {
			traverserHalfMoveIndex = moveNumber;
		}
	}

	public boolean hasBack() {
		LOG.debug("In hasPrevious travHMI=" + traverserHalfMoveIndex
				+ " sourceHMI=" + sourceGame.getHalfMoveCount());
		return traverserHalfMoveIndex - 1 >= 1;
	}

	public boolean hasFirst() {
		LOG.debug("In hasFirst travHMI=" + traverserHalfMoveIndex
				+ " sourceHMI=" + sourceGame.getHalfMoveCount());
		return traverserHalfMoveIndex > 1;
	}

	public boolean hasLast() {

		return traverserHalfMoveIndex != sourceGame.getHalfMoveCount();
	}

	public boolean hasNext() {
		return traverserHalfMoveIndex + 1 <= sourceGame.getHalfMoveCount();
	}

	public void last() {
		if (hasLast()) {
			traverserHalfMoveIndex = sourceGame.getHalfMoveCount();
			synch();
		}
	}

	public void next() {
		if (hasNext()) {
			traverserHalfMoveIndex++;
			synch();
		}
	}

	private void synch() {
		traversrState = sourceGame.deepCopy(true);
		LOG.debug("Before loop in sync:" + traverserHalfMoveIndex
				+ " sourceHMI=" + traversrState.getHalfMoveCount());
		while (traverserHalfMoveIndex < traversrState.getHalfMoveCount()) {
			LOG.debug("Looping in sync:" + traverserHalfMoveIndex
					+ " sourceHMI=" + traversrState.getHalfMoveCount());
			traversrState.rollback();
		}
	}
}
