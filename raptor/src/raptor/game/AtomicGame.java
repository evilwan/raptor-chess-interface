package raptor.game;

public class AtomicGame extends Game {

	public AtomicGame() {
		setType(Type.ATOMIC);
	}

	@Override
	public PriorityMoveList getLegalMoves() {
		PriorityMoveList result = getPseudoLegalMoves();
		
		for (int i = 0; i < result.getHighPrioritySize(); i++) {
			Move move = result.getHighPriority(i);
			forceMove(move);

			if (!isLegalPosition()) {
				result.removeHighPriority(i);
				i--;
			}
		}
		
		return result;
	}

	@Override
	public boolean isLegalPosition() {
		return true;
	}
}
