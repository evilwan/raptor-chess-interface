package raptor.game.util;

import static raptor.game.util.GameUtils.bitscanClear;
import static raptor.game.util.GameUtils.bitscanForward;

import java.security.SecureRandom;
import java.util.Random;

import raptor.game.Game;
import raptor.game.GameConstants;

public final class ZobristHash implements GameConstants {

	private static long[][][] ZOBRIST_POSITION = new long[2][7][64];
	private static long[] ZOBRIST_TO_MOVE = new long[2];
	private static long[] ZOBRIST_EP = new long[65];
	private static long[][] ZOBRIST_CASTLE = new long[2][4];
	private static long[][][] ZOBRIST_DROP_COUNT = new long[2][7][18];

	static {
		initZobrist();
	}

	private static void initZobrist() {
		Random random = new SecureRandom();

		for (int i = 0; i < ZOBRIST_DROP_COUNT.length; i++) {
			for (int j = 0; j < ZOBRIST_DROP_COUNT[i].length; j++) {
				for (int k = 0; k < ZOBRIST_DROP_COUNT[i][j].length; k++) {
					ZOBRIST_DROP_COUNT[i][j][k] = random.nextLong();
				}
			}
		}

		for (int i = 0; i < ZOBRIST_POSITION.length; i++) {
			for (int j = 0; j < ZOBRIST_POSITION[i].length; j++) {
				for (int k = 0; k < ZOBRIST_POSITION[i][j].length; k++) {
					ZOBRIST_POSITION[i][j][k] = random.nextLong();
				}
			}
		}

		for (int i = 0; i < ZOBRIST_TO_MOVE.length; i++) {
			ZOBRIST_TO_MOVE[i] = random.nextLong();
		}

		for (int i = 0; i < ZOBRIST_EP.length; i++) {
			ZOBRIST_EP[i] = random.nextLong();
		}

		for (int i = 0; i < ZOBRIST_CASTLE.length; i++) {
			for (int j = 0; j < ZOBRIST_CASTLE[i].length; j++) {
				ZOBRIST_CASTLE[i][j] = random.nextLong();
			}
		}
	}

	public static long zobrist(int color, int piece, int square) {
		return ZOBRIST_POSITION[color][piece][square];
	}

	public static long zobrist(int colorToMove, int epSquare,
			int whiteCastling, int blackCastling) {
		return ZOBRIST_TO_MOVE[colorToMove] ^ ZOBRIST_EP[epSquare]
				^ ZOBRIST_CASTLE[WHITE][whiteCastling]
				^ ZOBRIST_CASTLE[BLACK][blackCastling];
	}

	public static long zobristDropPieces(Game game) {
		return ZOBRIST_DROP_COUNT[WHITE][PAWN][game.getDropCount(WHITE, PAWN)]
				^ ZOBRIST_DROP_COUNT[WHITE][PAWN][game.getDropCount(WHITE,
						KNIGHT)]
				^ ZOBRIST_DROP_COUNT[WHITE][PAWN][game.getDropCount(WHITE,
						BISHOP)]
				^ ZOBRIST_DROP_COUNT[WHITE][PAWN][game.getDropCount(WHITE,
						QUEEN)]
				^ ZOBRIST_DROP_COUNT[WHITE][PAWN][game
						.getDropCount(WHITE, ROOK)]
				^ ZOBRIST_DROP_COUNT[BLACK][PAWN][game
						.getDropCount(BLACK, PAWN)]
				^ ZOBRIST_DROP_COUNT[BLACK][PAWN][game.getDropCount(BLACK,
						KNIGHT)]
				^ ZOBRIST_DROP_COUNT[BLACK][PAWN][game.getDropCount(BLACK,
						BISHOP)]
				^ ZOBRIST_DROP_COUNT[BLACK][PAWN][game.getDropCount(BLACK,
						QUEEN)]
				^ ZOBRIST_DROP_COUNT[BLACK][PAWN][game
						.getDropCount(BLACK, ROOK)];
	}

	public static long zobristHash(Game game) {
		return zobristHashPositionOnly(game)
				^ zobrist(game.getColorToMove(), game.getEpSquare(), game
						.getCastling(WHITE), game.getCastling(BLACK));
	}

	public static long zobristHashPositionOnly(Game game) {
		return zobristPiece(WHITE, PAWN, game)
				^ zobristPiece(WHITE, BISHOP, game)
				^ zobristPiece(WHITE, KNIGHT, game)
				^ zobristPiece(WHITE, ROOK, game)
				^ zobristPiece(WHITE, QUEEN, game)
				^ zobristPiece(WHITE, KING, game)
				^ zobristPiece(BLACK, PAWN, game)
				^ zobristPiece(BLACK, BISHOP, game)
				^ zobristPiece(BLACK, KNIGHT, game)
				^ zobristPiece(BLACK, ROOK, game)
				^ zobristPiece(BLACK, QUEEN, game)
				^ zobristPiece(BLACK, KING, game);
	}

	private static long zobristPiece(int color, int piece, Game game) {
		int result = 0;
		long current = game.getPieceBB(color, piece);
		while (current != 0L) {
			result ^= zobrist(color, piece, bitscanForward(current));
			current = bitscanClear(current);
		}
		return result;
	}
}
