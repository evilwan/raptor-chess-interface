package raptor.game;

public class AtomicGame extends Game {

	public AtomicGame() {
		setType(Type.ATOMIC);
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
