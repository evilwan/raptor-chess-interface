package raptor.game;

public class LosersGame extends Game {
	
	public LosersGame() {
		setType(Game.LOSERS);
	}
	
	@Override
	public PriorityMoveList getLegalMoves() {
		return null;
	}
	
	@Override
	public boolean isLegalPosition() {
		return false;
	}
}
