package raptor.swt.chess.movelist;

import java.util.ArrayList;
import java.util.List;

import raptor.chess.Game;

/**
 * MoveListVariation is a class to contains all information about a variation
 * which is painted onto TextAreaMoveList. Uses the separate copy of Game object
 * to represent the game state.
 */
public class MoveListVariation {
	private List<Integer> moveNodes = new ArrayList<Integer>();
	private List<Integer> moveNodesLengths = new ArrayList<Integer>();
	private List<String> moveSans = new ArrayList<String>();
	private int startingMove;
	private int startOffset;
	private Game varGame;
	private int paintedMovesCounter;
	private int totalLength;
	private int nextNodeOffset;

	public MoveListVariation(int startingMove, int startOffset, Game varGame) {
		this.startingMove = startingMove;
		this.startOffset = startOffset + 3;
		this.varGame = varGame;
	}

	public void addMove(String pseudoSan) {
		moveSans.add(pseudoSan);
	}

	public int getStartingMove() {
		return startingMove;
	}

	public List<String> getMoveSans() {
		return moveSans;
	}

	public Game getVarGame() {
		return varGame;
	}

	public List<Integer> getMoveNodes() {
		return moveNodes;
	}

	public void increasePaintedMovesCounter() {
		paintedMovesCounter++;
	}

	public int getPaintedMovesCounter() {
		return paintedMovesCounter;
	}

	public int getTotalLength() {
		return totalLength;
	}

	public void increaseTotalLengthBy(int val) {
		this.totalLength += val;
	}

	public int getNextNodeOffset() {
		return nextNodeOffset;
	}

	public void setNextNodeOffset(int nextNodeOffset) {
		this.nextNodeOffset = nextNodeOffset;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public List<Integer> getMoveNodesLengths() {
		return moveNodesLengths;
	}
}
