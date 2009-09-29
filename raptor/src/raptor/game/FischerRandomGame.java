package raptor.game;

public class FischerRandomGame extends Game {

	public FischerRandomGame() {
		setType(Type.FISCHER_RANDOM);
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
