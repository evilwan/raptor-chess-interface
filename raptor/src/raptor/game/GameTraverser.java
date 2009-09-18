package raptor.game;

public class GameTraverser {
    
	Game sourceGame;
	Game traversrState;
	int traverserHalfMoveIndex;
	
	public GameTraverser(Game sourceGame) {
		this.sourceGame = sourceGame;
	}
	
	public void dispose() {
		sourceGame = null;
		traversrState = null;
	}

	public boolean hasNext() {	
		return traverserHalfMoveIndex + 1 < sourceGame.getHalfMoveCount();
	}
	
	public boolean hasPrevious() {
		return traverserHalfMoveIndex - 1 > 0;
	}
	
	public boolean hasLast() {
		return traverserHalfMoveIndex != sourceGame.getHalfMoveCount();
	}
	
	public boolean hasFirst() {
		return traverserHalfMoveIndex != 0;
	}
	
	public void next() {
		if (hasNext()) {
			traverserHalfMoveIndex++;
			synch();
		}
	}
	
	public void previous() {
		if (hasPrevious()) {
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
	
	public Game getCurrnentGame() {	
		return traversrState;
	}
	
	public Game getSource() {	
		return sourceGame;
	}	
	
	private void synch() {
		traversrState = sourceGame.deepCopy(true);
		while(traverserHalfMoveIndex != sourceGame.getHalfMoveCount()) {
			traversrState.rollback();
		}
	}
}
