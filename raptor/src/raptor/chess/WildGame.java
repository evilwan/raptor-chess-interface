package raptor.chess;

import static raptor.chess.util.GameUtils.bitscanClear;
import static raptor.chess.util.GameUtils.bitscanForward;
import static raptor.chess.util.GameUtils.getBitboard;
import static raptor.chess.util.GameUtils.getFile;
import static raptor.chess.util.GameUtils.getSquare;
import raptor.chess.pgn.PgnHeader;

public class WildGame extends ClassicGame {

	private int whiteKingFile, whiteLongRookFile, whiteShortRookFile;
	
	private int blackKingFile, blackLongRookFile, blackShortRookFile;

	public WildGame() {
		setHeader(PgnHeader.Variant, Variant.wild.name());
	}
	
	public void initialPositionIsSet() {
		whiteKingFile = getFile(bitscanForward(getPieceBB(WHITE, KING)));
		long rookBB = getPieceBB(WHITE, ROOK);
		int firstRook = getFile(bitscanForward(rookBB));
		rookBB = bitscanClear(rookBB);
		int secondRook = getFile(bitscanForward(rookBB));
		if (firstRook < whiteKingFile) {
			whiteLongRookFile = whiteKingFile == 4 ? 
					firstRook : secondRook;
			whiteShortRookFile = whiteKingFile == 4 ? 
					secondRook : firstRook;
		} else {
			whiteLongRookFile = whiteKingFile == 4 ? 
					secondRook : firstRook;
			whiteShortRookFile = firstRook;
		}
		
		blackKingFile = getFile(bitscanForward(getPieceBB(BLACK, KING)));
		rookBB = getPieceBB(BLACK, ROOK);
		firstRook = getFile(bitscanForward(rookBB));
		rookBB = bitscanClear(rookBB);
		secondRook = getFile(bitscanForward(rookBB));
		if (firstRook < blackKingFile) {
			blackLongRookFile = blackKingFile == 4 ? 
					firstRook : secondRook;
			blackShortRookFile = blackKingFile == 4 ? 
					secondRook : firstRook;
		} else {
			blackLongRookFile = blackKingFile == 4 ? 
					secondRook : firstRook;
			blackShortRookFile = firstRook;
		}
	}
	
	protected void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList moves) {
		
		int kingSquare = getColorToMove() == WHITE ? getSquare(0,
				whiteKingFile) : getSquare(7, blackKingFile);
		long kingSquareBB = getBitboard(kingSquare);
		
		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == kingSquareBB ) {
			
			if (whiteKingFile == 4
					&& FischerRandomUtils.emptyBetweenFiles(this, 0, whiteKingFile,
					whiteShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_F1, WHITE,
							whiteShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_G1, WHITE,
							whiteShortRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this, kingSquare,
							SQUARE_G1, WHITE)) {				
				moves.appendLowPriority(new Move(kingSquare, SQUARE_G1, KING,
						WHITE, EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
				
			}
			else if (FischerRandomUtils.emptyBetweenFiles(this, 0, whiteShortRookFile,
					whiteKingFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_C1, WHITE,
							whiteShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_B1, WHITE,
							whiteShortRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this, kingSquare,
							SQUARE_C1, WHITE)) {								
				moves.appendLowPriority(new Move(kingSquare, SQUARE_B1, KING,
						WHITE, EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
			}
			
		}
		
		if (getColorToMove() == WHITE
				&& (getCastling(getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == kingSquareBB ) {
			
			if (whiteKingFile == 4
					&& FischerRandomUtils.emptyBetweenFiles(this, 0, whiteLongRookFile,
					whiteKingFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_D1, WHITE,
							whiteLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_C1, WHITE,
							whiteLongRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this, kingSquare,
							SQUARE_C1, WHITE)) {
				moves.appendLowPriority(new Move(kingSquare, SQUARE_C1, KING,
						WHITE, EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
				
			}
			else if (FischerRandomUtils.emptyBetweenFiles(this, 0, whiteKingFile,
					whiteLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_E1, WHITE,
							whiteLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_F1, WHITE,
							whiteLongRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this, kingSquare,
							SQUARE_F1, WHITE)) {
				moves.appendLowPriority(new Move(kingSquare, SQUARE_F1, KING,
						WHITE, EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
			}
		}	
		
		if (getColorToMove() == BLACK
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == kingSquareBB) {
			
			if (blackKingFile == 4
					&& FischerRandomUtils.emptyBetweenFiles(this, 7,
							blackKingFile, blackShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_F8,
							BLACK, blackShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_G8,
							BLACK, blackShortRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this,
							kingSquare, SQUARE_G8, BLACK)) {
				moves.appendLowPriority(new Move(kingSquare, SQUARE_G8, KING,
						BLACK, EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
				
			}
			else if (FischerRandomUtils.emptyBetweenFiles(this, 7,
							blackShortRookFile, blackKingFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_C8,
							BLACK, blackShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_B8,
							BLACK, blackShortRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this,
							kingSquare, SQUARE_B8, BLACK)) {								
				moves.appendLowPriority(new Move(kingSquare, SQUARE_B8, KING,
						BLACK, EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
			}
		}
		if (getColorToMove() == BLACK
				&& (getCastling(getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == kingSquareBB) {
			
			if (blackKingFile == 4
					&& FischerRandomUtils.emptyBetweenFiles(this, 7,
							blackLongRookFile, blackKingFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_D8,
							BLACK, blackLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_C8,
							BLACK, blackLongRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this,
							kingSquare, SQUARE_C8, BLACK)) {
				moves.appendLowPriority(new Move(kingSquare, SQUARE_C8, KING,
						BLACK, EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
			}
			else if (FischerRandomUtils.emptyBetweenFiles(this, 7,
					        blackKingFile, blackLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_E8,
							BLACK, blackLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, SQUARE_F8,
							BLACK, blackLongRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this,
							kingSquare, SQUARE_F8, BLACK)) {
				moves.appendLowPriority(new Move(kingSquare, SQUARE_F8, KING,
						BLACK, EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
			}
		}
		
	}
	
	public void makeCastlingMove(Move move) {
		int kingFromSquare = move.getColor() == WHITE ? getSquare(0,
				whiteKingFile) : getSquare(7, blackKingFile);
		long kingFromBB = getBitboard(kingFromSquare);
		long kingToBB = 0, rookFromBB = 0, rookToBB = 0;
		int rookFromSquare = 0;

		if (move.getColor() == WHITE) {
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = getSquare(0, whiteShortRookFile);
				rookFromBB = getBitboard(rookFromSquare);
				if (whiteKingFile == 4) {
					kingToBB = G1;
					rookToBB = F1;
					FischerRandomUtils.updateZobristCastle(this, WHITE, kingFromSquare,
							rookFromSquare, SQUARE_G1, SQUARE_F1);
				}
				else {
					kingToBB = B1;
					rookToBB = C1;
					FischerRandomUtils.updateZobristCastle(this, WHITE, kingFromSquare,
							rookFromSquare, SQUARE_B1, SQUARE_C1);
				}
				
			}
			else {
				rookFromSquare = getSquare(0, whiteLongRookFile);				
				rookFromBB = getBitboard(rookFromSquare);
				if (whiteKingFile == 4) {
					kingToBB = C1;
					rookToBB = D1;
					FischerRandomUtils.updateZobristCastle(this, WHITE, kingFromSquare,
							rookFromSquare, SQUARE_C1, SQUARE_D1);
				}
				else {
					kingToBB = F1;
					rookToBB = E1;
					FischerRandomUtils.updateZobristCastle(this, WHITE, kingFromSquare,
							rookFromSquare, SQUARE_F1, SQUARE_E1);
				}
			}
		}
		else if (move.getColor() == BLACK) {
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = getSquare(7, blackShortRookFile);
				rookFromBB = getBitboard(rookFromSquare);
				if (blackKingFile == 4) {
					kingToBB = G8;
					rookToBB = F8;
					FischerRandomUtils.updateZobristCastle(this, BLACK, kingFromSquare,
							rookFromSquare, SQUARE_G8, SQUARE_F8);
				}
				else {
					kingToBB = B8;
					rookToBB = C8;
					FischerRandomUtils.updateZobristCastle(this, BLACK, kingFromSquare,
							rookFromSquare, SQUARE_B8, SQUARE_C8);
				}
				
			}
			else {
				rookFromSquare = getSquare(7, blackLongRookFile);				
				rookFromBB = getBitboard(rookFromSquare);
				if (blackKingFile == 4) {
					kingToBB = C8;
					rookToBB = D8;
					FischerRandomUtils.updateZobristCastle(this, BLACK, kingFromSquare,
							rookFromSquare, SQUARE_C8, SQUARE_D8);
				}
				else {
					kingToBB = F8;
					rookToBB = E8;
					FischerRandomUtils.updateZobristCastle(this, BLACK, kingFromSquare,
							rookFromSquare, SQUARE_F8, SQUARE_E8);
				}
			}
		}
		
		if (kingToBB != kingFromBB) {
			long kingFromTo = kingToBB | kingFromBB;
			setPiece(bitscanForward(kingToBB), KING);
			setPiece(kingFromSquare, EMPTY);
			xor(move.getColor(), KING, kingFromTo);
			xor(move.getColor(), kingFromTo);
			setOccupiedBB(getOccupiedBB() ^ kingFromTo);
			setEmptyBB(getEmptyBB() ^ kingFromTo);
		}

		if (rookFromBB != rookToBB) {
			long rookFromTo = rookToBB | rookFromBB;
			setPiece(bitscanForward(rookToBB), ROOK);
			if (rookFromBB != kingToBB) {
				setPiece(rookFromSquare, EMPTY);
			}
			xor(move.getColor(), ROOK, rookFromTo);

			xor(move.getColor(), rookFromTo);
			setOccupiedBB(getOccupiedBB() ^ rookFromTo);
			setEmptyBB(getEmptyBB() ^ rookFromTo);
		}

		setCastling(getColorToMove(), CASTLE_NONE);
		setEpSquare(EMPTY_SQUARE);
	}
	
	protected void rollbackCastlingMove(Move move) {
		int kingFromSquare = move.getColor() == WHITE ? getSquare(0,
				whiteKingFile) : getSquare(7, blackKingFile);
		long kingFromBB = getBitboard(kingFromSquare);
		long kingToBB = 0, rookFromBB = 0, rookToBB = 0;
		int rookFromSquare = 0;

		if (move.getColor() == WHITE) {
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = getSquare(0, whiteShortRookFile);
				rookFromBB = getBitboard(rookFromSquare);
				if (whiteKingFile == 4) {
					kingToBB = G1;
					rookToBB = F1;
					FischerRandomUtils.updateZobristCastle(this, WHITE, kingFromSquare,
							rookFromSquare, SQUARE_G1, SQUARE_F1);
				}
				else {
					kingToBB = B1;
					rookToBB = C1;
					FischerRandomUtils.updateZobristCastle(this, WHITE, kingFromSquare,
							rookFromSquare, SQUARE_B1, SQUARE_C1);
				}
				
			}
			else {
				rookFromSquare = getSquare(0, whiteLongRookFile);				
				rookFromBB = getBitboard(rookFromSquare);
				if (whiteKingFile == 4) {
					kingToBB = C1;
					rookToBB = D1;
					FischerRandomUtils.updateZobristCastle(this, WHITE, kingFromSquare,
							rookFromSquare, SQUARE_C1, SQUARE_D1);
				}
				else {
					kingToBB = F1;
					rookToBB = E1;
					FischerRandomUtils.updateZobristCastle(this, WHITE, kingFromSquare,
							rookFromSquare, SQUARE_F1, SQUARE_E1);
				}
			}
		}
		else if (move.getColor() == BLACK) {
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = getSquare(7, blackShortRookFile);
				rookFromBB = getBitboard(rookFromSquare);
				if (blackKingFile == 4) {
					kingToBB = G8;
					rookToBB = F8;
					FischerRandomUtils.updateZobristCastle(this, BLACK, kingFromSquare,
							rookFromSquare, SQUARE_G8, SQUARE_F8);
				}
				else {
					kingToBB = B8;
					rookToBB = C8;
					FischerRandomUtils.updateZobristCastle(this, BLACK, kingFromSquare,
							rookFromSquare, SQUARE_B8, SQUARE_C8);
				}
				
			}
			else {
				rookFromSquare = getSquare(7, blackLongRookFile);				
				rookFromBB = getBitboard(rookFromSquare);
				if (blackKingFile == 4) {
					kingToBB = C8;
					rookToBB = D8;
					FischerRandomUtils.updateZobristCastle(this, BLACK, kingFromSquare,
							rookFromSquare, SQUARE_C8, SQUARE_D8);
				}
				else {
					kingToBB = F8;
					rookToBB = E8;
					FischerRandomUtils.updateZobristCastle(this, BLACK, kingFromSquare,
							rookFromSquare, SQUARE_F8, SQUARE_E8);
				}
			}
		}
		
		if (rookFromBB != rookToBB) {
			long rookFromTo = rookToBB | rookFromBB;
			setPiece(bitscanForward(rookToBB), EMPTY);
			setPiece(rookFromSquare, ROOK);
			xor(move.getColor(), ROOK, rookFromTo);
			xor(move.getColor(), rookFromTo);
			setOccupiedBB(getOccupiedBB() ^ rookFromTo);
			setEmptyBB(getEmptyBB() ^ rookFromTo);
		}

		if (kingToBB != kingFromBB) {
			long kingFromTo = kingToBB | kingFromBB;
			if (kingToBB != rookFromBB) {
				setPiece(bitscanForward(kingToBB), EMPTY);
			}
			setPiece(kingFromSquare, KING);
			xor(move.getColor(), KING, kingFromTo);
			xor(move.getColor(), kingFromTo);
			setOccupiedBB(getOccupiedBB() ^ kingFromTo);
			setEmptyBB(getEmptyBB() ^ kingFromTo);
		}

		setEpSquareFromPreviousMove();
	}
	
	protected void updateCastlingRightsForNonEpNonCastlingMove(Move move) {
		switch (move.getPiece()) {
		case KING:
			setCastling(getColorToMove(), CASTLE_NONE);
			break;
		default:
			if (move.getPiece() == ROOK && move.getFrom() == SQUARE_A1
					&& getColorToMove() == WHITE || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_A1 && getColorToMove() == BLACK) {				
				setCastling(WHITE, getCastling(WHITE) & 						
						(whiteKingFile == 4 ? CASTLE_SHORT : CASTLE_LONG));
			} else if (move.getPiece() == ROOK && move.getFrom() == SQUARE_H1
					&& getColorToMove() == WHITE || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_H1 && getColorToMove() == BLACK) {
				setCastling(WHITE, getCastling(WHITE) & 
						(whiteKingFile == 4 ? CASTLE_LONG : CASTLE_SHORT));
			} else if (move.getPiece() == ROOK && move.getFrom() == SQUARE_A8
					&& getColorToMove() == BLACK || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_A8 && getColorToMove() == WHITE) {
				setCastling(BLACK, getCastling(BLACK) & 
						(blackKingFile == 4 ? CASTLE_SHORT : CASTLE_LONG));
			} else if (move.getPiece() == ROOK && move.getFrom() == SQUARE_H8
					&& getColorToMove() == BLACK || move.getCapture() == ROOK
					&& move.getTo() == SQUARE_H8 && getColorToMove() == WHITE) {
				setCastling(BLACK, getCastling(BLACK) & 
						(blackKingFile == 4 ? CASTLE_LONG : CASTLE_SHORT));
			}
			break;
		}
	}

}
