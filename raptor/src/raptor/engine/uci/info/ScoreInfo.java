package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;

public class ScoreInfo extends UCIInfo {
	protected int valueInCentipawns;
	protected int mateInMoves;
	protected boolean isLowerBoundScore;
	protected boolean isUpperBoundScore;

	public int getMateInMoves() {
		return mateInMoves;
	}

	public int getValueInCentipawns() {
		return valueInCentipawns;
	}

	public boolean isLowerBoundScore() {
		return isLowerBoundScore;
	}

	public boolean isUpperBoundScore() {
		return isUpperBoundScore;
	}

	public void setLowerBoundScore(boolean isLowerBoundScore) {
		this.isLowerBoundScore = isLowerBoundScore;
	}

	public void setMateInMoves(int mateInMoves) {
		this.mateInMoves = mateInMoves;
	}

	public void setUpperBoundScore(boolean isUpperBoundScore) {
		this.isUpperBoundScore = isUpperBoundScore;
	}

	public void setValueInCentipawns(int valueInCentipawns) {
		this.valueInCentipawns = valueInCentipawns;
	}
}
