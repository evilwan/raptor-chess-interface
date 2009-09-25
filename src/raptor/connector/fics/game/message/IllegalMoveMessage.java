package raptor.connector.fics.game.message;

public class IllegalMoveMessage {
	public String move;

	@Override
	public String toString() {
		return "IllegalMoveMessage: move=" + move;
	}
}