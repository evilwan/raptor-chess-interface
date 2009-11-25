package raptor.engine.uci;

public class UCIBestMove {
	protected UCIMove bestMove;
	protected UCIMove ponderMove;

	public UCIMove getBestMove() {
		return bestMove;
	}

	public UCIMove getPonderMove() {
		return ponderMove;
	}

	public void setBestMove(UCIMove bestMove) {
		this.bestMove = bestMove;
	}

	public void setPonderMove(UCIMove ponderMove) {
		this.ponderMove = ponderMove;
	}
}
