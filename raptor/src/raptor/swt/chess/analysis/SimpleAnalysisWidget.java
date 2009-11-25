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
package raptor.swt.chess.analysis;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.Move;
import raptor.chess.util.GameUtils;
import raptor.engine.uci.UCIEngine;
import raptor.engine.uci.UCIInfo;
import raptor.engine.uci.UCIInfoListener;
import raptor.engine.uci.UCIMove;
import raptor.engine.uci.UCIOption;
import raptor.engine.uci.info.BestLineFoundInfo;
import raptor.engine.uci.info.DepthInfo;
import raptor.engine.uci.info.NodesPerSecondInfo;
import raptor.engine.uci.info.NodesSearchedInfo;
import raptor.engine.uci.info.ScoreInfo;
import raptor.engine.uci.info.TimeInfo;
import raptor.engine.uci.options.UCIButton;
import raptor.service.ThreadService;
import raptor.service.UCIEngineService;
import raptor.swt.RaptorTable;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.EngineAnalysisWidget;
import raptor.util.RaptorStringUtils;

public class SimpleAnalysisWidget implements EngineAnalysisWidget {
	private static final Log LOG = LogFactory
			.getLog(SimpleAnalysisWidget.class);

	protected ChessBoardController controller;
	protected Composite composite;
	protected UCIEngine currentEngine;
	protected Combo engineCombo;
	protected CLabel scoreCLabel;
	protected CLabel depthCLabel;
	protected CLabel nodesSearchedCLabel;
	protected CLabel nodesPerSecondCLabel;
	protected CLabel timeInfoCLabel;
	protected RaptorTable bestMoves;
	protected Button stopButton;
	protected Button startButton;
	protected Composite customButtons;
	protected boolean ignoreEngineSelection;
	protected boolean isInStart = false;
	protected UCIInfoListener listener = new UCIInfoListener() {
		public void engineSentInfo(final UCIInfo[] infos) {
			Raptor.getInstance().getDisplay().asyncExec(new Runnable() {
				public void run() {
					for (UCIInfo info : infos) {
						if (info instanceof ScoreInfo) {
							ScoreInfo scoreInfo = (ScoreInfo) info;
							if (((ScoreInfo) info).getMateInMoves() != 0) {
								scoreCLabel.setText("Mate in "
										+ scoreInfo.getMateInMoves());
							} else if (scoreInfo.isLowerBoundScore()) {
								scoreCLabel.setText("-infinity");
							} else if (scoreInfo.isUpperBoundScore()) {
								scoreCLabel.setText("+infinity");
							} else {
								scoreCLabel
										.setText(""
												+ new BigDecimal(
														scoreInfo
																.getValueInCentipawns() / 100.0)
														.setScale(
																2,
																BigDecimal.ROUND_HALF_UP)
														.toString());
							}
						} else if (info instanceof DepthInfo) {
							DepthInfo depthInfo = (DepthInfo) info;
							depthCLabel.setText(depthInfo.getSearchDepthPlies()
									+ " plies");
						} else if (info instanceof NodesSearchedInfo) {
							NodesSearchedInfo nodesSearchedInfo = (NodesSearchedInfo) info;
							nodesSearchedCLabel.setText(RaptorStringUtils
									.formatAsNumber(""
											+ nodesSearchedInfo
													.getNodesSearched() / 1000)
									+ "K");
						} else if (info instanceof NodesPerSecondInfo) {
							NodesPerSecondInfo nodesPerSecondInfo = (NodesPerSecondInfo) info;
							nodesPerSecondCLabel
									.setText(RaptorStringUtils
											.formatAsNumber(""
													+ nodesPerSecondInfo
															.getNodesPerSecond()
													/ 1000)
											+ "K");
						} else if (info instanceof TimeInfo) {
							TimeInfo timeInfo = (TimeInfo) info;
							timeInfoCLabel.setText(new BigDecimal(timeInfo
									.getTimeMillis() / 1000.0).setScale(1,
									BigDecimal.ROUND_HALF_UP).toString());
						} else if (info instanceof BestLineFoundInfo) {
							BestLineFoundInfo bestLineFoundInfo = (BestLineFoundInfo) info;
							StringBuilder line = new StringBuilder(100);
							Game gameClone = controller.getGame()
									.deepCopy(true);
							gameClone.addState(Game.UPDATING_SAN_STATE);
							gameClone
									.clearState(Game.UPDATING_ECO_HEADERS_STATE);

							boolean isFirstMove = true;

							for (UCIMove move : bestLineFoundInfo.getMoves()) {
								try {
									Move gameMove = gameClone.makeMove(move
											.getStartSquare(), move
											.getEndSquare(), move
											.getPromotedPiece());
									String san = GameUtils
											.convertSanToUseUnicode(gameMove
													.getSan(), gameMove
													.isWhitesMove());
									String moveNumber = isFirstMove
											&& !gameMove.isWhitesMove() ? gameMove
											.getFullMoveCount()
											+ ") ... "
											: gameMove.isWhitesMove() ? gameMove
													.getFullMoveCount()
													+ ") "
													: "";
									line
											.append((line.equals("") ? "" : " ")
													+ moveNumber
													+ san
													+ (gameClone.isInCheck() ? "+"
															: ""));
									isFirstMove = false;
								} catch (Throwable t) {
									LOG
											.warn(
													"Illegal line found skipping line.",
													t);
									break;
								}
							}
							bestMoves.refreshTable(new String[][] { { line
									.toString() } });
						}
					}
				}
			});
		}
	};

	public void clear() {
		scoreCLabel.setText("               ");
		depthCLabel.setText("               ");
		nodesSearchedCLabel.setText("               ");
		timeInfoCLabel.setText("               ");
		nodesPerSecondCLabel.setText("               ");
		bestMoves.clearTable();
	}

	public Composite create(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(6, false));

		CLabel engineComboCLabel = new CLabel(composite, SWT.LEFT);
		engineComboCLabel.setText("Engine:");
		engineComboCLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));

		engineCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		engineCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		engineCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!ignoreEngineSelection) {
					if (currentEngine != null
							&& !StringUtils.equals(engineCombo.getText(),
									currentEngine.getUserName())) {
						ThreadService.getInstance().run(new Runnable() {
							public void run() {
								if (currentEngine != null) {
									currentEngine.quit();
								}
								currentEngine = UCIEngineService.getInstance()
										.getUCIEngine(engineCombo.getText())
										.getDeepCopy();

								Raptor.getInstance().getDisplay().asyncExec(
										new Runnable() {
											public void run() {
												clear();
											}
										});

								currentEngine.connect();
								start();
							}
						});
					}
				}
			}
		});

		CLabel scoreHeaderCLabel = new CLabel(composite, SWT.LEFT);
		scoreHeaderCLabel.setText("Score:");
		scoreHeaderCLabel
				.setToolTipText("Score in pawns. Negative score is a black advantage. Positive score is a white advantage.");
		scoreHeaderCLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));

		scoreCLabel = new CLabel(composite, SWT.LEFT);
		scoreCLabel
				.setToolTipText("Score in pawns. Negative score is a black advantage. Positive score is a white advantage.");
		scoreCLabel.setText("               ");
		scoreCLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));

		CLabel depthHeaderCLabel = new CLabel(composite, SWT.LEFT);
		depthHeaderCLabel.setToolTipText("The current depth searched.");
		depthHeaderCLabel.setText("Depth:");
		depthHeaderCLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));

		depthCLabel = new CLabel(composite, SWT.LEFT);
		depthCLabel.setToolTipText("The current depth searched.");
		depthCLabel.setText("               ");
		depthCLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));

		CLabel nodesSearchedHeaderCLabel = new CLabel(composite, SWT.LEFT);
		nodesSearchedHeaderCLabel
				.setToolTipText("The total number of positions searched.");
		nodesSearchedHeaderCLabel.setText("Nodes Searched:");
		nodesSearchedHeaderCLabel.setLayoutData(new GridData(SWT.LEFT,
				SWT.CENTER, false, false, 1, 1));

		nodesSearchedCLabel = new CLabel(composite, SWT.LEFT);
		nodesSearchedCLabel
				.setToolTipText("The total number of positions searched.");
		nodesSearchedCLabel.setText("               ");
		nodesSearchedCLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));

		CLabel npsHeaderCLabel = new CLabel(composite, SWT.LEFT);
		npsHeaderCLabel
				.setToolTipText("The average number of positions searched in a second.");
		npsHeaderCLabel.setText("Nodes Per Second:");
		npsHeaderCLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));

		nodesPerSecondCLabel = new CLabel(composite, SWT.LEFT);
		nodesPerSecondCLabel
				.setToolTipText("The average number of positions searched in a second.");
		nodesPerSecondCLabel.setText("               ");
		nodesPerSecondCLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));

		CLabel timeInfoHeaderCLabel = new CLabel(composite, SWT.LEFT);
		timeInfoHeaderCLabel.setText("Time (seconds):");
		timeInfoHeaderCLabel
				.setToolTipText("The amount of time taken for the last search in seconds.");
		timeInfoHeaderCLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));

		timeInfoCLabel = new CLabel(composite, SWT.LEFT);
		timeInfoCLabel.setText("               ");
		timeInfoCLabel
				.setToolTipText("The amount of time taken for the last search in seconds.");
		timeInfoCLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));

		bestMoves = new RaptorTable(composite, SWT.BORDER | SWT.FULL_SELECTION,
				false, true);
		bestMoves.setToolTipText("The current best lines in the position.");
		bestMoves.addColumn("Best Moves", SWT.LEFT, 100, false, null);
		bestMoves.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6,
				1));

		customButtons = new Composite(composite, SWT.NONE);
		customButtons.setLayout(new RowLayout());
		customButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 6, 1));

		startButton = new Button(customButtons, SWT.PUSH);
		startButton.setText("Start");
		startButton
				.setToolTipText("Starts analysis if it is not currently running.");
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				start();
			}
		});

		stopButton = new Button(customButtons, SWT.PUSH);
		stopButton.setText("Stop");
		stopButton.setToolTipText("Stops analysis if it is currently running.");
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stop();
			}
		});
		updateEnginesCombo();
		updateCustomButtons();
		return composite;
	}

	public ChessBoardController getChessBoardController() {
		return controller;
	}

	public Composite getControl() {
		return composite;
	}

	public void onShow() {
		clear();
		updateEnginesCombo();
		updateCustomButtons();
		start();
	}

	public void quit() {
		if (!composite.isDisposed()) {
			clear();
		}
		if (currentEngine != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					currentEngine.quit();
				}
			});
		}
	}

	public void setController(ChessBoardController controller) {
		this.controller = controller;
	}

	public void start() {
		if (currentEngine != null && composite.isVisible() && !isInStart) {
			isInStart = true;
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					try {
						if (!currentEngine.isConnected()) {
							currentEngine.connect();
						}
						currentEngine.stop();
						currentEngine.newGame();
						currentEngine.isReady();

						currentEngine.setPosition(controller.getGame().toFen(),
								null);
						currentEngine.go("infinite", listener);
					} catch (Throwable t) {
						LOG.error("Error starting engine", t);
					} finally {
						isInStart = false;
					}
				}
			});
		}
	}

	public void stop() {
		if (currentEngine != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					currentEngine.stop();
				}
			});
		}
	}

	public void updateToGame() {
		start();
	}

	protected void updateCustomButtons() {
		if (currentEngine != null) {
			for (Control control : customButtons.getChildren()) {
				if (control != stopButton && control != startButton) {
					control.dispose();
				}
			}

			String[] controlNames = currentEngine.getOptionNames();
			for (String controlName : controlNames) {
				final UCIOption option = currentEngine.getOption(controlName);
				if (option instanceof UCIButton) {
					Button button = new Button(customButtons, SWT.PUSH);
					button.setText(controlName);
					button.setToolTipText("Custom engine analyis button.");
					button.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							ThreadService.getInstance().run(new Runnable() {
								public void run() {
									stop();
									currentEngine.setOption(option);
									start();
								}
							});
						}
					});
				}
			}
			customButtons.layout(true, true);
		}
	}

	protected void updateEnginesCombo() {
		ignoreEngineSelection = true;
		engineCombo.removeAll();
		UCIEngine[] engines = UCIEngineService.getInstance().getUCIEngines();
		for (UCIEngine engine : engines) {
			engineCombo.add(engine.getUserName());
		}

		UCIEngine defaultEngine = UCIEngineService.getInstance()
				.getDefaultEngine();
		if (defaultEngine != null) {
			for (int i = 0; i < engineCombo.getItemCount(); i++) {
				if (engineCombo.getItem(i).equals(defaultEngine.getUserName())) {
					currentEngine = engines[i].getDeepCopy();
					engineCombo.select(i);
					break;
				}
			}
		}
		ignoreEngineSelection = false;
	}
}
