package raptor.game;

public class ZHGame extends Game {

	public ZHGame() {
		setType(Type.CRAZYHOUSE);
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
