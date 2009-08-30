package raptor.game;

import java.util.ArrayList;
import java.util.List;

public final class PriorityMoveList implements GameConstants {
	private Move[] highPriorityMoves = new Move[MAX_LEGAL_MOVES];
	private Move[] lowPriorityMoves = new Move[MAX_LEGAL_MOVES];

	int highPrioritySize;
	int lowPrioritySize; 
	
	public int getSize() {
		return highPrioritySize + lowPrioritySize;  
	}

	public int getHighPrioritySize() {
		return highPrioritySize;
	}

	public int getLowPrioritySize() {
		return lowPrioritySize;
	}
	
	public void appendHighPriority(Move move) {
		highPriorityMoves[highPrioritySize++] = move;
	}

	public void appendLowPriority(Move move) {
		lowPriorityMoves[lowPrioritySize++] = move;
	}

	public Move getLowPriority(int index) {
		return lowPriorityMoves[index];
	}

	public Move getHighPriority(int index) {
		return highPriorityMoves[index];
	}

	public void removeLastHighPriority() {
		highPrioritySize--;
	}

	public void removeLastLowPriority() {
		lowPrioritySize--;
	}

	public void removeHighPriority(int index) {
		int size = highPrioritySize - 1;
		for (int i = index; i < size; i++) {
			highPriorityMoves[i] = highPriorityMoves[i + 1];
		}
		highPrioritySize--;
	}

	public void removeLowPriority(int index) {
		int size = lowPrioritySize - 1;
		for (int i = index; i < size; i++) {
			lowPriorityMoves[i] = lowPriorityMoves[i + 1];
		}
		lowPrioritySize--;
	}

	public Move[] asArray() {
		Move[] result = new Move[lowPrioritySize
				+ highPrioritySize];

		System.arraycopy(lowPriorityMoves, 0, result, 0, lowPrioritySize);
		System.arraycopy(highPriorityMoves, 0, result, lowPrioritySize,
				highPrioritySize);

		return result;
	}
	
	public List<Move> asList() {
		ArrayList<Move> result = new ArrayList<Move>(getSize());
		
		for (int i = 0; i < lowPrioritySize; i++) {
			result.add(lowPriorityMoves[i]);
		}
		for (int j = 0; j < highPrioritySize; j++) {
			result.add(highPriorityMoves[j]);
		} 
		
		return result;
	}

}
