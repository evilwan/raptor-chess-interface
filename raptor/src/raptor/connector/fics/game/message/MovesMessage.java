package raptor.connector.fics.game.message;

import java.util.Arrays;

public class MovesMessage {
	public String gameId;
	public String[] moves;
	public Long[] timePerMove;

	@Override
	public String toString() {
		return "MovesMessage: " + gameId + " " + Arrays.toString(moves) + " "
				+ Arrays.toString(timePerMove);
	}
}
