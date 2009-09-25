package raptor.game;

public class FischerRandomGame extends Game {

	public FischerRandomGame() {
		setType(Game.FISCHER_RANDOM);
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
