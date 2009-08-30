package raptor.game.util;

import java.security.SecureRandom;
import java.util.Random;

import raptor.game.Game;
import raptor.game.GameConstants;

import static raptor.game.util.GameUtils.*;

public final class ZobristHash implements GameConstants {

	private static int[][][] ZOBRIST_POSITION = new int[2][7][64];
	private static int[] ZOBRIST_TO_MOVE = new int[2];
	private static int[] ZOBRIST_EP = new int[65];
	private static int[][] ZOBRIST_CASTLE = new int[2][4];
	private static int[][][] ZOBRIST_DROP_COUNT = new int[2][7][18];

	static {
		initZobrist();
	}

	public static int zobristHashPositionOnly(Game game) {
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

	private static int zobristPiece(int color, int piece, Game game) {
		int result = 0;
		long current = game.getPieceBB(color, piece);
		while (current != 0L) {
			result ^= zobrist(color, piece, bitscanForward(current));
			current = bitscanClear(current);
		}
		return result;
	}

	public static int zobristHash(Game game) {
		return zobristHashPositionOnly(game)
				^ zobrist(game.getColorToMove(), game.getEpSquare(), game
						.getCastling(WHITE), game.getCastling(BLACK));
	}

	public static int zobrist(int color, int piece, int square) {
		return ZOBRIST_POSITION[color][piece][square];
	}

	public static int zobrist(int colorToMove, int epSquare, int whiteCastling,
			int blackCastling) {
		return ZOBRIST_TO_MOVE[colorToMove] ^ ZOBRIST_EP[epSquare]
				^ ZOBRIST_CASTLE[WHITE][whiteCastling]
				^ ZOBRIST_CASTLE[BLACK][blackCastling];
	}

	public static int zobristDropPieces(Game game) {
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

	private static void initZobrist() {
		Random random = new SecureRandom();

		for (int i = 0; i < ZOBRIST_DROP_COUNT.length; i++) {
			for (int j = 0; j < ZOBRIST_DROP_COUNT[i].length; j++) {
				for (int k = 0; k < ZOBRIST_DROP_COUNT[i][j].length; k++) {
					ZOBRIST_DROP_COUNT[i][j][k] = random.nextInt();
				}
			}
		}

		for (int i = 0; i < ZOBRIST_POSITION.length; i++) {
			for (int j = 0; j < ZOBRIST_POSITION[i].length; j++) {
				for (int k = 0; k < ZOBRIST_POSITION[i][j].length; k++) {
					ZOBRIST_POSITION[i][j][k] = random.nextInt();
				}
			}
		}

		for (int i = 0; i < ZOBRIST_TO_MOVE.length; i++) {
			ZOBRIST_TO_MOVE[i] = random.nextInt();
		}

		for (int i = 0; i < ZOBRIST_EP.length; i++) {
			ZOBRIST_EP[i] = random.nextInt();
		}

		for (int i = 0; i < ZOBRIST_CASTLE.length; i++) {
			for (int j = 0; j < ZOBRIST_CASTLE[i].length; j++) {
				ZOBRIST_CASTLE[i][j] = random.nextInt();
			}
		}
	}
}
