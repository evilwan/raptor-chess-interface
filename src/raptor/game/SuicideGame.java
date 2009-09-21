package raptor.game;

public class SuicideGame extends Game {
	
	public SuicideGame() {
		setType(Game.SUICIDE);
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
