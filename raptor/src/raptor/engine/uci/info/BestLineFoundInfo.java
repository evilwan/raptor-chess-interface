package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;
import raptor.engine.uci.UCIMove;

public class BestLineFoundInfo extends UCIInfo {
	protected UCIMove[] moves;

	public UCIMove[] getMoves() {
		return moves;
	}

	public void setMoves(UCIMove[] moves) {
		this.moves = moves;
	}
}
