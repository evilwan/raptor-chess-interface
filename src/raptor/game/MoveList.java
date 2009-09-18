package raptor.game;

import java.util.ArrayList;
import java.util.List;

public final class MoveList implements GameConstants {
	private Move[] moves;
	private int size = 0;
	
	public MoveList() {
	   this(MAX_HALF_MOVES_IN_GAME);	
	}
	
	public MoveList(int maxSize) {
		moves = new Move[maxSize];
	}
	
	public MoveList deepCopy() {
		MoveList result = new MoveList();
		for (int i = 0; i < moves.length; i++) {
			result.moves[i] = moves[i];
		}
		return result;
	}

	public void append(Move move) {
		moves[size++] = move;
	}

	public Move[] asArray() {
		Move[] result = new Move[size];
		System.arraycopy(moves, 0, result, 0, size);
		return result;
	}

	public List<Move> asList() {
		List<Move> result = new ArrayList<Move>(size);
		for (int i = 0; i < size; i++) {
			result.add(moves[i]);
		}
		return result;
	}

	@SuppressWarnings("unused")
	private void dump(String message) {
		System.err.println(message);
		for (int i = 0; i < size; i++) {
			System.err.println(moves[i]);
		}
	}

	public Move get(int index) {
		return moves[index];
	}

	public Move getLast() {
		return moves[size - 1];
	}

	public int getSize() {
		return size;
	}

	public Move removeLast() {
		return moves[--size];
	}

	@Override
	public String toString() {
		return asList().toString();
	}
}
