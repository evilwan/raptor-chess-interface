package raptor.game;

public interface GameConstants {
	public static final int MOVE_REP_CACHE_SIZE = (1 << 12);
	public static final int MOVE_REP_CACHE_SIZE_MINUS_1 = MOVE_REP_CACHE_SIZE - 1;

	public static final int MAX_HALF_MOVES_IN_GAME = 500;
	public static final int MAX_LEGAL_MOVES = 500;

	// bitboard coordinate constants
	public static final long A1 = 1L;
	public static final long B1 = A1 << 1;
	public static final long C1 = A1 << 2;
	public static final long D1 = A1 << 3;
	public static final long E1 = A1 << 4;
	public static final long F1 = A1 << 5;
	public static final long G1 = A1 << 6;
	public static final long H1 = A1 << 7;

	public static final long A2 = A1 << 8;
	public static final long B2 = A1 << 9;
	public static final long C2 = A1 << 10;
	public static final long D2 = A1 << 11;
	public static final long E2 = A1 << 12;
	public static final long F2 = A1 << 13;
	public static final long G2 = A1 << 14;
	public static final long H2 = A1 << 15;

	public static final long A3 = A1 << 16;
	public static final long B3 = A1 << 17;
	public static final long C3 = A1 << 18;
	public static final long D3 = A1 << 19;
	public static final long E3 = A1 << 20;
	public static final long F3 = A1 << 21;
	public static final long G3 = A1 << 22;
	public static final long H3 = A1 << 23;

	public static final long A4 = A1 << 24;
	public static final long B4 = A1 << 25;
	public static final long C4 = A1 << 26;
	public static final long D4 = A1 << 27;
	public static final long E4 = A1 << 28;
	public static final long F4 = A1 << 29;
	public static final long G4 = A1 << 30;
	public static final long H4 = A1 << 31;

	public static final long A5 = A1 << 32;
	public static final long B5 = A1 << 33;
	public static final long C5 = A1 << 34;
	public static final long D5 = A1 << 35;
	public static final long E5 = A1 << 36;
	public static final long F5 = A1 << 37;
	public static final long G5 = A1 << 38;
	public static final long H5 = A1 << 39;

	public static final long A6 = A1 << 40;
	public static final long B6 = A1 << 41;
	public static final long C6 = A1 << 42;
	public static final long D6 = A1 << 43;
	public static final long E6 = A1 << 44;
	public static final long F6 = A1 << 45;
	public static final long G6 = A1 << 46;
	public static final long H6 = A1 << 47;

	public static final long A7 = A1 << 48;
	public static final long B7 = A1 << 49;
	public static final long C7 = A1 << 50;
	public static final long D7 = A1 << 51;
	public static final long E7 = A1 << 52;
	public static final long F7 = A1 << 53;
	public static final long G7 = A1 << 54;
	public static final long H7 = A1 << 55;

	public static final long A8 = A1 << 56;
	public static final long B8 = A1 << 57;
	public static final long C8 = A1 << 58;
	public static final long D8 = A1 << 59;
	public static final long E8 = A1 << 60;
	public static final long F8 = A1 << 61;
	public static final long G8 = A1 << 62;
	public static final long H8 = A1 << 63;

	public static final int SQUARE_A1 = 0;
	public static final int SQUARE_B1 = 1;
	public static final int SQUARE_C1 = 2;
	public static final int SQUARE_D1 = 3;
	public static final int SQUARE_E1 = 4;
	public static final int SQUARE_F1 = 5;
	public static final int SQUARE_G1 = 6;
	public static final int SQUARE_H1 = 7;

	public static final int SQUARE_A2 = 8;
	public static final int SQUARE_B2 = 9;
	public static final int SQUARE_C2 = 10;
	public static final int SQUARE_D2 = 11;
	public static final int SQUARE_E2 = 12;
	public static final int SQUARE_F2 = 13;
	public static final int SQUARE_G2 = 14;
	public static final int SQUARE_H2 = 15;

	public static final int SQUARE_A3 = 16;
	public static final int SQUARE_B3 = 17;
	public static final int SQUARE_C3 = 18;
	public static final int SQUARE_D3 = 19;
	public static final int SQUARE_E3 = 20;
	public static final int SQUARE_F3 = 21;
	public static final int SQUARE_G3 = 22;
	public static final int SQUARE_H3 = 23;

	public static final int SQUARE_A4 = 24;
	public static final int SQUARE_B4 = 25;
	public static final int SQUARE_C4 = 26;
	public static final int SQUARE_D4 = 27;
	public static final int SQUARE_E4 = 28;
	public static final int SQUARE_F4 = 29;
	public static final int SQUARE_G4 = 30;
	public static final int SQUARE_H4 = 31;

	public static final int SQUARE_A5 = 32;
	public static final int SQUARE_B5 = 33;
	public static final int SQUARE_C5 = 34;
	public static final int SQUARE_D5 = 35;
	public static final int SQUARE_E5 = 36;
	public static final int SQUARE_F5 = 37;
	public static final int SQUARE_G5 = 38;
	public static final int SQUARE_H5 = 39;

	public static final int SQUARE_A6 = 40;
	public static final int SQUARE_B6 = 41;
	public static final int SQUARE_C6 = 42;
	public static final int SQUARE_D6 = 43;
	public static final int SQUARE_E6 = 44;
	public static final int SQUARE_F6 = 45;
	public static final int SQUARE_G6 = 46;
	public static final int SQUARE_H6 = 47;

	public static final int SQUARE_A7 = 48;
	public static final int SQUARE_B7 = 49;
	public static final int SQUARE_C7 = 50;
	public static final int SQUARE_D7 = 51;
	public static final int SQUARE_E7 = 52;
	public static final int SQUARE_F7 = 53;
	public static final int SQUARE_G7 = 54;
	public static final int SQUARE_H7 = 55;

	public static final int SQUARE_A8 = 56;
	public static final int SQUARE_B8 = 57;
	public static final int SQUARE_C8 = 58;
	public static final int SQUARE_D8 = 59;
	public static final int SQUARE_E8 = 60;
	public static final int SQUARE_F8 = 61;
	public static final int SQUARE_G8 = 62;
	public static final int SQUARE_H8 = 63;

	// Castle state constants.
	public static final int CASTLE_NONE = 0;
	public static final int CASTLE_KINGSIDE = 1;
	public static final int CASTLE_QUEENSIDE = 2;
	public static final int CASTLE_BOTH = CASTLE_KINGSIDE | CASTLE_QUEENSIDE;

	// Direction constants.
	public static final int NORTH = 0;
	public static final int SOUTH = 2;
	public static final int EAST = 4;
	public static final int WEST = 8;
	public static final int NORTHEAST = 16;
	public static final int NORTHWEST = 32;
	public static final int SOUTHEAST = 64;
	public static final int SOUTHWEST = 128;

	// Rank bitmaps
	public static final long RANK1 = A1 | B1 | C1 | D1 | E1 | F1 | G1 | H1;
	public static final long RANK2 = A2 | B2 | C2 | D2 | E2 | F2 | G2 | H2;
	public static final long RANK3 = A3 | B3 | C3 | D3 | E3 | F3 | G3 | H3;
	public static final long RANK4 = A4 | B4 | C4 | D4 | E4 | F4 | G4 | H4;
	public static final long RANK5 = A5 | B5 | C5 | D5 | E5 | F5 | G5 | H5;
	public static final long RANK6 = A6 | B6 | C6 | D6 | E6 | F6 | G6 | H6;
	public static final long RANK7 = A7 | B7 | C7 | D7 | E7 | F7 | G7 | H7;
	public static final long RANK8 = A8 | B8 | C8 | D8 | E8 | F8 | G8 | H8;

	public static final long RANK8_OR_RANK1 = RANK1 | RANK8;

	public static final long NOT_RANK1 = ~RANK1;
	public static final long NOT_RANK2 = ~RANK2;
	public static final long NOT_RANK3 = ~RANK3;
	public static final long NOT_RANK4 = ~RANK4;
	public static final long NOT_RANK5 = ~RANK5;
	public static final long NOT_RANK6 = ~RANK6;
	public static final long NOT_RANK7 = ~RANK7;
	public static final long NOT_RANK8 = ~RANK8;

	// File bitmaps
	public static final long AFILE = A1 | A2 | A3 | A4 | A5 | A6 | A7 | A8;
	public static final long BFILE = B1 | B2 | B3 | B4 | B5 | B6 | B7 | B8;
	public static final long CFILE = C1 | C2 | C3 | C4 | C5 | C6 | C7 | C8;
	public static final long DFILE = D1 | D2 | D3 | D4 | D5 | D6 | D7 | D8;
	public static final long EFILE = E1 | E2 | E3 | E4 | E5 | E6 | E7 | E8;
	public static final long FFILE = F1 | F2 | F3 | F4 | F5 | F6 | F7 | F8;
	public static final long GFILE = G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8;
	public static final long HFILE = H1 | H2 | H3 | H4 | H5 | H6 | H7 | H8;

	public static final long NOT_AFILE = ~AFILE;
	public static final long NOT_BFILE = ~BFILE;
	public static final long NOT_CFILE = ~CFILE;
	public static final long NOT_DFILE = ~DFILE;
	public static final long NOT_EFILE = ~EFILE;
	public static final long NOT_FFILE = ~FFILE;
	public static final long NOT_GFILE = ~GFILE;
	public static final long NOT_HFILE = ~HFILE;

	// Piece constants.
	public static final int EMPTY = 0;
	public static final int PAWN = 1;
	public static final int BISHOP = 2;
	public static final int KNIGHT = 3;
	public static final int ROOK = 4;
	public static final int QUEEN = 5;
	public static final int KING = 6;
	
	public static final int PROMOTED_MASK = 8;
	public static final int NOT_PROMOTED_MASK = 7;
	public static final int PROMOTED_BISHOP = BISHOP | PROMOTED_MASK;
	public static final int PROMOTED_KNIGHT = KNIGHT | PROMOTED_MASK;
	public static final int PROMOTED_ROOK = ROOK | PROMOTED_MASK;
	public static final int PROMOTED_QUEEN = QUEEN | PROMOTED_MASK;
	
	
	//Colored piece constants. 
	//*NOTE* These are not used in the game class, 
	//however they are useful for other classes.
	public static final int WP = 1;
	public static final int WB = 2;
	public static final int WN = 3;
	public static final int WR = 4;
	public static final int WQ = 5;
	public static final int WK = 6;
	public static final int BP = 7;
	public static final int BB = 8;
	public static final int BN = 9;
	public static final int BR = 10;
	public static final int BQ = 11;
	public static final int BK = 12;

	// Color constants.
	public static final int WHITE = 0;
	public static final int BLACK = 1;

	public static final int EMPTY_SQUARE = 64;

	public static final String SQUARE_TO_FILE_SAN = "abcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghx";

	public static final String SQUARE_TO_RANK_SAN = "1111111122222222333333334444444455555555666666667777777788888888x";

	public static final String RANK_FROM_SAN = "12345678";

	public static final String FILE_FROM_SAN = "abcdefgh";

	public static final String[] COLOR_DESCRIPTION = new String[] { "White",
			"Black" };

	public static final String PIECE_TO_SAN = " PBNRQK";

	public static final String[] COLOR_PIECE_TO_CHAR = { "*PBNRQK", "*pbnrqk" };

	public static final String STARTING_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

	public static final long[] ZERO_BASED_RANK_INDEX_TO_BB = { RANK1, RANK2,
			RANK3, RANK4, RANK5, RANK6, RANK7, RANK8 };

	public static final long[] ZERO_BASED_FILE_INDEX_TO_BB = { AFILE, BFILE,
			CFILE, DFILE, EFILE, FFILE, GFILE, HFILE };

	public static final long BORDER = AFILE | HFILE | RANK1 | RANK8;
	public static final long NOT_BORDER = ~BORDER;

	public static final String SPACES = "                                                                    ";
	public static long[] SQUARE_TO_COORDINATE = { A1, B1, C1, D1, E1, F1, G1,
			H1, A2, B2, C2, D2, E2, F2, G2, H2, A3, B3, C3, D3, E3, F3, G3, H3,
			A4, B4, C4, D4, E4, F4, G4, H4, A5, B5, C5, D5, E5, F5, G5, H5, A6,
			B6, C6, D6, E6, F6, G6, H6, A7, B7, C7, D7, E7, F7, G7, H7, A8, B8,
			C8, D8, E8, F8, G8, H8, 0L };

	public static final long[] KING_START = { E1, E8 };
	public static final long[] ROOK_START = { A1 | H1, A8 | H8 };

}
