/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.chess.pgn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Move;
import raptor.chess.Result;
import raptor.chess.Variant;

/**
 * A Lenient PGN Parser Listener which creates Games from the PGN being handled.
 * 
 * Currently sub-lines are disabled.
 * 
 */
public abstract class LenientPgnParserListener implements PgnParserListener {
	private static final Log LOG = LogFactory
			.getLog(LenientPgnParserListener.class);

	protected static class NagWordTrimResult {
		String move;

		Nag nag;
	}

	protected SublineNode currentAnalysisLine;

	protected Game currentGame = null;

	protected Map<String, String> currentHeaders = new HashMap<String, String>();

	protected Move currentMoveInfo;

	protected List<MoveAnnotation> danglingAnnotations = new ArrayList<MoveAnnotation>(
			5);

	protected boolean isIgnoringCurrentGame;

	protected boolean isIgnoringSubline;

	protected boolean isParsingGameHeaders;

	protected boolean isParsingGameMoves;

	protected boolean isParsingMove;

	protected boolean isParsingSubline;

	protected boolean isSearchingForGameStart = true;

	protected int nestedSublineCount = 0;

	public LenientPgnParserListener() {
	}

	public Game createGameFromDescription() {
		String fen = null;
		Variant variant = null;
		Game result = null;

		if (currentHeaders.get(PgnHeader.FEN.name()) != null) {
			fen = currentHeaders.get(PgnHeader.FEN.name());
		}

		// Check for the Variant header.
		if (currentHeaders.get(PgnHeader.Variant.name()) != null) {
			try {
				variant = Variant.valueOf(currentHeaders.get(PgnHeader.Variant
						.name()));
			} catch (IllegalArgumentException iae) {
			}
		}

		// Couldn't find it now check for keywords in event.
		if (variant == null
				&& currentHeaders.get(PgnHeader.Event.name()) != null) {
			if (StringUtils.containsIgnoreCase(currentHeaders
					.get(PgnHeader.Event.name()), "crazyhouse")) {
				variant = Variant.crazyhouse;
			} else if (StringUtils.containsIgnoreCase(currentHeaders
					.get(PgnHeader.Event.name()), "atomic")) {
				variant = Variant.atomic;
			} else if (StringUtils.containsIgnoreCase(currentHeaders
					.get(PgnHeader.Event.name()), "suicide")) {
				variant = Variant.suicide;
			} else if (StringUtils.containsIgnoreCase(currentHeaders
					.get(PgnHeader.Event.name()), "losers")) {
				variant = Variant.losers;
			} else if (StringUtils.containsIgnoreCase(currentHeaders
					.get(PgnHeader.Event.name()), "wild fr")) {
				variant = Variant.fischerRandom;
			} else if (StringUtils.containsIgnoreCase(currentHeaders
					.get(PgnHeader.Event.name()), "bughouse")) {
				variant = Variant.bughouse;
			} else if (StringUtils.containsIgnoreCase(currentHeaders
					.get(PgnHeader.Event.name()), "wild")) {
				variant = Variant.wild;
			}
		}

		// No variant detected, set it to classic.
		if (variant == null) {
			variant = Variant.classic;
		}

		if (fen != null) {
			result = GameFactory.createFromFen(fen, variant);
			result.setHeader(PgnHeader.FEN, fen);
		} else {
			result = GameFactory.createStartingPosition(variant);
		}

		if (variant != Variant.classic) {
			result.setHeader(PgnHeader.Variant, variant.toString());
		}
		return result;
	}

	public abstract void errorEncountered(PgnParserError error);

	public abstract void gameParsed(Game game, int lineNumber);

	public void onAnnotation(PgnParser parser, String annotation) {
		if (!isIgnoringCurrentGame) {
			if (isParsingGameMoves) {
				if (isParsingSubline && !isIgnoringSubline) {
					if (currentAnalysisLine == null
							|| currentAnalysisLine.getMove() == null) {
						// This can happen in chess base for now just don't
						// support it its a pain in the ass.
						MoveAnnotation[] annotations = pgnAnnotationToMoveAnnotations(annotation);
						for (MoveAnnotation moveAnnotation : annotations) {
							danglingAnnotations.add(moveAnnotation);
						}
					} else {
						MoveAnnotation[] annotations = pgnAnnotationToMoveAnnotations(annotation);
						for (MoveAnnotation moveAnnotation : annotations) {
							currentAnalysisLine.getMove().addAnnotation(
									moveAnnotation);
						}
					}
				} else if (isParsingMove && !isIgnoringSubline) {
					if (currentMoveInfo == null) {
						MoveAnnotation[] annotations = pgnAnnotationToMoveAnnotations(annotation);
						for (MoveAnnotation moveAnnotation : annotations) {
							danglingAnnotations.add(moveAnnotation);
						}
					} else {
						MoveAnnotation[] annotations = pgnAnnotationToMoveAnnotations(annotation);
						for (MoveAnnotation moveAnnotation : annotations) {
							currentMoveInfo.addAnnotation(moveAnnotation);
						}
					}
				}
			} else if (!isIgnoringSubline) {
				MoveAnnotation[] annotations = pgnAnnotationToMoveAnnotations(annotation);
				for (MoveAnnotation moveAnnotation : annotations) {
					danglingAnnotations.add(moveAnnotation);
				}
			}
		}
	}

	public void onGameEnd(PgnParser parser, Result result) {
		if (!isIgnoringCurrentGame) {
			if (isParsingGameMoves) {
				// Check for dangling sublines.
				if (!isIgnoringSubline && currentAnalysisLine != null) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.DANGLING_SUBLINE,
							PgnParserError.Action.IGNORING, parser
									.getLineNumber()));
					currentAnalysisLine = null;
				}

				// add the game
				currentGame
						.setHeader(PgnHeader.Result, result.getDescription());
				currentGame.addState(Game.INACTIVE_STATE);
				gameParsed(currentGame, parser.getLineNumber());
			} else {
				errorEncountered(new PgnParserError(
						PgnParserError.Type.UNEXPECTED_GAME_END,
						PgnParserError.Action.IGNORING_CURRENT_GAME, parser
								.getLineNumber()));
			}
		}

		setStateToSearchingForNewGame();
	}

	public void onGameStart(PgnParser parser) {
		if (!isSearchingForGameStart) {
			errorEncountered(new PgnParserError(
					PgnParserError.Type.UNEXPECTED_GAME_START,
					PgnParserError.Action.NONE, parser.getLineNumber()));
		}
		setStateToSearchingForNewGame();
		isParsingGameHeaders = true;
	}

	public void onHeader(PgnParser parser, String headerName, String headerValue) {
		if (!isIgnoringCurrentGame) {
			if (isParsingGameHeaders) {
				currentHeaders.put(headerName, headerValue);
			} else {
				errorEncountered(new PgnParserError(
						PgnParserError.Type.UNEXPECTED_HEADER,
						PgnParserError.Action.IGNORING, parser.getLineNumber()));
			}
		}
	}

	public void onMoveNag(PgnParser parser, Nag nag) {
		if (!isIgnoringCurrentGame && isParsingMove) {
			if (!isParsingSubline) {
				if (currentMoveInfo == null) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.DANGLING_NAG,
							PgnParserError.Action.IGNORING, parser
									.getLineNumber(), new String[] { nag
									.getNagString() }));
				} else {
					currentMoveInfo.addAnnotation(nag);
				}
			} else if (!isIgnoringSubline) {
				if (currentAnalysisLine == null
						|| currentAnalysisLine.getMove() == null) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.DANGLING_NAG,
							PgnParserError.Action.IGNORING, parser
									.getLineNumber(), new String[] { nag
									.getNagString() }));
				} else {
					currentAnalysisLine.getMove().addAnnotation(nag);
				}
			}
		}
	}

	public void onMoveNumber(PgnParser parser, int moveNumber) {
		if (!isIgnoringCurrentGame) {
			if (isParsingGameHeaders) {
				createGameFromHeaders(parser);
				isParsingGameHeaders = false;
				isParsingGameMoves = true;
				isParsingMove = true;
			} else if (isParsingGameMoves && !isParsingSubline) {
				if (!isIgnoringSubline && currentAnalysisLine != null) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.DANGLING_SUBLINE,
							PgnParserError.Action.IGNORING, parser
									.getLineNumber()));
					currentAnalysisLine = null;
				}
			} else if (isParsingGameMoves && isParsingSubline) {
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void onMoveSublineEnd(PgnParser parser) {
		if (isParsingGameMoves) {
			nestedSublineCount--;
			if (currentAnalysisLine == null
					|| currentAnalysisLine.getMove() == null) {
				if (!isIgnoringSubline) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.UNEXPECTED_SUBLINE_END,
							PgnParserError.Action.IGNORING_CURRENT_SUBLINE,
							parser.getLineNumber()));
				}

				if (nestedSublineCount > 0) {
					isIgnoringSubline = true;
				} else {
					currentAnalysisLine = null;
					isParsingSubline = false;
					isIgnoringSubline = false;
				}
			} else if (nestedSublineCount < 0) {
				if (!isIgnoringSubline) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.UNEXPECTED_SUBLINE_END,
							PgnParserError.Action.IGNORING_CURRENT_SUBLINE,
							parser.getLineNumber()));
				}
				currentAnalysisLine = null;
				isParsingSubline = false;
				isIgnoringSubline = false;
			} else if (nestedSublineCount > 0) {
				if (currentAnalysisLine == null) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.INVALID_SUBLINE_STATE,
							PgnParserError.Action.IGNORING_CURRENT_SUBLINE,
							parser.getLineNumber()));
					isIgnoringSubline = true;
				} else {
					// We need to find the sub-line owner. This happens to be
					// the greatest parents sub-line owner.
					SublineNode greatestParent = currentAnalysisLine
							.getGreatestParent();
					if (greatestParent.hasSublineOwner()) {
						currentAnalysisLine = greatestParent.getSublineOwner();
					} else {
						errorEncountered(new PgnParserError(
								PgnParserError.Type.INVALID_SUBLINE_STATE,
								PgnParserError.Action.IGNORING_CURRENT_SUBLINE,
								parser.getLineNumber()));
						isIgnoringSubline = true;
					}
				}
				isParsingSubline = true;
			} else if (currentAnalysisLine.isChild() && nestedSublineCount == 0) {
				if (!isIgnoringSubline) {
					// Traverse back to the parent and add as a subline.
					while (currentAnalysisLine.isChild()) {
						currentAnalysisLine = currentAnalysisLine.getParent();
					}
					currentMoveInfo.addAnnotation(currentAnalysisLine);
				}

				currentAnalysisLine = null;
				isParsingSubline = false;
				isIgnoringSubline = false;

			} else // we are finished with all sub-lines.
			{
				// add the sub-line as an annotation to the current move if its
				// there.
				if (currentMoveInfo != null) {
					if (currentAnalysisLine != null && !isIgnoringSubline) {
						currentMoveInfo.addAnnotation(currentAnalysisLine);
					} else if (!isIgnoringSubline) {
						errorEncountered(new PgnParserError(
								PgnParserError.Type.INVALID_SUBLINE_STATE,
								PgnParserError.Action.IGNORING, parser
										.getLineNumber()));
					}
				} else if (!isIgnoringSubline) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.INVALID_SUBLINE_STATE,
							PgnParserError.Action.IGNORING, parser
									.getLineNumber()));
				}
				currentAnalysisLine = null;
				isParsingSubline = false;
				isIgnoringSubline = false;
			}
		} else {
			errorEncountered(new PgnParserError(
					PgnParserError.Type.UNEXPECTED_SUBLINE_END,
					PgnParserError.Action.IGNORING, parser.getLineNumber()));
		}
	}

	@SuppressWarnings("deprecation")
	public void onMoveSublineStart(PgnParser parser) {
		if (isParsingGameMoves) {
			if (nestedSublineCount == 0) {
				if (currentAnalysisLine != null && !isIgnoringSubline) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.DANGLING_SUBLINE,
							PgnParserError.Action.IGNORING, parser
									.getLineNumber()));
				}
				currentAnalysisLine = new SublineNode();
				isParsingSubline = true;
			} else if (!isParsingSubline) {
				if (!isIgnoringSubline) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.DANGLING_SUBLINE,
							PgnParserError.Action.IGNORING, parser
									.getLineNumber()));
					isIgnoringSubline = true;
				}
				isParsingSubline = true;
			} else {
				// we need to create a sub-line in the current analysis node and
				// set
				// the currentAnalysisLine to it.
				if (currentAnalysisLine == null && !isIgnoringSubline) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.DANGLING_SUBLINE,
							PgnParserError.Action.IGNORING, parser
									.getLineNumber()));
					isIgnoringSubline = true;
				} else if (!isIgnoringSubline) {
					if (currentAnalysisLine.getMove() == null) {
						errorEncountered(new PgnParserError(
								PgnParserError.Type.INVALID_SUBLINE_STATE,
								PgnParserError.Action.IGNORING, parser
										.getLineNumber()));
						isIgnoringSubline = true;
					} else {
						currentAnalysisLine = currentAnalysisLine
								.createSubline(null);
					}
				} else {
					currentAnalysisLine = new SublineNode();
				}
				isIgnoringSubline = false;
			}
			nestedSublineCount++;
		} else {
			errorEncountered(new PgnParserError(
					PgnParserError.Type.UNEXPECTED_SUBLINE_START,
					PgnParserError.Action.IGNORING, parser.getLineNumber()));
		}
	}

	public void onMoveWord(PgnParser parser, String word) {
		if (!isIgnoringCurrentGame) {
			if (!isParsingGameMoves) {
				errorEncountered(new PgnParserError(
						PgnParserError.Type.UNEXPECTED_MOVE_WORD,
						PgnParserError.Action.IGNORING, parser.getLineNumber(),
						new String[] { word }));
			} else if (!isParsingSubline) {

				if (!isIgnoringSubline && currentAnalysisLine != null) {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.DANGLING_SUBLINE,
							PgnParserError.Action.IGNORING, parser
									.getLineNumber()));
					currentAnalysisLine = null;
				}

				// First complete last move if we are in the middle of one.
				try {
					currentMoveInfo = makeGameMoveFromWord(word);
				} catch (IllegalArgumentException ime) {
					LOG.error("Invalid move encountered", ime);
					errorEncountered(new PgnParserError(
							PgnParserError.Type.ILLEGAL_MOVE_ENCOUNTERED,
							PgnParserError.Action.IGNORING_CURRENT_GAME, parser
									.getLineNumber(), new String[] { word }));
					isIgnoringCurrentGame = true;
				}
			} else if (!isIgnoringSubline) {
				if (currentAnalysisLine != null) {
					if (currentAnalysisLine.getMove() == null) {

						// SUBLINES are currently disabled.
						// THis needs to be fixed.
						// Disabled when converted from an old project of mine
						// into raptor.

						// first analysis line encountered. When this is the
						// case getMove is not populated.

						// ActivePosition currentPosition = null;
						//
						// if (!currentAnalysisLine.hasSublineOwner()) {
						// currentPosition = getPreviousPosition(currentGame);
						// } else {
						// // Find the first sub-line owner with a parent.
						// // If you run into a root node without a subline
						// // owner then use the games previous position
						// SublineNode toUse = currentAnalysisLine
						// .getSublineOwner();
						//
						// while (toUse != null && !toUse.isChild()) {
						// if (toUse.hasSublineOwner()) {
						// toUse = toUse.getSublineOwner();
						// } else {
						// toUse = null;
						// }
						// }
						//
						// if (toUse == null) {
						// currentPosition = getPreviousPosition(currentGame);
						// } else {
						// ChessGameMove moveInfo = toUse.getParent()
						// .getMove();
						// if (moveInfo != null) {
						// currentPosition = moveInfo.getPosition();
						// } else {
						// errorEncountered(new PgnParserError(
						// PgnParserError.Type.INVALID_SUBLINE_STATE,
						// PgnParserError.Action.IGNORING,
						// parser.getLineNumber()));
						// isIgnoringSubline = true;
						// }
						// }
						// }
						// try {
						// makeSublineMoveFromWord(word, currentPosition,
						// false);
						// /*
						// * ActivePosition nextPosition = currentPosition
						// * .makeMove(word); currentAnalysisLine.setMove(new
						// * ChessGameMove( nextPosition,
						// * nextPosition.getLastMove(),
						// * currentGame.getNumHalfMoves() + 1));
						// *
						// * for (MoveAnnotation annotation :
						// * danglingAnnotations) {
						// * currentAnalysisLine.getMove().addAnnotation(
						// * annotation); } danglingAnnotations.clear();
						// */
						// } catch (IllegalArgumentException ime) {
						// LOG.error("Invalid subline encountered",
						// ime);
						// errorEncountered(new PgnParserError(
						// PgnParserError.Type.ILLEGAL_MOVE_ENCOUNTERED,
						// PgnParserError.Action.IGNORING_CURRENT_SUBLINE,
						// parser.getLineNumber(),
						// new String[] { word }));
						// isIgnoringSubline = true;
						// }
						// } else {
						// // A non first move in an analysis line was detected,
						// so
						// // just add this move as a reply.
						// try {
						// makeSublineMoveFromWord(word, currentAnalysisLine
						// .getMove().getPosition(), true);
						// /*
						// * ActivePosition nextPosition = currentAnalysisLine
						// * .getMove().getPosition().makeMove(word);
						// * currentAnalysisLine = currentAnalysisLine
						// * .createReply(new ChessGameMove( nextPosition,
						// * nextPosition .getLastMove(),
						// * currentAnalysisLine.getMove() .getHalfMoveIndex()
						// * + 1)); for (MoveAnnotation annotation :
						// * danglingAnnotations) {
						// * currentAnalysisLine.getMove().addAnnotation(
						// * annotation); } danglingAnnotations.clear();
						// */
						// } catch (IllegalArgumentException ime) {
						// LOG.error("Invalid subline encountered",
						// ime);
						// errorEncountered(new PgnParserError(
						// PgnParserError.Type.ILLEGAL_MOVE_ENCOUNTERED,
						// PgnParserError.Action.IGNORING_CURRENT_SUBLINE,
						// parser.getLineNumber(),
						// new String[] { word }));
						// isIgnoringSubline = true;
						// }
					}
				} else {
					errorEncountered(new PgnParserError(
							PgnParserError.Type.INVALID_SUBLINE_STATE,
							PgnParserError.Action.IGNORING_CURRENT_SUBLINE,
							parser.getLineNumber()));
				}
			}
		}
	}

	public void onUnknown(PgnParser parser, String unknown) {
		if (!isIgnoringCurrentGame) {
			errorEncountered(new PgnParserError(
					PgnParserError.Type.UNKNOWN_TEXT_ENCOUNTERED,
					PgnParserError.Action.IGNORING, parser.getLineNumber(),
					new String[] { unknown }));
		}
	}

	// SUBLINES are currently disabled.
	// THis needs to be fixed.
	// Disabled when converted from an old project of mine
	// into Raptor.
	// protected ActivePosition makeSublineMoveFromWord(String word,
	// ActivePosition fromPosition, boolean isReply)
	// throws IllegalMoveException {
	// NagWordTrimResult trim = trimOutNag(word);
	//
	// ActivePosition nextPosition = fromPosition.makeMove(trim.move);
	//
	// if (isReply) {
	// currentAnalysisLine = currentAnalysisLine
	// .createReply(new ChessGameMove(nextPosition, nextPosition
	// .getLastMove(), currentAnalysisLine.getMove()
	// .getHalfMoveIndex() + 1));
	// } else {
	// currentAnalysisLine.setMove(new ChessGameMove(nextPosition,
	// nextPosition.getLastMove(),
	// currentGame.getNumHalfMoves() + 1));
	// }
	// if (trim.nag != null) {
	// currentAnalysisLine.getMove().addAnnotation(trim.nag);
	// }
	// for (MoveAnnotation annotation : danglingAnnotations) {
	// currentAnalysisLine.getMove().addAnnotation(annotation);
	// }
	// danglingAnnotations.clear();
	// return nextPosition;
	// }

	protected void createGameFromHeaders(PgnParser parser) {
		try {
			currentGame = createGameFromDescription();
			currentGame.addState(Game.UPDATING_SAN_STATE);
			currentGame.addState(Game.UPDATING_ECO_HEADERS_STATE);

			// Set all of the headers.
			for (String header : currentHeaders.keySet()) {

				try {
					currentGame.setHeader(PgnHeader.valueOf(header),
							currentHeaders.get(header));
				} catch (IllegalArgumentException iae) {
					// errorEncountered(new PgnParserError(
					// PgnParserError.Type.UNSUPPORTED_PGN_HEADER,
					// PgnParserError.Action.IGNORIONG_HEADER, parser
					// .getLineNumber(), header, currentHeaders
					// .get(header)));
				}
			}
		} catch (IllegalArgumentException ife) {
			LOG.warn("error setting up game", ife);
			errorEncountered(new PgnParserError(
					PgnParserError.Type.UNABLE_TO_PARSE_INITIAL_FEN,
					PgnParserError.Action.IGNORING_CURRENT_GAME, parser
							.getLineNumber()));
			isIgnoringCurrentGame = true;
		}

	}

	protected Move makeGameMoveFromWord(String word)
			throws IllegalArgumentException {
		NagWordTrimResult trim = trimOutNag(word);

		Move result = currentGame.makeSanMove(trim.move);

		if (trim.nag != null) {
			result.addAnnotation(trim.nag);
		}

		for (MoveAnnotation annotation : danglingAnnotations) {
			result.addAnnotation(annotation);
		}
		danglingAnnotations.clear();

		return result;
	}

	protected MoveAnnotation[] pgnAnnotationToMoveAnnotations(
			String pgnAnnotation) {
		List<MoveAnnotation> annotations = new ArrayList<MoveAnnotation>(3);

		if (pgnAnnotation.startsWith("[%emt")) {
			annotations.add(new TimeTakenForMove(pgnAnnotation));
		} else {
			annotations.add(new Comment(pgnAnnotation));
		}

		return annotations.toArray(new MoveAnnotation[0]);
	}

	protected void setStateToSearchingForNewGame() {
		isParsingGameHeaders = true;
		isParsingGameMoves = false;
		isParsingMove = false;
		isParsingSubline = false;
		isIgnoringCurrentGame = false;
		isIgnoringSubline = false;
		isParsingSubline = false;
		nestedSublineCount = 0;
		currentMoveInfo = null;
		currentAnalysisLine = null;
		currentHeaders.clear();
		isSearchingForGameStart = true;
	}

	private NagWordTrimResult trimOutNag(String word) {
		NagWordTrimResult result = new NagWordTrimResult();

		if (word.endsWith("!!")) {
			result.nag = Nag.NAG_3;
			result.move = word.substring(0, word.length() - 2);
		} else if (word.endsWith("!?")) {
			result.nag = Nag.NAG_4;
			result.move = word.substring(0, word.length() - 2);
		} else if (word.endsWith("?!")) {
			result.nag = Nag.NAG_5;
			result.move = word.substring(0, word.length() - 2);
		} else if (word.endsWith("+=")) {
			result.nag = Nag.NAG_14;
			result.move = word.substring(0, word.length() - 2);
		} else if (word.endsWith("=+")) {
			result.nag = Nag.NAG_15;
			result.move = word.substring(0, word.length() - 2);
		} else if (word.endsWith("+/-")) {
			result.nag = Nag.NAG_16;
			result.move = word.substring(0, word.length() - 3);
		} else if (word.endsWith("-/+")) {
			result.nag = Nag.NAG_17;
			result.move = word.substring(0, word.length() - 3);
		} else if (word.endsWith("+-")) {
			result.nag = Nag.NAG_18;
			result.move = word.substring(0, word.length() - 2);
		} else if (word.endsWith("-+")) {
			result.nag = Nag.NAG_19;
			result.move = word.substring(0, word.length() - 2);
		} else if (word.endsWith("+--")) {
			result.nag = Nag.NAG_20;
			result.move = word.substring(0, word.length() - 3);
		} else if (word.endsWith("--+")) {
			result.nag = Nag.NAG_21;
			result.move = word.substring(0, word.length() - 3);
		} else if (word.endsWith("!")) {
			result.nag = Nag.NAG_1;
			result.move = word.substring(0, word.length() - 1);
		} else if (word.endsWith("?")) {
			result.nag = Nag.NAG_2;
			result.move = word.substring(0, word.length() - 1);
		} else if (word.endsWith("=")) {
			result.nag = Nag.NAG_11;
			result.move = word.substring(0, word.length() - 1);
		} else {
			result.move = word;
		}
		return result;
	}
}
