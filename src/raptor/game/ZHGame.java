package raptor.game;

public class ZHGame extends Game {

	public ZHGame() {
		setType(Game.CRAZY_HOUSE);
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
