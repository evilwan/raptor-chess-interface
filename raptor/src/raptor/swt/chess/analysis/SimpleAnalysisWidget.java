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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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
import raptor.engine.uci.UCIBestMove;
import raptor.engine.uci.UCIEngine;
import raptor.engine.uci.UCIInfo;
import raptor.engine.uci.UCIInfoListener;
import raptor.engine.uci.UCIMove;
import raptor.engine.uci.UCIOption;
import raptor.engine.uci.info.BestLineFoundInfo;
import raptor.engine.uci.info.CPULoadInfo;
import raptor.engine.uci.info.DepthInfo;
import raptor.engine.uci.info.NodesPerSecondInfo;
import raptor.engine.uci.info.NodesSearchedInfo;
import raptor.engine.uci.info.ScoreInfo;
import raptor.engine.uci.info.TimeInfo;
import raptor.engine.uci.options.UCIButton;
import raptor.pref.PreferenceKeys;
import raptor.service.ThreadService;
import raptor.service.UCIEngineService;
import raptor.swt.RaptorTable;
import raptor.swt.UCIEnginePropertiesDialog;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.EngineAnalysisWidget;
import raptor.util.RaptorStringUtils;

public class SimpleAnalysisWidget implements EngineAnalysisWidget {
	private static final Log LOG = LogFactory
			.getLog(SimpleAnalysisWidget.class);

	protected ChessBoardController controller;
	protected Composite composite, topLine;
	protected UCIEngine currentEngine;
	protected CLabel engineComboCLabel;
	protected CLabel nodesPerSecondLabelLabel;
	protected CLabel nodesPerSecondLabel;
	protected CLabel cpuPercentageLabelLabel;
	protected CLabel cpuPercentageLabel;
	protected Combo engineCombo;
	protected RaptorTable bestMoves;
	protected Button stopButton, startButton, propertiesButton;
	protected boolean ignoreEngineSelection;
	protected boolean isInStart = false;
	protected UCIInfoListener listener = new UCIInfoListener() {
		public void engineSentBestMove(UCIBestMove uciBestMove) {
		}

		public void engineSentInfo(final UCIInfo[] infos) {
			Raptor.getInstance().getDisplay().asyncExec(new Runnable() {
				public void run() {
					String score = null;
					String time = null;
					String depth = null;
					String nodes = null;
					String cpu = null;
					String nps = null;
					List<String> pvs = new ArrayList<String>(3);

					for (UCIInfo info : infos) {
						if (info instanceof ScoreInfo) {
							ScoreInfo scoreInfo = (ScoreInfo) info;
							if (((ScoreInfo) info).getMateInMoves() != 0) {
								score = "Mate in " + scoreInfo.getMateInMoves();
							} else if (scoreInfo.isLowerBoundScore()) {
								score = "-inf";
							} else if (scoreInfo.isUpperBoundScore()) {
								score = "+inf";
							} else {
								double scoreAsDouble = controller.getGame()
										.isWhitesMove()
										|| !currentEngine
												.isMultiplyBlackScoreByMinus1() ? scoreInfo
										.getValueInCentipawns() / 100.0
										: -scoreInfo.getValueInCentipawns() / 100.0;

								score = ""
										+ new BigDecimal(scoreAsDouble)
												.setScale(
														2,
														BigDecimal.ROUND_HALF_UP)
												.toString();
							}
						} else if (info instanceof DepthInfo) {
							DepthInfo depthInfo = (DepthInfo) info;
							depth = depthInfo.getSearchDepthPlies() + " plies";
						} else if (info instanceof NodesSearchedInfo) {
							NodesSearchedInfo nodesSearchedInfo = (NodesSearchedInfo) info;
							nodes = RaptorStringUtils.formatAsNumber(""
									+ nodesSearchedInfo.getNodesSearched()
									/ 1000);
						} else if (info instanceof CPULoadInfo) {
							CPULoadInfo cpuLoad = (CPULoadInfo) info;
							cpu = new BigDecimal(
									cpuLoad.getCpuUsage() / 1000.0 * 100)
									.setScale(0, BigDecimal.ROUND_HALF_UP)
									.toString();
							cpu = StringUtils.rightPad(cpu, 4);
						} else if (info instanceof NodesPerSecondInfo) {
							NodesPerSecondInfo nodesPerSecondInfo = (NodesPerSecondInfo) info;
							nps = RaptorStringUtils.formatAsNumber(""
									+ nodesPerSecondInfo.getNodesPerSecond()
									/ 1000);
							nps = StringUtils.rightPad(nps, 6);
						} else if (info instanceof TimeInfo) {
							TimeInfo timeInfo = (TimeInfo) info;
							time = new BigDecimal(
									timeInfo.getTimeMillis() / 1000.0)
									.setScale(1, BigDecimal.ROUND_HALF_UP)
									.toString();
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
									Move gameMove = null;

									if (move.isPromotion()) {
										gameMove = gameClone.makeMove(move
												.getStartSquare(), move
												.getEndSquare(), move
												.getPromotedPiece());
									} else {
										gameMove = gameClone.makeMove(move
												.getStartSquare(), move
												.getEndSquare());
									}

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
															: "")
													+ (gameClone.isCheckmate() ? "#"
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
							pvs.add(line.toString());
						}
					}

					final String finalScore = score;
					final String finalTime = time;
					final String finalDepth = depth;
					final String finalNodes = nodes;
					final List<String> finalPVs = pvs;
					final String finalCPU = cpu;
					final String finalNPS = nps;

					Raptor.getInstance().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (composite.isDisposed()) {
								return;
							}
							if (!finalPVs.isEmpty()) {
								String[][] data = new String[bestMoves
										.getRowCount()
										+ finalPVs.size()][5];

								for (int i = 0; i < finalPVs.size(); i++) {
									data[0][0] = StringUtils
											.defaultString(finalScore);
									data[0][1] = StringUtils
											.defaultString(finalDepth);
									data[0][2] = StringUtils
											.defaultString(finalTime);
									data[0][3] = StringUtils
											.defaultString(finalNodes);
									data[0][4] = StringUtils
											.defaultString(finalPVs.get(i));
								}

								for (int i = 0; i < bestMoves.getRowCount(); i++) {
									for (int j = 0; j < bestMoves
											.getColumnCount(); j++) {
										data[i + finalPVs.size()][j] = bestMoves
												.getText(i, j);
									}
								}

								bestMoves.refreshTable(data);
							} else if (bestMoves.getRowCount() > 0) {
								if (StringUtils.isNotBlank(finalScore)) {
									bestMoves.setText(0, 0, finalScore);
								}
								if (StringUtils.isNotBlank(finalDepth)) {
									bestMoves.setText(0, 1, finalDepth);
								}
								if (StringUtils.isNotBlank(finalTime)) {
									bestMoves.setText(0, 2, finalTime);
								}

								if (StringUtils.isNotBlank(finalNodes)) {
									bestMoves.setText(0, 3, finalNodes);
								}
							}
							if (finalCPU != null) {
								cpuPercentageLabel.setText(finalCPU);
							}
							if (finalNPS != null) {
								nodesPerSecondLabel.setText(finalNPS);
							}
						}
					});
				}
			});
		}
	};

	public void clear() {
		Raptor.getInstance().getDisplay().asyncExec(new Runnable() {
			public void run() {
				bestMoves.clearTable();
				cpuPercentageLabel.setText("   ");
				nodesPerSecondLabel.setText("      ");
			}
		});
	}

	public Composite create(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		topLine = new Composite(composite, SWT.LEFT);
		topLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		topLine.setLayout(new RowLayout());

		engineComboCLabel = new CLabel(topLine, SWT.LEFT);
		engineComboCLabel.setText("Engine:");

		engineCombo = new Combo(topLine, SWT.DROP_DOWN | SWT.READ_ONLY);
		engineCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ignoreEngineSelection) {
					return;
				}

				final String value = engineCombo.getText();
				if (LOG.isDebugEnabled()) {
					LOG.debug("engineCombo value selected: " + value);
				}

				if (currentEngine != null) {
					if (currentEngine.getUserName().equals(value)) {
						return;
					}

					final UCIEngine engineToQuit = currentEngine;
					ThreadService.getInstance().run(new Runnable() {
						public void run() {
							engineToQuit.quit();
						}
					});

				}
				ThreadService.getInstance().run(new Runnable() {
					public void run() {
						try {
							currentEngine = UCIEngineService.getInstance()
									.getUCIEngine(value).getDeepCopy();
							if (LOG.isDebugEnabled()) {
								LOG.debug("Changing engine to : "
										+ currentEngine.getUserName());
							}
							start(true);
						} catch (Throwable t) {
							LOG.error("Error switching chess engines", t);
						}
					}
				});
			}
		});

		nodesPerSecondLabelLabel = new CLabel(topLine, SWT.LEFT);
		nodesPerSecondLabelLabel.setText("NPS(K):");
		nodesPerSecondLabelLabel
				.setToolTipText("Nodes per second in thousands");

		nodesPerSecondLabel = new CLabel(topLine, SWT.LEFT);
		nodesPerSecondLabel.setToolTipText("Nodes per second in thousands");
		nodesPerSecondLabel.setText("       ");

		cpuPercentageLabelLabel = new CLabel(topLine, SWT.LEFT);
		cpuPercentageLabelLabel.setText("CPU%:");
		cpuPercentageLabelLabel
				.setToolTipText("Percentage of cpu being used by the engine");

		cpuPercentageLabel = new CLabel(topLine, SWT.LEFT);
		cpuPercentageLabel.setText("   ");
		cpuPercentageLabel
				.setToolTipText("Percentage of cpu being used by the engine");

		startButton = new Button(topLine, SWT.PUSH);
		startButton.setText("Start");
		startButton
				.setToolTipText("Starts analysis if it is not currently running.");
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				start();
			}
		});

		stopButton = new Button(topLine, SWT.PUSH);
		stopButton.setText("Stop");
		stopButton.setToolTipText("Stops analysis if it is currently running.");
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stop();
			}
		});

		propertiesButton = new Button(topLine, SWT.PUSH);
		propertiesButton.setText("Engine Properties");
		propertiesButton.setToolTipText("Shows the engines custom properties.");
		propertiesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UCIEnginePropertiesDialog dialog = new UCIEnginePropertiesDialog(
						composite.getShell(), currentEngine);
				dialog.open();

				ThreadService.getInstance().run(new Runnable() {
					public void run() {
						currentEngine.stop();
						currentEngine.quit();
						currentEngine.connect();
						start(true);
					}
				});
			}
		});

		bestMoves = new RaptorTable(composite, SWT.BORDER | SWT.FULL_SELECTION,
				false, true);
		bestMoves.setToolTipText("The current best lines in the position.");
		bestMoves.addColumn("Score(Pawns)", SWT.LEFT, 10, false, null);
		bestMoves.addColumn("Depth(ply)", SWT.LEFT, 10, false, null);
		bestMoves.addColumn("Time(sec)", SWT.LEFT, 10, false, null);
		bestMoves.addColumn("Nodes(K)", SWT.LEFT, 10, false, null);
		bestMoves.addColumn("Principal Variation", SWT.LEFT, 60, false, null);
		bestMoves.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		updateEnginesCombo();
		updateCustomButtons();

		updateFromPrefs();
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
		Raptor.getInstance().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!composite.isDisposed()) {
					clear();
				}
			}
		});
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
		if (composite.isVisible()) {
			start(false);
		}
	}

	public void stop() {
		if (currentEngine != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					currentEngine.stop();
					currentEngine.isReady();
				}
			});
		}
	}

	public void updateFromPrefs() {
		Color background = Raptor.getInstance().getPreferences().getColor(
				PreferenceKeys.BOARD_BACKGROUND_COLOR);
		Color foreground = Raptor.getInstance().getPreferences().getColor(
				PreferenceKeys.BOARD_STATUS_COLOR);
		composite.setBackground(background);
		topLine.setBackground(background);
		engineComboCLabel.setBackground(background);
		engineComboCLabel.setForeground(foreground);
		nodesPerSecondLabelLabel.setBackground(background);
		nodesPerSecondLabelLabel.setForeground(foreground);
		nodesPerSecondLabel.setBackground(background);
		nodesPerSecondLabel.setForeground(foreground);
		cpuPercentageLabelLabel.setBackground(background);
		cpuPercentageLabelLabel.setForeground(foreground);
		cpuPercentageLabel.setBackground(background);
		cpuPercentageLabel.setForeground(foreground);
	}

	public void updateToGame() {
		start();
	}

	protected void start(boolean override) {

		if (currentEngine != null && (!isInStart || override)) {
			isInStart = true;
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					if (LOG.isDebugEnabled()) {
						LOG.debug("In SimpleAnalysisWidget.start("
								+ currentEngine.getUserName() + ")");
					}
					try {
						if (!currentEngine.isConnected()) {
							currentEngine.connect();
						}
						Raptor.getInstance().getDisplay().asyncExec(
								new Runnable() {
									public void run() {
										clear();
									}
								});
						currentEngine.stop();
						currentEngine.newGame();
						currentEngine.setPosition(controller.getGame().toFen(),
								null);
						currentEngine.isReady();
						currentEngine.go(currentEngine
								.getGoAnalysisParameters(), listener);
					} catch (Throwable t) {
						LOG.error("Error starting engine", t);
					} finally {
						isInStart = false;
					}
				}
			});
		}
	}

	protected void updateCustomButtons() {
		if (currentEngine != null) {
			for (Control control : topLine.getChildren()) {
				if (control instanceof Button && control != stopButton
						&& control != startButton
						&& control != propertiesButton) {
					control.dispose();
				}
			}

			String[] controlNames = currentEngine.getOptionNames();
			for (String controlName : controlNames) {
				final UCIOption option = currentEngine.getOption(controlName);
				if (option instanceof UCIButton) {
					Button button = new Button(topLine, SWT.PUSH);
					button.setText(controlName);
					button.setToolTipText("Custom engine analyis button.");
					button.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							ThreadService.getInstance().run(new Runnable() {
								public void run() {
									currentEngine.stop();
									currentEngine.isReady();
									currentEngine.setOption(option);
									currentEngine.isReady();
									start(true);
								}
							});
						}
					});
				}
			}
			topLine.pack(true);
			topLine.layout(true, true);
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
		topLine.pack(true);
		topLine.layout(true, true);
	}
}
