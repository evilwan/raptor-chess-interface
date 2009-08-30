package raptor.game;

import java.util.ArrayList;
import java.util.List;

public final class MoveList implements GameConstants {
	private Move[] moves = new Move[MAX_HALF_MOVES_IN_GAME];
	private int size = 0;

	public void append(Move move) {
		moves[size++] = move;
	}

	@SuppressWarnings("unused")
	private void dump(String message) {
		System.err.println(message);
		for (int i = 0; i < size; i++) {
			System.err.println(moves[i]);
		}
	}

	public Move removeLast() {
		return moves[--size];
	}

	public String toString() {
		return asList().toString();
	}

	public int getSize() {
		return size;
	}

	public Move getLast() {
		return moves[size - 1];
	}

	public Move get(int index) {
		return moves[index];
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
}
