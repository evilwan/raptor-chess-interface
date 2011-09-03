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
package raptor.swt.chess.analysis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.util.GameUtils;
import raptor.engine.uci.UCIBestMove;
import raptor.engine.uci.UCIEngine;
import raptor.engine.uci.UCIInfo;
import raptor.engine.uci.UCIInfoListener;
import raptor.engine.uci.UCIMove;
import raptor.engine.uci.info.BestLineFoundInfo;
import raptor.engine.uci.info.CPULoadInfo;
import raptor.engine.uci.info.DepthInfo;
import raptor.engine.uci.info.NodesPerSecondInfo;
import raptor.engine.uci.info.NodesSearchedInfo;
import raptor.engine.uci.info.ScoreInfo;
import raptor.engine.uci.info.TimeInfo;
import raptor.engine.uci.options.UCICheck;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.service.ThreadService;
import raptor.service.UCIEngineService;
import raptor.swt.RaptorTable;
import raptor.swt.RaptorTable.RaptorTableAdapter;
import raptor.swt.SWTUtils;
import raptor.swt.UCIEnginePropertiesDialog;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.EngineAnalysisWidget;
import raptor.swt.chess.controller.AutomaticAnalysisController;
import raptor.util.RaptorLogger;
import raptor.util.RaptorRunnable;
import raptor.util.RaptorStringUtils;

public class UciAnalysisWidget implements EngineAnalysisWidget {
	private static final RaptorLogger LOG = RaptorLogger
			.getLog(UciAnalysisWidget.class);

	protected ChessBoardController controller;
	protected AutomaticAnalysisController analysisController;
	protected Composite composite, topLine, labelComposite;
	protected UCIEngine currentEngine;
	protected Label nodesPerSecondLabel;
	protected Label cpuPercentageLabel;
	protected Combo engineCombo;
	protected RaptorTable bestMoves;
	protected Button startStopButton, propertiesButton;
	protected boolean ignoreEngineSelection;
	protected boolean isInStart = false;
	protected static L10n local = L10n.getInstance();
	protected UCIInfoListener listener = new UCIInfoListener() {
		public void engineSentBestMove(UCIBestMove uciBestMove) {
		}

		public void engineSentInfo(final UCIInfo[] infos) {
			if (analysisController != null)
				analysisController.engineSentInfo(infos, currentEngine
						.isMultiplyBlackScoreByMinus1());
			
			Raptor.getInstance().getDisplay()
					.asyncExec(new RaptorRunnable(controller.getConnector()) {
						@Override
						public void execute() {
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
										score = local.getString("uciAnalW_0") 
												+ scoreInfo.getMateInMoves();
									} else if (scoreInfo.isLowerBoundScore()) {
										score = local.getString("uciAnalW_1"); 
									} else if (scoreInfo.isUpperBoundScore()) {
										score = local.getString("uciAnalW_2"); 
									} else {
										double scoreAsDouble = controller
												.getGame().isWhitesMove()
												|| !currentEngine
														.isMultiplyBlackScoreByMinus1() ? scoreInfo
												.getValueInCentipawns() / 100.0
												: -scoreInfo
														.getValueInCentipawns() / 100.0;

										score = "" 
												+ new BigDecimal(scoreAsDouble)
														.setScale(
																2,
																BigDecimal.ROUND_HALF_UP)
														.toString();
									}
								} else if (info instanceof DepthInfo) {
									DepthInfo depthInfo = (DepthInfo) info;
									depth = depthInfo.getSearchDepthPlies()
											+ local.getString("uciAnalW_4");
								} else if (info instanceof NodesSearchedInfo) {
									NodesSearchedInfo nodesSearchedInfo = (NodesSearchedInfo) info;
									nodes = RaptorStringUtils.formatAsNumber("" 
											+ nodesSearchedInfo
													.getNodesSearched() / 1000);
								} else if (info instanceof CPULoadInfo) {
									CPULoadInfo cpuLoad = (CPULoadInfo) info;
									cpu = local.getString("uciAnalW_6") 
											+ new BigDecimal(
													cpuLoad.getCpuUsage() / 1000.0 * 100)
													.setScale(
															0,
															BigDecimal.ROUND_HALF_UP)
													.toString();
								} else if (info instanceof NodesPerSecondInfo) {
									NodesPerSecondInfo nodesPerSecondInfo = (NodesPerSecondInfo) info;
									nps = local.getString("uciAnalW_7") 
											+ RaptorStringUtils.formatAsNumber("" 
													+ nodesPerSecondInfo
															.getNodesPerSecond()
													/ 1000);
								} else if (info instanceof TimeInfo) {
									TimeInfo timeInfo = (TimeInfo) info;
									time = new BigDecimal(timeInfo
											.getTimeMillis() / 1000.0)
											.setScale(1,
													BigDecimal.ROUND_HALF_UP)
											.toString();
								} else if (info instanceof BestLineFoundInfo) {
									if (!currentEngine.isProcessingGo())
										return;
									
									BestLineFoundInfo bestLineFoundInfo = (BestLineFoundInfo) info;
									StringBuilder line = new StringBuilder(100);
									Game gameClone = GameFactory.createFromFen(currentEngine.getLastSetFen(),
											controller.getGame().getVariant());
									gameClone.addState(Game.UPDATING_SAN_STATE);
									gameClone
											.clearState(Game.UPDATING_ECO_HEADERS_STATE);

									boolean isFirstMove = true;

									for (UCIMove move : bestLineFoundInfo
											.getMoves()) {
										try {
											Move gameMove = null;

											if (move.isPromotion()) {
												gameMove = gameClone.makeMove(
														move.getStartSquare(),
														move.getEndSquare(),
														move.getPromotedPiece());
											} else {
												gameMove = gameClone.makeMove(
														move.getStartSquare(),
														move.getEndSquare());
											}

											String san = GameUtils.convertSanToUseUnicode(
													gameMove.getSan(),
													gameMove.isWhitesMove());
											String moveNumber = isFirstMove
													&& !gameMove.isWhitesMove() ? gameMove
													.getFullMoveCount()
													+ ") ... " : gameMove 
													.isWhitesMove() ? gameMove
													.getFullMoveCount() + ") " 
													: ""; 
											line.append((line.toString().equals("") ? ""  //$NON-NLS-2$
													: " ") 
													+ moveNumber
													+ san
													+ (gameClone.isInCheck() ? "+" 
															: "") 
													+ (gameClone.isCheckmate() ? "#" 
															: "")); 
											isFirstMove = false;
										} catch (Throwable t) {
											if (LOG.isInfoEnabled()) {
												LOG.info(
														"Illegal line found skipping line (This can occur if the position was " 
																+ "changing when the analysis line was being calculated).", 
														t);
											}
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

							Raptor.getInstance()
									.getDisplay()
									.asyncExec(
											new RaptorRunnable(controller
													.getConnector()) {
												@Override
												public void execute() {
													if (composite.isDisposed()) {
														return;
													}
													if (!finalPVs.isEmpty()) {
														String[][] data = new String[bestMoves
																.getRowCount()
																+ finalPVs
																		.size()][5];

														for (int i = 0; i < finalPVs
																.size(); i++) {
															data[0][0] = StringUtils
																	.defaultString(finalScore);
															data[0][1] = StringUtils
																	.defaultString(finalDepth);
															data[0][2] = StringUtils
																	.defaultString(finalTime);
															data[0][3] = StringUtils
																	.defaultString(finalNodes);
															data[0][4] = StringUtils
																	.defaultString(finalPVs
																			.get(i));
														}

														for (int i = 0; i < bestMoves
																.getRowCount(); i++) {
															for (int j = 0; j < bestMoves
																	.getColumnCount(); j++) {
																data[i
																		+ finalPVs
																				.size()][j] = bestMoves
																		.getText(
																				i,
																				j);
															}
														}

														bestMoves
																.refreshTable(data);
													} else if (bestMoves
															.getRowCount() > 0) {
														if (StringUtils
																.isNotBlank(finalScore)) {
															bestMoves.setText(
																	0, 0,
																	finalScore);
														}
														if (StringUtils
																.isNotBlank(finalDepth)) {
															bestMoves.setText(
																	0, 1,
																	finalDepth);
														}
														if (StringUtils
																.isNotBlank(finalTime)) {
															bestMoves.setText(
																	0, 2,
																	finalTime);
														}

														if (StringUtils
																.isNotBlank(finalNodes)) {
															bestMoves.setText(
																	0, 3,
																	finalNodes);
														}
													}
													if (finalCPU != null) {
														cpuPercentageLabel
																.setText(finalCPU);
														topLine.layout(true,
																true);
													}
													if (finalNPS != null) {
														nodesPerSecondLabel
																.setText(finalNPS);
														topLine.layout(true,
																true);
													}
												}
											});
						}
					});
		}
	};

	public void clear() {
		Raptor.getInstance().getDisplay()
				.asyncExec(new RaptorRunnable(controller.getConnector()) {
					@Override
					public void execute() {
						bestMoves.clearTable();
						nodesPerSecondLabel.setText("NPS(K):"); 
						cpuPercentageLabel.setText("CPU%:"); 
					}
				});
	}

	public Composite create(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		composite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (currentEngine != null) {
					currentEngine.quit();
				}
			}
		});

		topLine = new Composite(composite, SWT.LEFT);
		topLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginBottom = 0;
		rowLayout.marginTop = 0;
		rowLayout.marginLeft = 5;
		rowLayout.marginRight = 5;
		rowLayout.marginHeight = 2;
		rowLayout.marginWidth = 2;
		rowLayout.spacing = 0;
		topLine.setLayout(rowLayout);

		topLine.setLayout(new RowLayout());

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
				startStopButton.setText(local.getString("uciAnalW_7")); 
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

		labelComposite = new Composite(topLine, SWT.NONE);
		labelComposite.setLayout(SWTUtils
				.createMarginlessRowLayout(SWT.VERTICAL));
		nodesPerSecondLabel = new Label(labelComposite, SWT.LEFT);
		nodesPerSecondLabel.setToolTipText(local.getString("uciAnalW_10"));
		nodesPerSecondLabel.setText(StringUtils.rightPad(local.getString("uciAnalW_11"), 15)); 

		cpuPercentageLabel = new Label(labelComposite, SWT.LEFT);
		cpuPercentageLabel.setText(StringUtils.rightPad(local.getString("uciAnalW_12"), 10)); 
		cpuPercentageLabel
				.setToolTipText(local.getString("uciAnalW_30")); 

		startStopButton = new Button(topLine, SWT.FLAT);
		startStopButton.setText(local.getString("uciAnalW_31")); 
		startStopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (startStopButton.getText().equals(local.getString("uciAnalW_32"))) { 
					start();
					startStopButton.setText(local.getString("uciAnalW_33")); 
				} else {
					stop();
					startStopButton.setText(local.getString("uciAnalW_34")); 
				}
			}
		});

		propertiesButton = new Button(topLine, SWT.FLAT);
		propertiesButton.setText(local.getString("uciAnalW_35")); 
		propertiesButton.setToolTipText(local.getString("uciAnalW_36")); 
		propertiesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UCIEnginePropertiesDialog dialog = new UCIEnginePropertiesDialog(
						composite.getShell(), currentEngine);
				dialog.open();

				ThreadService.getInstance().run(new Runnable() {
					public void run() {
						boolean isStarted = currentEngine.isProcessingGo();
						currentEngine.stop();
						currentEngine.quit();
						currentEngine.connect();

						if (isStarted) {
							start(true);
						}
					}
				});
			}
		});

		bestMoves = new RaptorTable(composite, SWT.BORDER | SWT.FULL_SELECTION,
				false, true);
		bestMoves.setToolTipText(local.getString("uciAnalW_37")); 
		bestMoves.addColumn(local.getString("uciAnalW_38"), SWT.LEFT, 10, false, null); 
		bestMoves.addColumn(local.getString("uciAnalW_39"), SWT.LEFT, 10, false, null); 
		bestMoves.addColumn(local.getString("uciAnalW_40"), SWT.LEFT, 10, false, null); 
		bestMoves.addColumn(local.getString("uciAnalW_41"), SWT.LEFT, 10, false, null); 
		bestMoves.addColumn(local.getString("uciAnalW_42"), SWT.LEFT, 60, false, null); 
		bestMoves.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		bestMoves.addRaptorTableListener(new RaptorTableAdapter() {
			@Override
			public void rowRightClicked(MouseEvent event, final String[] rowData) {
				Menu menu = new Menu(UciAnalysisWidget.this.composite
						.getShell(), SWT.POP_UP);
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(local.getString("uciAnalW_43")); 
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Clipboard clipboard = new Clipboard(composite.getDisplay());
						String text = GameUtils.removeUnicodePieces(rowData[4]);
						TextTransfer textTransfer = TextTransfer.getInstance();
						Transfer[] transfers = new Transfer[] { textTransfer };
						Object[] data = new Object[] { text };
						clipboard.setContents(data, transfers);
						clipboard.dispose();
					}
				});
				menu.setVisible(true);
				while (!menu.isDisposed() && menu.isVisible()) {
					if (!composite.getDisplay().readAndDispatch()) {
						composite.getDisplay().sleep();
					}
				}
				menu.dispose();
			}
		});

		updateEnginesCombo();
		// updateCustomButtons();

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
		// updateCustomButtons();
		start();
		composite.layout(true, true);
	}

	public void quit() {
		Raptor.getInstance().getDisplay()
				.asyncExec(new RaptorRunnable(controller.getConnector()) {
					@Override
					public void execute() {
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
					if (currentEngine.isConnected()) {
						currentEngine.stop();
						currentEngine.isReady();
						Raptor.getInstance().getDisplay()
								.asyncExec(new RaptorRunnable() {
									@Override
									public void execute() {
										startStopButton.setText(local.getString("uciAnalW_44")); 
									}
								});
					}
				}
			});
		}
	}

	public void updateFromPrefs() {
		Color background = Raptor.getInstance().getPreferences()
				.getColor(PreferenceKeys.BOARD_BACKGROUND_COLOR);
		Color foreground = Raptor.getInstance().getPreferences()
				.getColor(PreferenceKeys.BOARD_CONTROL_COLOR);
		composite.setBackground(background);
		topLine.setBackground(background);
		labelComposite.setBackground(background);
		nodesPerSecondLabel.setBackground(background);
		nodesPerSecondLabel.setForeground(foreground);
		cpuPercentageLabel.setBackground(background);
		cpuPercentageLabel.setForeground(foreground);
	}

	public void updateToGame() {
		if (startStopButton.getText().equals(local.getString("uciAnalW_45"))) { 
			start();
		}
	}

	protected void start(boolean override) {
		if (currentEngine != null && (!isInStart || override)) {
			isInStart = true;
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					if (LOG.isDebugEnabled()) {
						LOG.debug("In UciAnalysisWidget.start("  
								+ currentEngine.getUserName() + ")");  
					}
					try {
						if (!currentEngine.isConnected()) {
							currentEngine.connect();
						}
						Raptor.getInstance()
								.getDisplay()
								.asyncExec(
										new RaptorRunnable(controller
												.getConnector()) {
											@Override
											public void execute() {
												clear();
											}
										});
						currentEngine.stop();

						if (controller.getGame().getVariant() == Variant.fischerRandom
								&& currentEngine.hasOption("UCI_Chess960")) {  
							UCICheck opt = (UCICheck) currentEngine
									.getOption("UCI_Chess960");  
							opt.setValue("true");  
						} else if (controller.getGame().getVariant() != Variant.fischerRandom
								&& currentEngine.hasOption("UCI_Chess960")) {  
							UCICheck opt = (UCICheck) currentEngine
									.getOption("UCI_Chess960");  
							opt.setValue("false");  
						}

						currentEngine.newGame();
						currentEngine.setPosition(controller.getGame().toFen(),
								null);
						currentEngine.isReady();
						currentEngine.go(
								currentEngine.getGoAnalysisParameters(),
								listener);
						Raptor.getInstance().getDisplay()
								.asyncExec(new RaptorRunnable() {
									@Override
									public void execute() {
										startStopButton.setText(local.getString("uciAnalW_54")); 
									}
								});

					} catch (Throwable t) {
						LOG.error("Error starting engine", t);  
					} finally {
						isInStart = false;
					}
				}
			});
		}
	}

	protected void updateEnginesCombo() {
		ignoreEngineSelection = true;
		engineCombo.removeAll();

		UCIEngine[] engines;
		UCIEngine defaultEngine;
		if (controller.getGame().getVariant() == Variant.fischerRandom) {
			engines = UCIEngineService.getInstance().getFrUCIEngines();

			if (engines.length > 0)
				defaultEngine = engines[0];
			else
				defaultEngine = null;
		} else {
			engines = UCIEngineService.getInstance().getUCIEngines();
			defaultEngine = UCIEngineService.getInstance().getDefaultEngine();
		}

		for (UCIEngine engine : engines)
			engineCombo.add(engine.getUserName());

		for (int i = 0; i < engineCombo.getItemCount(); i++) {
			if (defaultEngine != null
					&& engineCombo.getItem(i).equals(
							defaultEngine.getUserName())) {
				currentEngine = engines[i];
				engineCombo.select(i);
				break;
			}
		}

		ignoreEngineSelection = false;
		topLine.pack(true);
		topLine.layout(true, true);
	}
	
	public AutomaticAnalysisController getAnalysisController() {
		return analysisController;
	}

	public void setAnalysisController(AutomaticAnalysisController analysisController) {
		this.analysisController = analysisController;
	}
}
