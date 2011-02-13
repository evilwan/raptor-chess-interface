/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.chess;

import raptor.chess.pgn.PgnHeader;

/**
 * A game class which uses bitboards,Zobrist hash keys, and a reptition hash
 * table.. Games may contain PgnHeaders to store useful data about the game
 * (e.g. whites name, whites remaining time, etc).
 * 
 * Games also contain a state. The state is a bitmask of the various *_STATE
 * constants in this class. You can add to or remove a games state flag.
 * 
 * PgnHeaders can effect the way a game is displayed. You can set various
 * headers to effect the clocks, if white pieces are on top, the result, etc.
 */
public interface Game extends GameConstants {
	/**
	 * The active state bitmask. Set when a game is actively being played.
	 */
	public static final int ACTIVE_STATE = 1;

	/**
	 * The droppable state bitmask. Set when the game is a droppable game, e.g.
	 * crazyhouse or bughouse. Set when the game is not timed.
	 */
	public static final int DROPPABLE_STATE = ACTIVE_STATE << 5;

	/**
	 * The fischer random state bitmask. Set when the game uses FR castling
	 * rules game, e.g. fischer random, fischer random zh, fischer random
	 * bughouse.
	 */
	public static final int FISCHER_RANDOM_STATE = ACTIVE_STATE << 12;

	/**
	 * The examining state bitmask. Set when a game is in an examined state.
	 */
	public static final int EXAMINING_STATE = ACTIVE_STATE << 2;

	/**
	 * The inactive state bitmask. Set when a game is no longer being played.
	 */
	public static final int INACTIVE_STATE = ACTIVE_STATE << 1;

	/**
	 * The clock is ticking state bitmask. Set when the color to moves clock is
	 * ticking.
	 */
	public static final int IS_CLOCK_TICKING_STATE = ACTIVE_STATE << 6;

	/**
	 * The observing an examined game bitmask Set when the game is an
	 * observation of an examinied game.
	 */
	public static final int OBSERVING_EXAMINED_STATE = ACTIVE_STATE << 7;

	/**
	 * The observing state bitmask. Set when the game is an observation.
	 */
	public static final int OBSERVING_STATE = ACTIVE_STATE << 8;

	/**
	 * The playing state bitmask. Set when a user is playing the game.
	 */
	public static final int PLAYING_STATE = ACTIVE_STATE << 3;

	/**
	 * The setup bitmask. Set when the game is in a setup state e.g. the
	 * position is being set up.
	 */
	public static final int SETUP_STATE = ACTIVE_STATE << 9;

	/**
	 * The untimed state bitmask. Set when the game is not timed.
	 */
	public static final int UNTIMED_STATE = ACTIVE_STATE << 4;

	/**
	 * The updating eco headers bitmask. Set when the game is updating eco
	 * header information. Should be turned off for engines.
	 */
	public static final int UPDATING_ECO_HEADERS_STATE = ACTIVE_STATE << 10;

	/**
	 * The setup bitmask. Set when the game is update san on the moves made.
	 * Should be turned off for engines.
	 */
	public static final int UPDATING_SAN_STATE = ACTIVE_STATE << 11;

	/**
	 * Adds the state flag to the games state.
	 */
	public void addState(int state);

	/**
	 * Returns true if a king of each color is on the board.
	 */
	public boolean areBothKingsOnBoard();

	public boolean canBlackCastleLong();

	public boolean canBlackCastleShort();

	public boolean canWhiteCastleLong();

	public boolean canWhiteCastleShort();

	/**
	 * Sets the state of this game to an empty board. Does not clear the
	 * headers, the state, or the games id.
	 */
	public void clear();

	/**
	 * Clears the specified state constant from the games state.
	 */
	public void clearState(int state);

	/**
	 * @param ignoreHashes
	 *            Whether to include copying hash tables.
	 * @return An deep clone copy of this Game object.
	 */
	public Game deepCopy(boolean ignoreHashes);

	/**
	 * Makes a move with out any legality checking.
	 */
	public void forceMove(Move move);

	/**
	 * Returns an array of all of the pgn headers set for this game.
	 */
	public PgnHeader[] getAllHeaders();

	/**
	 * Returns an array of all of the non required headers set for this game.
	 */
	public PgnHeader[] getAllNonRequiredHeaders();

	/**
	 * Returns the games board. This is an int[64] containg non-colored piece
	 * constants where there are pieces.
	 * 
	 * @param moves
	 *            A move list.
	 */
	public int[] getBoard();

	/**
	 * Returns the castling constant for the specified color.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @return The castling constant.
	 */
	public int getCastling(int color);

	/**
	 * Returns a bitboard with 1s in the squares of the pieces of the specified
	 * color.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 */
	public long getColorBB(int color);

	/**
	 * Returns the color to move, WHITE or BLACK.
	 */
	public int getColorToMove();

	/**
	 * Returns the drop piece count for the specified color and piece. This is
	 * useful for games such as bughouse or crazyhosue
	 */
	public int getDropCount(int color, int piece);

	/**
	 * Returns a bitboard with 1s in the squares which are empty.
	 * 
	 * @return
	 */
	public long getEmptyBB();

	/**
	 * If the last move was a double pawn push, this method returns the square
	 * otherwise returns EMPTY.
	 */
	public int getEpSquare();

	/**
	 * Returns the castle part of the fen string.
	 */
	public String getFenCastle();

	/**
	 * Returns the number of half moves since the last irreversible move. This
	 * is useful for determining the 50 move draw rule.
	 */
	public int getFiftyMoveCount();

	/**
	 * Returns the full move count. The next move will have this number.
	 */
	public int getFullMoveCount();

	/**
	 * Returns the games half moves count.
	 */
	public int getHalfMoveCount();

	/**
	 * Returns the value of the PGN header for this game. Returns null if a
	 * value is not set.
	 */
	public String getHeader(PgnHeader header);

	/**
	 * @return The games id.
	 */
	public String getId();

	/**
	 * Returns the initial EP square from the starting position of this game.
	 */
	public int getInitialEpSquare();

	/**
	 * Returns the last move made, null if there was not one.
	 */
	public Move getLastMove();

	/**
	 * Returns a move list of all legal moves in the games current position.
	 */
	public PriorityMoveList getLegalMoves();

	/**
	 * Returns a move list of the moves that have been made in the position.
	 * 
	 */
	public MoveList getMoveList();

	/**
	 * Returns a bitboard with 1s on all of the squares that do not contain the
	 * color to moves pieces.
	 */
	public long getNotColorToMoveBB();

	/**
	 * Returns a bitboard with 1s on all squares that are occupied in the
	 * position.
	 */
	public long getOccupiedBB();

	/**
	 * Returns the piece at the specified square with its promotion mask
	 * removed.
	 */
	public int getPiece(int square);

	/**
	 * Returns a bitboard with 1s on all of the squares containing the piece.
	 * 
	 * @param piece
	 *            The un-colored piece constant.
	 */
	public long getPieceBB(int piece);

	/**
	 * Returns a bitboard with 1s on all squares containing the piece.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The un-colored piece constant.
	 */
	public long getPieceBB(int color, int piece);

	/**
	 * Returns the number of pieces on the board.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The un-colored piece constant.
	 */
	public int getPieceCount(int color, int piece);

	/**
	 * Returns an array indexed by piece type containing the piece jail counts
	 * for the specified color.
	 * 
	 * @param color
	 *            The color to get the piece jail form. WHITE will return a
	 *            piece jail representation for the BLACK pieces, i.e. captured
	 *            WHITE pieces.
	 * @return An array indexed by piece type containing the piece jail counts.
	 */
	public int[] getPieceJailCounts(int color);

	/**
	 * Returns the piece at the specified square with its promotion mask.
	 */
	public int getPieceWithPromoteMask(int square);

	/**
	 * Returns a move list containing all pseudo legal moves.
	 */
	public PriorityMoveList getPseudoLegalMoves();

	/**
	 * Returns the number of times this position has occured.
	 */
	public int getRepCount();

	/**
	 * Returns a hash that can be used to reference moveRepHash. The hash is
	 * created using the zobrist position hash.
	 * 
	 * @return The hash.
	 */
	public int getRepHash();

	/**
	 * Returns the games result constant.
	 */
	public Result getResult();

	/**
	 * Returns an integer with 1s in all of the states the game has set.
	 * 
	 * @return The games state
	 */
	public int getState();

	/**
	 * Returns the games variant. This is a convenience method so you don't have
	 * to grab it from the header.
	 */
	public Variant getVariant();

	/**
	 * Returns the games Zobrist game hash. The game hash includes state
	 * information such as color to move, castling, and ep info.
	 * 
	 * @return The Zobrist game hash.
	 */
	public long getZobristGameHash();

	/**
	 * Returns the games Zobrist position hash. The position hash is the Zobrist
	 * WITHOUT state info such as color to move, castling, ep info.
	 * 
	 * @return THe Zobrist position hash.
	 */
	public long getZobristPositionHash();

	/**
	 * Increments the move hash repetition count for the current position.
	 */
	public void incrementRepCount();

	/**
	 * Returns true if the current position is check-mate.
	 */
	public boolean isCheckmate();

	/**
	 * Returns true if the current position is check-mate given a list of moves.
	 * This method is provided so move generation does'nt have to be done twice
	 * if it was already performed and a moveList is available.
	 */
	public boolean isCheckmate(PriorityMoveList moveList);

	/**
	 * Returns true if the side to move is in check.
	 * 
	 * @return true if in check, otherwise false.
	 */
	public boolean isInCheck();

	/**
	 * Returns true if the specified color is in check in the specified
	 * position.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @return true if in check, otherwise false.
	 */
	public boolean isInCheck(int color);

	/**
	 * Returns true if the specified square would be in check if it contained a
	 * king.
	 * 
	 * @param color
	 *            WHITE or BLACK.
	 * @param pieceBB
	 *            A bitboard representing the square to to check.
	 * @return true if in check, false otherwise.
	 */
	public boolean isInCheck(int color, long pieceBB);

	/**
	 * Returns true if one of the state flags is in the specified state.
	 */
	public boolean isInState(int state);

	/**
	 * This is one of the methods that needs to be overridden in subclasses.
	 * 
	 * @return If the position is legal.
	 */
	public boolean isLegalPosition();

	/**
	 * Returns true if the eco game headers are updated on each move.
	 */
	public boolean isSettingEcoHeaders();

	/**
	 * Returns true if SAN, short algebraic notation, is being set on all mvoes
	 * generated by this class. This is an expensive operation and should be
	 * turned off for engines.
	 */
	public boolean isSettingMoveSan();

	/**
	 * Returns true if the current position is stalemate.
	 */
	public boolean isStalemate();

	/**
	 * Returns true if the current position is stalemate given a list of moves.
	 * This method is provided so move generation doesnt have to be done twice
	 * if it was already performed and a moveList is available.
	 */
	public boolean isStalemate(PriorityMoveList moveList);

	/**
	 * @return If it is currently white's move in this Game.
	 */
	public boolean isWhitesMove();

	/**
	 * Makes a drop move given the piece to drop and the destination square.
	 * 
	 * @param piece
	 *            THe piece to drop.
	 * @param destination
	 *            The destination square.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             thrown if the move is invalid.
	 */
	public Move makeDropMove(int piece, int destination);

	/**
	 * Makes the specified move in LAN, long algebraic notation.
	 * 
	 * @param lan
	 *            The move in LAN.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move was invalid.
	 */
	public Move makeLanMove(String lan) throws IllegalArgumentException;

	/**
	 * Makes a move using the start/end square.
	 * 
	 * @param startSquare
	 *            The start square.
	 * @param endSquare
	 *            The end square.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move is illegal.
	 */
	public Move makeMove(int startSquare, int endSquare)
			throws IllegalArgumentException;

	/**
	 * Makes a move using the start/end square and the specified promotion
	 * piece.
	 * 
	 * @param startSquare
	 *            The start square.
	 * @param endSquare
	 *            The end square.
	 * @param promotionPiece
	 *            The non colored piece constant representing the promoted
	 *            piece.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move is illegal.
	 */
	public Move makeMove(int startSquare, int endSquare, int promotePiece)
			throws IllegalArgumentException;

	/**
	 * Makes a move given SAN, short algebraic notation.
	 * 
	 * @param shortAlgebraic
	 *            The move in SAN.
	 * @return THe move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move was invalid.
	 */
	public Move makeSanMove(String shortAlgebraic)
			throws IllegalArgumentException;

	/**
	 * Makes a move. If the move is illegal false is returned.
	 * 
	 * @param move
	 *            The move to make.
	 * @return true if the move was legal, false otherwise.
	 */
	public boolean move(Move move);

	/**
	 * Copys the information from this game into the passed in game.
	 */
	public void overwrite(Game game, boolean ignoreHashes);

	/**
	 * Removes the specified pgn header from this game.
	 */
	public void removeHeader(PgnHeader headerName);

	/**
	 * Rolls back the last move made.
	 */
	public void rollback();

	/**
	 * Sets the board. The board is indexed by square and contains the uncolored
	 * piece at the square. The board also can contain promote masked pieces
	 * when pieces are promoted. This does NOT update any bitboards.
	 * 
	 * @param board
	 *            The position board.
	 */
	public void setBoard(int[] board);

	/**
	 * Sets the castling state for the specified color.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param castling
	 *            The new castling state constant.
	 */
	public void setCastling(int color, int castling);

	/**
	 * Sets the color bitboard for a specified color. This bitboard contains 1s
	 * on all of the squares that contain the specified colors pieces.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param bb
	 *            The new bitboard.
	 */
	public void setColorBB(int color, long bb);

	/**
	 * Sets the color to move. WHITE or BLACK.
	 */
	public void setColorToMove(int color);

	/**
	 * Sets the drop count of the specified piece. This is useful for droppable
	 * games like bughouse and crazyhouse.
	 */
	public void setDropCount(int color, int piece, int count);

	/**
	 * Sets the empty bitboard. This is the bitboard with 1s in all of the empty
	 * squares.
	 */
	public void setEmptyBB(long emptyBB);

	/**
	 * Sets the EP square. This is the move of the last double pawn push. Can be
	 * EMPTY.
	 */
	public void setEpSquare(int epSquare);

	/**
	 * Returns the current 50 move count. This is the count since the last
	 * non-reversible move. This count is used to determine the 50 move draw
	 * rule.
	 */
	public void setFiftyMoveCount(int fiftyMoveCount);

	/**
	 * Sets the number of half moves played.
	 * 
	 * @param halfMoveCount
	 */
	public void setHalfMoveCount(int halfMoveCount);

	/**
	 * Sets the specified pgn header to the specified value for this game.
	 */
	public void setHeader(PgnHeader header, String value);

	/**
	 * Sets the games id.
	 */
	public void setId(String id);

	/**
	 * Returns the initial ep square used to create this game. This is useful
	 * when rolling back the first move. The ep square is the square of the last
	 * double pawn push. Can be EMPTY.
	 */
	public void setInitialEpSquare(int initialEpSquare);

	/**
	 * Sets the not color to move bitboard to the specified bitboard. This is a
	 * bitmap containing the negation of the bitmap containing 1s where all of
	 * the color to moves pieces are location.
	 * 
	 * @param notColorToMoveBB
	 */
	public void setNotColorToMoveBB(long notColorToMoveBB);

	/**
	 * squares set to 1.
	 * 
	 * @param occupiedBB
	 *            The occupied bitboard.
	 */
	public void setOccupiedBB(long occupiedBB);

	/**
	 * Sets a piece at a specified square. This only updated the board object
	 * and does NOT update the bitmaps.
	 * 
	 * @param square
	 *            The square.
	 * @param piece
	 *            The un-colored piece constant.
	 */
	public void setPiece(int square, int piece);

	/**
	 * Sets the piece bitboard to the specified bitboard.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The un-colored piece constant.
	 * @param bb
	 *            The new bitboard.
	 */
	public void setPieceBB(int color, int piece, long bb);

	/**
	 * Sets the piece count for the specified piece. This method handles
	 * un-masking the piece so its ok to pass in promotion masked pieces.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The un-colored piece constant.
	 * @param count
	 *            The new piece count.
	 */
	public void setPieceCount(int color, int piece, int count);

	/**
	 * Returns the games Zobrist game hash. The game hash includes state
	 * information such as color to move, castling, and ep info.
	 * 
	 * @return The Zobrist game hash.
	 */
	public void setZobristGameHash(long hash);

	/**
	 * Returns the games Zobrist position hash. The position hash is the Zobrist
	 * WITHOUT state info such as color to move, castling, ep info.
	 * 
	 * @return THe Zobrist position hash.
	 */
	public void setZobristPositionHash(long hash);

	/**
	 * Returns the FEN, Forsyth Edwards notation, of the game.
	 * 
	 * <pre>
	 *  rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
	 * </pre>
	 * 
	 * @return The games position in FEN.
	 */
	public String toFen();

	/**
	 * Returns only the position part of the fen.
	 * 
	 * <pre>
	 * rnbqkbnr / pppppppp / 8 / 8 / 8 / 8 / PPPPPPPP / RNBQKBNR
	 * </pre>
	 * 
	 * @return The board with pieces part only of the FEN message.
	 */
	public String toFenPosition();

	/**
	 * Returns a string containing PGN (Portable Game Notation) for the
	 * specified game.
	 */
	public String toPgn();

}
