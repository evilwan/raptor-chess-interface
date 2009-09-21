package raptor.game.util;

import static raptor.game.util.GameUtils.createFromFen;
import raptor.game.Game;
import raptor.game.Move;

public class Benchmark {
	// manyLegal2MoveTest: mean(nps): 794466 stdv 56010 removing bitboard ops.
	// moveWalk: mean(nps): 844927 stdv 63506 removing move gen
	// moveWalk: mean(nps): 850000 stdv 61165
	// moveWalk: mean(nps): 842320 stdv 56226
	// moveWalk: mean(nps): 845611 stdv 69194 adding finals/protecteds
	// moveWalk: mean(nps): 848537 stdv 63288 removing finals/protecteds
	// moveWalk: mean(nps): 746308 stdv 67651 switch isntead of array
	// moveWalk: mean(nps): 827691 stdv 60234 copying array mems to vars
	// moveWalk: mean(nps): 845798 stdv 61297 arrays
	// moveWalk: mean(nps): 851025 stdv 57688 making class final.
	// moveWalk: mean(nps): 986527 stdv 67375 Using MoveList instead of list.
	// moveWalk: mean(nps): 977845 stdv 120893 Replaced getOppositeColor with
	// ^^^ op.
	// moveWalk: mean(nps): 278567 stdv 28412 Switching to hyperbola quessence.
	// moveWalk: mean(nps): 587576 stdv 49314
	// moveWalk: mean(nps): 596447 stdv 62373
	// moveWalk: mean(nps): 1398600 stdv 73276 12275000 Fixed H8 >> >>> bug.
	// moveWalk: mean(nps): 1433890 stdv 105152 12275000
	// moveWalk: mean(nps): 1366489 stdv 78956 12532675
	// moveWalk: mean(nps): 1584883 stdv 87460 13137425 (with eval) additional
	// tweaks (question speed increase)
	// moveWalk: mean(nps): 1301252 stdv 58084 13137425 (with eval) bug fixes.
	// (Code runs slower with above test something strange was going on).
	// moveWalk: mean(nps): 1354747 stdv 90034 13137425
	// moveWalk: mean(nps): 1380615 stdv 65890 13137425 removed branch.

	// moveWalk: mean(nps): 930622 stdv 84919 2563400
	// moveWalk: mean(nps): 912616 stdv 78894 2563400 (adding game rules).
	// moveWalk: mean(nps): 889397 stdv 89598 2559200 (ep bug fix).
	// moveWalk: mean(nps): 1287302 stdv 102786 2623180 (reboot).
	// moveWalk: mean(nps): 1324255 stdv 40644 2623180 (adding drops).
	private static long counter = 0;

	public static void main(String args[]) {
		// Prime:
		for (int i = 0; i < 5; i++) {
			Game game = createFromFen("rnbqkbnr/p2ppp2/6pp/1pp5/1PP5/6PP/P2PPP2/RNBQKBNR w KQkq - 0 1",Game.STANDARD);
			moveWalk(game);
		}

		// Now run:
		int n = 200;
		long[] trials = new long[n];

		for (int i = 0; i < n; i++) {
			Game game = createFromFen("rnbqkbnr/p2ppp2/6pp/1pp5/1PP5/6PP/P2PPP2/RNBQKBNR w KQkq - 0 1",Game.STANDARD);
			long startTime = System.nanoTime();
			long nodes = moveWalk(game);

			double totalTime = (System.nanoTime() - startTime) / 1000000000.0;
			trials[i] = (long) (nodes / totalTime);
		}

		System.err.println("moveWalk: mean(nps): " + mean(trials) + " stdv "
				+ stdv(trials) + " " + counter);
	}

	public static long mean(long[] samples) {
		long total = 0;
		for (long sample : samples) {
			total += sample;
		}
		return total / samples.length;
	}

	public static long moveWalk(Game game) {
		return moveWalk(game, 0, 3);
	}

	private static long moveWalk(Game game, int depth, int maxDepth) {
		counter++;
		long result = 1;

		if (depth < maxDepth) {
			Move[] moves = game.getPseudoLegalMoves().asArray();

			for (int j = 0; j < moves.length; j++) {
				if (game.move(moves[j])) {
					result += moveWalk(game, depth + 1, maxDepth);
					game.rollback();
				}
			}

			// PriorityMoveList moves = game.getPseudoLegalMoves();
			//
			// for (int j = 0; j < moves.getHighPrioritySize(); j++) {
			// if (game.move(moves.getHighPriority(j))) {
			// result += moveWalk(game, depth + 1, maxDepth);
			// EvalUtil.eval(game);
			// game.rollback();
			// } else {
			// // moves.removeHighPriority(j);
			// }
			// }
			//
			// for (int k = 0; k < moves.getLowPrioritySize(); k++) {
			// if (game.move(moves.getLowPriority(k))) {
			// result += moveWalk(game, depth + 1, maxDepth);
			// EvalUtil.eval(game);
			// game.rollback();
			// } else {
			// // moves.removeLowPriority(k);
			// }
			// }
		}

		return result;
	}

	public static long stdv(long[] samples) {
		long mean = mean(samples);

		long accum = 0;

		for (long sample : samples) {
			accum += (long) Math.pow(sample - mean, 2);
		}

		return (long) Math.sqrt(accum / samples.length);
	}
}
