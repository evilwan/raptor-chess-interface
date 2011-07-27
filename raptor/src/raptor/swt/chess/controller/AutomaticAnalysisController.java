/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chess.controller;

import java.math.BigDecimal;

import raptor.engine.uci.UCIInfo;
import raptor.engine.uci.info.BestLineFoundInfo;
import raptor.engine.uci.info.ScoreInfo;
import raptor.international.L10n;
import raptor.service.ThreadService;
import raptor.swt.chess.movelist.TextAreaMoveList;
import raptor.swt.chess.analysis.UciAnalysisWidget;

public class AutomaticAnalysisController {
	protected static L10n local = L10n.getInstance();
	private InactiveController boardController;
	private ScoreInfo previousPosScore;
	private ScoreInfo thisPosScore;
	private BestLineFoundInfo thisPosBestLine;
	private boolean isMultiplyBlackScoreByMinus;

	public AutomaticAnalysisController(InactiveController boardController) {
		this.boardController = boardController;
	}

	private double asDouble(ScoreInfo score) {
		return (boardController.getGame().isWhitesMove() || isMultiplyBlackScoreByMinus) ? score
				.getValueInCentipawns() / 100.0
				: -score.getValueInCentipawns() / 100.0;
	}

	public void startAnalysis(final int secsPerMove, final float threshold,
			final int startMove) {
		final AutomaticAnalysisController thisCont = this;
		ThreadService.getInstance().run(new Runnable() {

			@Override
			public void run() {
				boardController.getBoard().getControl().getDisplay()
						.syncExec(new Runnable() {
							@Override
							public void run() {
								boardController.getBoard()
										.showEngineAnalysisWidget();
								boardController.getBoard().showMoveList();
							}
						});
				((UciAnalysisWidget) boardController.getBoard()
						.getEngineAnalysisWidget())
						.setAnalysisController(thisCont);

				int nOfMoves = boardController.getGame().getMoveList()
						.getSize();
				for (int i = startMove * 2 - 1; i <= nOfMoves; i++) {
					final int ii = i;
					boardController.getBoard().getControl().getDisplay()
							.syncExec(new Runnable() {
								@Override
								public void run() {
									boardController.gotoMove(ii);
								}
							});

					try {
						Thread.sleep(secsPerMove * 1000);
					} catch (InterruptedException e) {
					}
					boardController.getBoard().getControl().getDisplay()
							.syncExec(new Runnable() {
								@Override
								public void run() {
									String score;
									if (thisPosScore.getMateInMoves() != 0) {
										score = local.getString("uciAnalW_0")
												+ Math.abs(thisPosScore.getMateInMoves());
									} else if (thisPosScore.isLowerBoundScore()) {
										score = local.getString("uciAnalW_1");
									} else if (thisPosScore.isUpperBoundScore()) {
										score = local.getString("uciAnalW_2");
									} else {
										double scoreAsDouble = asDouble(thisPosScore);

										score = ""
												+ new BigDecimal(scoreAsDouble)
														.setScale(
																2,
																BigDecimal.ROUND_HALF_UP)
														.toString();
									}

									boolean bad = false;
									if (previousPosScore != null && thisPosScore.getMateInMoves() == 0) {
										System.out.println("This: " + asDouble(thisPosScore));
										System.out.println("Prev: " + asDouble(previousPosScore));
										System.out.println(boardController.getGame().isWhitesMove());
										double prevMoveDiff = boardController
												.getGame().isWhitesMove() ? (-asDouble(thisPosScore) + asDouble(previousPosScore))
												: (-asDouble(previousPosScore) - asDouble(thisPosScore));

										if (prevMoveDiff > threshold * 2) {
											score += " VERY BAD!";
											bad = true;
										}
										else if (prevMoveDiff > threshold) {
											score += " BAD!";
											bad = true;
										}
									}
									score = "(" + score + ")";
									((TextAreaMoveList) boardController
											.getBoard().getMoveList())
											.addCommentToMove(ii - 1, score, bad);
									previousPosScore = thisPosScore;
								}
							});
				}
				boardController.getBoard().getControl().getDisplay()
						.syncExec(new Runnable() {
							@Override
							public void run() {
								boardController.getBoard()
										.hideEngineAnalysisWidget();
							}
						});
			}

		});

	}

	public void engineSentInfo(UCIInfo[] infos,
			boolean isMultiplyBlackScoreByMinus) {
		this.isMultiplyBlackScoreByMinus = isMultiplyBlackScoreByMinus;
		for (UCIInfo info : infos) {
			if (info instanceof ScoreInfo) {
				thisPosScore = (ScoreInfo) info;
			} else if (info instanceof BestLineFoundInfo)
				thisPosBestLine = (BestLineFoundInfo) info;
		}
	}

}
