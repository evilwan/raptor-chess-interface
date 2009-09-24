package raptor.game.util;

import java.util.List;

import raptor.game.Game;
import raptor.game.Move;

public class TestCase {
	public void asserts(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	public void asserts(List<Move> listA, List<Move> listB, Game game) {
		for (Move moveA : listA) {
			int foundIndex = -1;
			for (int i = 0; i < listB.size(); i++) {
				if (listB.get(i).getLan().equals(moveA.getLan())) {
					foundIndex = i;
					break;
				}
			}
			if (foundIndex == -1) {
				throw new AssertionError("Could not find move "
						+ moveA.getLan() + " in " + listB + "\n" + game);
			} else {
				listB.remove(foundIndex);
			}
		}

		if (listB.size() != 0) {
			throw new AssertionError(
					"The following moves were not found in listA " + listB
							+ "\n" + game);
		}
	}

	public void assertsContains(List<Move> moveList, String lan) {
		boolean found = false;

		for (Move candidate : moveList) {
			if (candidate.getLan().equals(lan)) {
				found = true;
				break;
			}
		}

		asserts(found == true, "Could not find move " + lan + " in " + moveList);
	}

	public void assertsDoesntContains(List<Move> moveList, String lan) {
		boolean found = false;

		for (Move candidate : moveList) {
			if (candidate.getLan().equals(lan)) {
				found = true;
				break;
			}
		}

		asserts(found == false, "Found move " + lan + " in " + moveList);
	}

	public boolean contains(String string, String[] array) {
		boolean result = false;
		for (int i = 0; !result && i < array.length; i++) {
			result = array[i].equals(string);
		}
		return result;
	}
}
