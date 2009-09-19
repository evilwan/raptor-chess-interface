package raptor.game;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MoveListTraverser {
	static final Log LOG = LogFactory.getLog(MoveListTraverser.class);

	Game sourceGame;
	Game traversrState;
	int traverserHalfMoveIndex;

	public MoveListTraverser(Game sourceGame) {
		this.sourceGame = sourceGame;
		adjustHalfMoveIndex();
	}
	
	public int getTraverserHalfMoveIndex() {
		return traverserHalfMoveIndex;
	}

	public void adjustHalfMoveIndex() {
		traverserHalfMoveIndex = sourceGame.halfMoveCount;
	}

	public void dispose() {
		sourceGame = null;
		traversrState = null;
	}

	public boolean hasNext() {
		return traverserHalfMoveIndex + 1 <= sourceGame.getHalfMoveCount();
	}

	public boolean hasBack() {
		LOG.debug("In hasPrevious travHMI=" + traverserHalfMoveIndex + " sourceHMI=" + sourceGame.getHalfMoveCount());
		return traverserHalfMoveIndex - 1 >= 1;
	}

	public boolean hasLast() {
		
		return traverserHalfMoveIndex != sourceGame.getHalfMoveCount();
	}

	public boolean hasFirst() {
		LOG.debug("In hasFirst travHMI=" + traverserHalfMoveIndex + " sourceHMI=" + sourceGame.getHalfMoveCount());
		return traverserHalfMoveIndex > 1;
	}

	public void next() {
		if (hasNext()) {
			traverserHalfMoveIndex++;
			synch();
		}
	}

	public void back() {
		if (hasBack()) {
			traverserHalfMoveIndex--;
			synch();
		}
	}

	public void last() {
		if (hasLast()) {
			traverserHalfMoveIndex = sourceGame.getHalfMoveCount();
			synch();
		}
	}

	public void first() {
		if (hasFirst()) {
			traverserHalfMoveIndex = 0;
			synch();
		}
	}

	public void gotoHalfMove(int moveNumber) {
		if (moveNumber >= 0 && moveNumber <= sourceGame.getHalfMoveCount()) {
			traverserHalfMoveIndex = moveNumber;
		}
	}

	public Game getAdjustedGame() {
		return traversrState;
	}

	public Game getSource() {
		return sourceGame;
	}

	private void synch() {
		traversrState = sourceGame.deepCopy(true);
		LOG.debug("Before loop in sync:" + traverserHalfMoveIndex + " sourceHMI=" + traversrState.getHalfMoveCount());
		while (traverserHalfMoveIndex < traversrState.getHalfMoveCount()) {
			LOG.debug("Looping in sync:" + traverserHalfMoveIndex + " sourceHMI=" + traversrState.getHalfMoveCount());
			traversrState.rollback();
		}
	}
}
