package raptor.problemgen;

import raptor.engine.uci.UCIMove;
import raptor.engine.uci.info.ScoreInfo;

public class ProblemResult {
	protected String fen;
	protected UCIMove[] line;
	protected ScoreInfo score;

	public ProblemResult(String fen, UCIMove[] line, ScoreInfo score) {
		this.fen = fen;
		this.line = line;
		this.score = score;
	}

	public String getFen() {
		return fen;
	}

	public UCIMove[] getLine() {
		return line;
	}

	public ScoreInfo getScore() {
		return score;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	public void setLine(UCIMove[] line) {
		this.line = line;
	}

	public void setScore(ScoreInfo score) {
		this.score = score;
	}
}
