package raptor.game;

public class SetupGame extends Game {

	public SetupGame() {
		super();
		addState(Game.SETUP_STATE);
		addState(Game.DROPPABLE_STATE);
	}

	@Override
	public boolean isLegalPosition() {
		return isInState(Game.SETUP_STATE) ? true : super.isLegalPosition();
	}
}
