package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;
import raptor.engine.uci.UCIMove;

public class CurrentMoveInfo extends UCIInfo {
	protected UCIMove move;
	protected int moveNumber;

	public UCIMove getMove() {
		return move;
	}

	public int getMoveNumber() {
		return moveNumber;
	}

	public void setMove(UCIMove move) {
		this.move = move;
	}

	public void setMoveNumber(int moveNumber) {
		this.moveNumber = moveNumber;
	}
}
