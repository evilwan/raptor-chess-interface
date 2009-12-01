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
package raptor.engine.uci;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chess.Move;
import raptor.engine.uci.info.BestLineFoundInfo;
import raptor.engine.uci.info.CPULoadInfo;
import raptor.engine.uci.info.CurrentMoveInfo;
import raptor.engine.uci.info.DepthInfo;
import raptor.engine.uci.info.NodesPerSecondInfo;
import raptor.engine.uci.info.NodesSearchedInfo;
import raptor.engine.uci.info.ScoreInfo;
import raptor.engine.uci.info.SelectiveSearchDepthInfo;
import raptor.engine.uci.info.StringInfo;
import raptor.engine.uci.info.TableBaseHitsInfo;
import raptor.engine.uci.info.TimeInfo;
import raptor.engine.uci.options.UCIButton;
import raptor.engine.uci.options.UCICheck;
import raptor.engine.uci.options.UCICombo;
import raptor.engine.uci.options.UCISpinner;
import raptor.engine.uci.options.UCIString;
import raptor.service.ThreadService;
import raptor.util.RaptorStringTokenizer;

/**
 * A class representing a UCIEngine. Ponder is currently unsupported.
 * 
 * Information on UCI can be found in these URLs:
 * http://wbec-ridderkerk.nl/html/UCIProtocol.html
 * http://www.docstoc.com/docs/15900289/UCI-_=-Universal-Chess-Interface_
 */
public class UCIEngine {
	private static final Log LOG = LogFactory.getLog(UCIEngine.class);
	protected static final String[] SUPPORTED_INFO_TYPES = { "depth",
			"seldepth", "time", "nodes", "pv", "multipv", "score", "currmove",
			"currentmovenumber", "hashfull", "nps", "tbhits", "cpuload",
			"string" };

	protected Process process;
	protected boolean isUsingThreadService = true;
	protected BufferedReader in;
	protected PrintWriter out;
	protected boolean isConnected;
	protected Map<String, UCIOption> nameToOptions = new HashMap<String, UCIOption>();
	protected Map<String, String> overrideOptions = new HashMap<String, String>();
	protected String processPath;
	protected String engineName;
	protected String engineAuthor;
	protected Runnable goRunnable;
	protected boolean cancelGo;
	protected boolean multiplyBlackScoreByMinus1 = true;
	protected UCIBestMove lastBestMove;
	protected String[] parameters;
	protected String userName;
	protected boolean isDefault;
	protected Object stopSynch = new Object();
	protected String goAnalysisParameters = "infinite";

	/**
	 * Connects to the engine. After this method is invoked the engine name,
	 * engine author, and options will be populated in this object.
	 * 
	 * @return true if connection was successful, false otherwise.
	 */
	public boolean connect() {
		if (isConnected()) {
			return true;
		}

		try {
			long startTime = System.currentTimeMillis();

			if (parameters == null || parameters.length == 0) {
				process = new ProcessBuilder(processPath).start();
			} else {
				String[] args = new String[parameters.length + 1];
				args[0] = processPath;
				for (int i = 0; i < parameters.length; i++) {
					args[1 + i] = parameters[i];
				}
				process = new ProcessBuilder(args).start();
			}
			in = new BufferedReader(new InputStreamReader(process
					.getInputStream()), 10000);
			out = new PrintWriter(process.getOutputStream());

			send("uci");

			String currentLine = null;
			while ((currentLine = readLine()) != null) {
				LOG.debug(currentLine);
				if (currentLine.startsWith("id")) {
					parseIdLine(currentLine);
				} else if (currentLine.startsWith("option ")) {
					parseOptionLine(currentLine);
				} else if (currentLine.startsWith("uciok")) {
					break;
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Unknown response to uci ignoring: "
								+ currentLine);
					}
				}
			}

			sendAllNonDefaultOptions();
			isReady();

			if (LOG.isDebugEnabled()) {
				LOG.debug("engineName=" + engineName + " engineAuthor="
						+ engineAuthor + "Options:\n" + nameToOptions.values()
						+ " initialized in "
						+ (System.currentTimeMillis() - startTime));
			}

			return true;
		} catch (Throwable t) {
			LOG.error("Error connecting to UCI Engine " + this, t);
			disconnect();
			return false;
		}
	}

	/**
	 * Disconnects from the engine
	 */
	public void disconnect() {
		try {
			if (isConnected()) {
				process.destroy();
			}
		} catch (Throwable t) {
			LOG.error("Error disconnecting from UCIEngine " + this, t);
		} finally {
			resetConnectionState();
		}
	}

	public UCIEngine getDeepCopy() {
		UCIEngine result = new UCIEngine();
		result.setProcessPath(getProcessPath());
		result.setParameters(getParameters());
		result.setUserName(getUserName());
		result.setGoAnalysisParameters(getGoAnalysisParameters());
		result.nameToOptions = nameToOptions;
		result.overrideOptions = overrideOptions;
		result.isDefault = isDefault;
		return result;
	}

	public String getEngineAuthor() {
		return engineAuthor;
	}

	public String getEngineName() {
		return engineName;
	}

	public String getGoAnalysisParameters() {
		return goAnalysisParameters;
	}

	/**
	 * Returns the UCIOption with the specified name.
	 */
	public UCIOption getOption(String name) {
		return nameToOptions.get(name);
	}

	/**
	 * Returns an array of all supported option names.
	 */
	public String[] getOptionNames() {
		return nameToOptions.keySet().toArray(new String[0]);
	}

	public String getOverrideOption(String name) {
		return overrideOptions.get(name);
	}

	public String[] getOverrideOptionNames() {
		return overrideOptions.keySet().toArray(new String[0]);
	}

	public String[] getParameters() {
		return parameters;
	}

	/**
	 * Returns the path to the UCIEngine process.
	 */
	public String getProcessPath() {
		return processPath;
	}

	public String getUserName() {
		return userName;
	}

	/**
	 * go start calculating on the current position set up with the "position"
	 * command. There are a number of commands that can follow this command, all
	 * will be sent in the same string. If one command is not send its value
	 * should be interpreted as it would not influence the search.
	 * 
	 * <pre>
	 * searchmoves  .... 
	 * 		restrict search to this moves only
	 * 		Example: After &quot;position startpos&quot; and &quot;go infinite searchmoves e2e4 d2d4&quot;
	 * 		the engine should only search the two moves e2e4 and d2d4 in the initial position.
	 * ponder
	 * 		start searching in pondering mode.
	 * 		Do not exit the search in ponder mode, even if it's mate!
	 * 		This means that the last move sent in in the position string is the ponder move.
	 * 		The engine can do what it wants to do, but after a &quot;ponderhit&quot; command
	 * 		it should execute the suggested move to ponder on. This means that the ponder move sent by
	 * 		the GUI can be interpreted as a recommendation about which move to ponder. However, if the
	 * 		engine decides to ponder on a different move, it should not display any mainlines as they are
	 * 		likely to be misinterpreted by the GUI because the GUI expects the engine to ponder
	 * 	   on the suggested move.
	 * wtime 
	 * 		white has x msec left on the clock
	 * btime 
	 * 		black has x msec left on the clock
	 * winc 
	 * 		white increment per move in mseconds if x &gt; 0
	 * binc 
	 * 		black increment per move in mseconds if x &gt; 0
	 * movestogo 
	 *       there are x moves to the next time control,
	 * 		this will only be sent if x &gt; 0,
	 * 		if you don't get this and get the wtime and btime it's sudden death
	 * depth 
	 * 		search x plies only.
	 * nodes 
	 * 	   search x nodes only,
	 * mate 
	 * 		search for a mate in x moves
	 * movetime 
	 * 		search exactly x mseconds
	 * infinite
	 * 		search until the &quot;stop&quot; command. Do not exit the search without being told so in this mode!
	 * </pre>
	 */
	public void go(String options, final UCIInfoListener listener) {
		if (!isConnected()) {
			throw new IllegalStateException("Engine is not connected.");
		}

		lastBestMove = null;
		if (!isProcessingGo()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Entering go(" + options + ",...)");
			}

			if (StringUtils.isNotBlank(options)) {
				send("go");
			} else {
				send("go " + options);
			}

			Runnable runnable = goRunnable = new Runnable() {
				public void run() {
					try {
						String line = readLine();
						while (!cancelGo && line != null) {
							if (line.startsWith("info")) {
								parseInfoLine(line, listener);
							} else if (line.startsWith("bestmove")) {
								lastBestMove = parseBestMove(line);
								listener.engineSentBestMove(lastBestMove);
								break;
							}
							line = readLine();
						}
						goRunnable = null;
					} catch (Throwable t) {
						LOG.error("Error occured executng go ", t);
					}
				}
			};
			if (isUsingThreadService) {
				ThreadService.getInstance().run(runnable);
			} else {
				new Thread(runnable).start();
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Go is in process. Ignoring go call.");
			}
		}
	}

	/**
	 * Returns true if there is a connection to the UCIEngine, false otherwise.
	 */
	public boolean isConnected() {
		if (process != null) {
			try {
				process.exitValue();
				resetConnectionState();
				return false;
			} catch (IllegalThreadStateException itse) {
				return true;
			}
		}
		return false;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public boolean isMultiplyBlackScoreByMinus1() {
		return multiplyBlackScoreByMinus1;
	}

	/**
	 * Returns true if a go command is currently being processed, otherwise
	 * false.
	 */
	public boolean isProcessingGo() {
		return goRunnable != null;
	}

	/**
	 * Blocks until readyok is received.
	 * 
	 * 
	 * this is used to synchronize the engine with the GUI. When the GUI has
	 * sent a command or multiple commands that can take some time to complete,
	 * this command can be used to wait for the engine to be ready again or to
	 * ping the engine to find out if it is still alive. E.g. this should be
	 * sent after setting the path to the tablebases as this can take some time.
	 * This command is also required once before the engine is asked to do any
	 * search to wait for the engine to finish initializing. This command must
	 * always be answered with "readyok" and can be sent also when the engine is
	 * calculating in which case the engine should also immediately answer with
	 * "readyok" without stopping the search.
	 */
	public void isReady() {
		if (!isConnected()) {
			throw new IllegalStateException("Engine is not connected.");
		}
		if (isProcessingGo()) {
			// Do nothing currently not supported if isProcessingGo.
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Entering isReady()");
			}

			send("isready");
			try {
				String reply = readLine();
				while (reply != null && !reply.equalsIgnoreCase("readyok")) {
					reply = readLine();
				}
			} catch (Throwable t) {
				LOG.error("Error occured in isReady. Disconnecting.", t);
				disconnect();
			}
		}
	}

	public boolean isUsingThreadService() {
		return isUsingThreadService;
	}

	/**
	 * 
	 * 
	 * ucinewgame this is sent to the engine when the next search (started with
	 * "position" and "go") will be from a different game. This can be a new
	 * game the engine should play or a new game it should analyse but also the
	 * next position from a testsuite with positions only. If the GUI hasn't
	 * sent a "ucinewgame" before the first "position" command, the engine
	 * shouldn't expect any further ucinewgame commands as the GUI is probably
	 * not supporting the ucinewgame command. So the engine should not rely on
	 * this command even though all new GUIs should support it. As the engine's
	 * reaction to "ucinewgame" can take some time the GUI should always send
	 * "isready" after "ucinewgame" to wait for the engine to finish its
	 * operation.
	 */
	public void newGame() {
		if (!isConnected()) {
			throw new IllegalStateException("Engine is not connected.");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering newGame()");
		}

		if (isProcessingGo()) {
			stop();
		}
		send("ucinewgame");
	}

	/**
	 * ponderhit the user has played the expected move. This will be sent if the
	 * engine was told to ponder on the same move the user has played. The
	 * engine should continue searching but switch from pondering to normal
	 * search.
	 */
	public void ponderHit(Move move) {
		throw new UnsupportedOperationException(
				"ponderHit is not yet implemented");
	}

	/**
	 * Quits the program as soon as possible
	 */
	public void quit() {
		if (!isConnected()) {
			return;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering quit()");
		}

		send("quit");
		try {
			Thread.sleep(500);
		} catch (InterruptedException ie) {
		}
		disconnect();
	}

	/**
	 * debug [ on | off ] switch the debug mode of the engine on and off. In
	 * debug mode the engine should sent additional infos to the GUI, e.g. with
	 * the "info string" command, to help debugging, e.g. the commands that the
	 * engine has received etc. This mode should be switched off by default and
	 * this command can be sent any time, also when the engine is thinking.
	 */
	public void setDebug(boolean isOn) {
		if (!isConnected()) {
			throw new IllegalStateException("Engine is not connected.");
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering setDebug(" + isOn + ")");
		}

		send("debug " + (isOn ? "on" : "off"));
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public void setEngineAuthor(String engineAuthor) {
		this.engineAuthor = engineAuthor;
	}

	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}

	public void setGoAnalysisParameters(String goAnalysisParameters) {
		this.goAnalysisParameters = goAnalysisParameters;
	}

	public void setMultiplyBlackScoreByMinus1(boolean multiplyBlackScoreByMinus1) {
		this.multiplyBlackScoreByMinus1 = multiplyBlackScoreByMinus1;
	}

	/**
	 * setoption name [value ] this is sent to the engine when the user wants to
	 * change the internal parameters of the engine. For the "button" type no
	 * value is needed. One string will be sent for each parameter and this will
	 * only be sent when the engine is waiting. The name of the option in should
	 * not be case sensitive and can inludes spaces like also the value. The
	 * substrings "value" and "name" should be avoided in and to allow
	 * unambiguous parsing, for example do not use = "draw value". Here are some
	 * strings for the example below: "setoption name Nullmove value true\n"
	 * "setoption name Selectivity value 3\n"
	 * "setoption name Style value Risky\n" "setoption name Clear Hash\n"
	 * "setoption name NalimovPath value c:\chess\tb\4;c:\chess\tb\5\n"
	 */
	public void setOption(UCIOption option) {
		if (!isConnected()) {
			throw new IllegalStateException("Engine is not connected.");
		}

		try {
			if (!option.isDefaultValue()) {
				overrideOptions.put(option.getName(), option.getValue());
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Entering setOption(" + option + ")");
			}

			if (option instanceof UCIButton) {
				send("setoption " + option.getName());
			} else {
				send("setoption " + option.getName() + " value "
						+ option.getValue());
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Set UCIOption: " + option);
			}
		} catch (Throwable t) {
			LOG.warn("Error occured setting option: " + option, t);
			disconnect();
		}
	}

	public void setOverrideOption(String name, String value) {
		overrideOptions.put(name, value);
	}

	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}

	/**
	 * Sets the position to fen and passes in the specified moves
	 * 
	 * position [fen | startpos ] moves .... set up the position described in
	 * fenstring on the internal board and play the moves on the internal chess
	 * board. if the game was played from the start position the string
	 * "startpos" will be sent Note: no "new" command is needed. However, if
	 * this position is from a different game than the last position sent to the
	 * engine, the GUI should have sent a "ucinewgame" inbetween.
	 */
	public void setPosition(String fen, UCIMove[] moves) {
		if (!isConnected()) {
			throw new IllegalStateException("Engine is not connected.");
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering setPosition(" + fen + ",...)");
		}

		if (isProcessingGo()) {
			stop();
		}

		if (moves == null || moves.length == 0) {
			send("position fen " + fen);
		} else {
			String movesString = "";
			for (UCIMove move : moves) {
				movesString += (movesString.equals("") ? "" : " ")
						+ move.getValue();
			}
			send("position fen " + fen + " " + movesString);
		}
	}

	/**
	 * Sets the path to the engine process.
	 * 
	 * @param processPath
	 */
	public void setProcessPath(String processPath) {
		this.processPath = processPath;
	}

	/**
	 * Sets the position to the starting position and passes in the specified
	 * moves
	 * 
	 * position [fen | startpos ] moves .... set up the position described in
	 * fenstring on the internal board and play the moves on the internal chess
	 * board. if the game was played from the start position the string
	 * "startpos" will be sent Note: no "new" command is needed. However, if
	 * this position is from a different game than the last position sent to the
	 * engine, the GUI should have sent a "ucinewgame" inbetween.
	 */
	public void setStartingPosition(UCIMove[] moves) {
		if (!isConnected()) {
			throw new IllegalStateException("Engine is not connected.");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering setStartingPosition(...)");
		}

		if (isProcessingGo()) {
			stop();
		}

		if (moves == null || moves.length == 0) {
			send("position startpos");
		} else {
			String movesString = "";
			for (UCIMove move : moves) {
				movesString += (movesString.equals("") ? "" : " ")
						+ move.getValue();
			}
			send("position startpos " + movesString);
		}
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUsingThreadService(boolean isUsingThreadService) {
		this.isUsingThreadService = isUsingThreadService;
	}

	/**
	 * Stops a go that is in process. The UCIBestMove is returned.
	 */
	public UCIBestMove stop() {
		if (!isConnected()) {
			throw new IllegalStateException("Engine is not connected.");
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering stop(...)");
		}

		UCIBestMove result = null;

		synchronized (stopSynch) {
			if (!isProcessingGo()) {
				result = lastBestMove;
				lastBestMove = null;

			} else {
				long totalSleepTime = 0;
				send("stop");
				while (goRunnable != null && totalSleepTime < 2500) {
					try {
						Thread.sleep(500);
						totalSleepTime += 500;
					} catch (InterruptedException ie) {
					}
				}
				result = lastBestMove;
				lastBestMove = null;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return engineName != null ? engineName : processPath;
	}

	protected boolean isSupportedInfoType(String type) {
		boolean result = false;
		for (String currentType : SUPPORTED_INFO_TYPES) {
			if (currentType.equalsIgnoreCase(type)) {
				result = true;
				break;
			}
		}
		return result;
	}

	protected UCIBestMove parseBestMove(String bestMove) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("parseBestMove(" + bestMove + ")");
		}

		RaptorStringTokenizer tok = new RaptorStringTokenizer(bestMove, " ",
				true);

		UCIBestMove result = new UCIBestMove();
		result.setBestMove(parseUCIMove(tok.nextToken()));

		if (tok.hasMoreTokens()) {
			String nextToken = tok.nextToken();
			if (nextToken.equalsIgnoreCase("ponder")) {
				result.setPonderMove(parseUCIMove(tok.nextToken()));
			}
		}

		return result;
	}

	protected void parseIdLine(String idLine) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Parsing id line: " + idLine);
		}

		RaptorStringTokenizer tok = new RaptorStringTokenizer(idLine, " ", true);
		tok.nextToken();
		String varName = tok.nextToken();
		String varValue = tok.getWhatsLeft();

		if (varName.equalsIgnoreCase("name")) {
			engineName = varValue;
		} else if (varName.equalsIgnoreCase("author")) {
			engineAuthor = varValue;
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("Unknown id variable name. " + varName + "="
						+ varValue);
			}
		}
	}

	/**
	 * the engine wants to send infos to the GUI. This should be done whenever
	 * one of the info has changed. The engine can send only selected infos and
	 * multiple infos can be send with one info command, e.g.
	 * "info currmove e2e4 currmovenumber 1" or
	 * "info depth 12 nodes 123456 nps 100000". Also all infos belonging to the
	 * pv should be sent together e.g.
	 * "info depth 2 score cp 214 time 1242 nodes 2124 nps 34928 pv e2e4 e7e5 g1f3"
	 * I suggest to start sending "currmove", "currmovenumber", "currline" and
	 * "refutation" only after one second to avoid too much traffic.
	 * 
	 * <pre>
	 * Additional info:
	 * depth 
	 * 		search depth in plies
	 * seldepth 
	 * 		selective search depth in plies,
	 * 		if the engine sends seldepth there must also a &quot;depth&quot; be present in the same string.
	 * time 
	 * 		the time searched in ms, this should be sent together with the pv.
	 * nodes 
	 * 		x nodes searched, the engine should send this info regularly
	 * pv  ... 
	 * 		the best line found
	 * multipv 
	 * 		this for the multi pv mode.
	 * 		for the best move/pv add &quot;multipv 1&quot; in the string when you send the pv.
	 * 		in k-best mode always send all k variants in k strings together.
	 * score
	 * cp 
	 * 			the score from the engine's point of view in centipawns.
	 * mate 
	 * 			mate in y moves, not plies.
	 * 			If the engine is getting mated use negativ values for y.
	 * lowerbound
	 * 	      the score is just a lower bound.
	 * upperbound
	 * 		   the score is just an upper bound.
	 * currmove 
	 * 		currently searching this move
	 * currmovenumber 
	 * 		currently searching move number x, for the first move x should be 1 not 0.
	 * hashfull 
	 * 		the hash is x permill full, the engine should send this info regularly
	 * nps 
	 * 		x nodes per second searched, the engine should send this info regularly
	 * tbhits 
	 * 		x positions where found in the endgame table bases
	 * cpuload 
	 * 		the cpu usage of the engine is x permill.
	 * string 
	 * 		any string str which will be displayed be the engine,
	 * 		if there is a string command the rest of the line will be interpreted as .
	 * refutation   ... 
	 * 	   move  is refuted by the line  ... , i can be any number &gt;= 1.
	 * 	   Example: after move d1h5 is searched, the engine can send
	 * 	   &quot;info refutation d1h5 g6h5&quot;
	 * 	   if g6h5 is the best answer after d1h5 or if g6h5 refutes the move d1h5.
	 * 	   if there is norefutation for d1h5 found, the engine should just send
	 * 	   &quot;info refutation d1h5&quot;
	 * 		The engine should only send this if the option &quot;UCI_ShowRefutations&quot; is set to true.
	 * currline   ... 
	 * 	   this is the current line the engine is calculating.  is the number of the cpu if
	 * 	   the engine is running on more than one cpu.  = 1,2,3....
	 * 	   if the engine is just using one cpu,  can be omitted.
	 * 	   If  is greater than 1, always send all k lines in k strings together.
	 * 		The engine should only send this if the option &quot;UCI_ShowCurrLine&quot; is set to true.
	 * </pre>
	 * 
	 * Examples:
	 * 
	 * <pre>
	 * go infinite
	 * info depth 1 seldepth 0 time 34 nodes 0 nps 151466 score cp 1 pv c7c5 
	 * info nps 151466 nodes 0 cpuload 0 hashfull 0 time 35
	 * bestmove c7c5 
	 * stop
	 * </pre>
	 */
	protected void parseInfoLine(String info, UCIInfoListener listener) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering parseInfoLine(" + info + ",...)");
		}
		RaptorStringTokenizer tok = new RaptorStringTokenizer(info, " ", true);
		tok.nextToken();

		int currentMoveNumber = 0;

		List<UCIInfo> infos = new ArrayList<UCIInfo>(10);

		String nextType = null;
		while (tok.hasMoreTokens()) {
			String type = null;
			if (nextType != null) {
				type = nextType;
				nextType = null;
			} else {
				type = tok.nextToken();
			}

			while (!isSupportedInfoType(type) && tok.hasMoreTokens()) {
				type = tok.nextToken();
			}

			if (!isSupportedInfoType(type)) {
				break;
			}

			if (type.equalsIgnoreCase("depth")) {
				DepthInfo depthInfo = new DepthInfo();
				depthInfo
						.setSearchDepthPlies(Integer.parseInt(tok.nextToken()));
				infos.add(depthInfo);
			} else if (type.equalsIgnoreCase("seldepth")) {
				SelectiveSearchDepthInfo ssDepthInfo = new SelectiveSearchDepthInfo();
				ssDepthInfo.setDepthInPlies(Integer.parseInt(tok.nextToken()));
				infos.add(ssDepthInfo);
			} else if (type.equalsIgnoreCase("time")) {
				TimeInfo timeInfo = new TimeInfo();
				timeInfo.setTimeMillis(Integer.parseInt(tok.nextToken()));
				infos.add(timeInfo);
			} else if (type.equalsIgnoreCase("nodes")) {
				NodesSearchedInfo nodesSearched = new NodesSearchedInfo();
				nodesSearched.setNodesSearched(Integer
						.parseInt(tok.nextToken()));
				infos.add(nodesSearched);
			} else if (type.equalsIgnoreCase("pv")) {
				BestLineFoundInfo bestLineFoundInfo = new BestLineFoundInfo();
				String currentMove = tok.nextToken();
				List<UCIMove> currentLine = new ArrayList<UCIMove>(10);
				while (true) {
					currentLine.add(new UCIMove(currentMove));
					if (tok.hasMoreTokens()) {
						currentMove = tok.nextToken();
						if (isSupportedInfoType(currentMove)) {
							nextType = currentMove;
							break;
						}
					} else {
						break;
					}
				}
				bestLineFoundInfo.setMoves(currentLine.toArray(new UCIMove[0]));
				infos.add(bestLineFoundInfo);
			} else if (type.equalsIgnoreCase("multipv")) {
				tok.nextToken();
			} else if (type.equalsIgnoreCase("score")) {
				ScoreInfo scoreInfo = new ScoreInfo();
				String nextToken = tok.nextToken();

				while (true) {
					if (nextToken.equalsIgnoreCase("cp")) {
						scoreInfo.setValueInCentipawns(Integer.parseInt(tok
								.nextToken()));
					} else if (nextToken.equalsIgnoreCase("mate")) {
						scoreInfo.setMateInMoves(Integer.parseInt(tok
								.nextToken()));
					} else if (nextToken.equalsIgnoreCase("lowerbound")) {
						scoreInfo.setLowerBoundScore(true);
					} else if (nextToken.equalsIgnoreCase("upperbound")) {
						scoreInfo.setUpperBoundScore(true);
					} else {
						nextType = nextToken;
						break;
					}
					if (tok.hasMoreTokens()) {
						nextToken = tok.nextToken();
					} else {
						break;
					}
				}
				infos.add(scoreInfo);
			} else if (type.equalsIgnoreCase("currmove")) {
				CurrentMoveInfo currentMoveInfo = new CurrentMoveInfo();
				currentMoveInfo.setMove(parseUCIMove(tok.nextToken()));
				currentMoveInfo.setMoveNumber(currentMoveNumber);
				infos.add(currentMoveInfo);
			} else if (type.equalsIgnoreCase("currentmovenumber")) {
				currentMoveNumber = Integer.parseInt(tok.nextToken());
			} else if (type.equalsIgnoreCase("hashfull")) {
				tok.nextToken();
			} else if (type.equalsIgnoreCase("nps")) {
				NodesPerSecondInfo nodesPerSecInfo = new NodesPerSecondInfo();
				nodesPerSecInfo.setNodesPerSecond(Integer.parseInt(tok
						.nextToken()));
				infos.add(nodesPerSecInfo);
			} else if (type.equalsIgnoreCase("tbhits")) {
				TableBaseHitsInfo tbInfo = new TableBaseHitsInfo();
				tbInfo.setNumberOfHits(Integer.parseInt(tok.nextToken()));
				infos.add(tbInfo);
			} else if (type.equalsIgnoreCase("cpuload")) {
				CPULoadInfo cpuInfo = new CPULoadInfo();
				cpuInfo.setCpuUsage(Integer.parseInt(tok.nextToken()));
				infos.add(cpuInfo);
			} else if (type.equalsIgnoreCase("string")) {
				StringInfo stringInfo = new StringInfo();
				stringInfo.setValue(tok.getWhatsLeft().trim());
				infos.add(stringInfo);
			} else {
				LOG.warn("Unkown type: " + type);
			}
		}
		listener.engineSentInfo(infos.toArray(new UCIInfo[0]));
	}

	protected void parseOptionLine(String optionLine) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Parsing option line: " + optionLine);
		}

		RaptorStringTokenizer tok = new RaptorStringTokenizer(optionLine, " ",
				true);
		tok.nextToken();
		tok.nextToken();
		String name = parseUntil("type", tok);
		String type = tok.nextToken();
		UCIOption option = null;

		if (type.equalsIgnoreCase("spin")) {
			String defaultValue = null;
			int minValue = -1;
			int maxValue = -1;

			while (tok.hasMoreTokens()) {
				String nextToken = tok.nextToken();
				if (nextToken.equalsIgnoreCase("default")) {
					defaultValue = tok.nextToken();
				} else if (nextToken.equals("min")) {
					minValue = Integer.parseInt(tok.nextToken());
				} else if (nextToken.equalsIgnoreCase("max")) {
					maxValue = Integer.parseInt(tok.nextToken());
				}
			}

			if (defaultValue == null) {
				LOG
						.warn("Spinner type encountered without a default. Ignoring option. "
								+ optionLine);
			} else if (minValue == -1) {
				LOG
						.warn("Spinner type encountered without a min. Ignoring option. "
								+ optionLine);
			} else if (maxValue == -1) {
				LOG
						.warn("Spinner type encountered without a max. Ignoring option. "
								+ optionLine);
			}

			UCISpinner spinner = new UCISpinner();
			spinner.setDefaultValue(defaultValue);
			spinner.setName(name);
			spinner.setMaximum(maxValue);
			spinner.setMinimum(minValue);
			option = spinner;

		} else if (type.equalsIgnoreCase("string")) {
			String defaultValue = null;
			if (tok.hasMoreTokens()) {
				if (tok.nextToken().equalsIgnoreCase("default")) {
					if (tok.hasMoreTokens()) {
						defaultValue = tok.getWhatsLeft().trim();
					}
				}
			}
			UCIString string = new UCIString();
			string.setName(name);
			string.setDefaultValue(defaultValue);
			option = string;
		} else if (type.equalsIgnoreCase("check")) {
			String defaultValue = null;
			if (tok.hasMoreTokens()) {
				if (tok.nextToken().equalsIgnoreCase("default")) {
					if (tok.hasMoreTokens()) {
						defaultValue = tok.nextToken();
					}
				}
			}
			UCICheck check = new UCICheck();
			check.setName(name);
			check
					.setDefaultValue(defaultValue == null ? "false"
							: defaultValue);
			option = check;
		} else if (type.equalsIgnoreCase("combo")) {
			String nextToken = tok.nextToken();
			List<String> options = new ArrayList<String>(10);
			String defaultValue = null;
			if (nextToken.equalsIgnoreCase("default")) {
				defaultValue = parseUntil("var", tok);
			}

			while (tok.hasMoreTokens()) {
				options.add(parseUntil("var", tok));
			}

			UCICombo combo = new UCICombo();
			combo.setName(name);
			combo.setDefaultValue(defaultValue);
			combo.setOptions(options.toArray(new String[0]));
			option = combo;
		} else if (type.equalsIgnoreCase("button")) {
			UCIButton button = new UCIButton();
			button.setName(name);
			option = button;
		} else {
			LOG
					.warn("Unknown option type encountered in line (Please post this on the Raptor site as an issue so it can be added). "
							+ optionLine);
		}

		if (!nameToOptions.containsKey(name)) {
			nameToOptions.put(name, option);
		}
	}

	protected UCIMove parseUCIMove(String move) {
		return new UCIMove(move);
	}

	protected String parseUntil(String untilKeyword, RaptorStringTokenizer tok) {
		String result = "";
		String token = tok.nextToken();
		while (!token.equalsIgnoreCase(untilKeyword) && tok.hasMoreTokens()) {
			result += (result.equals("") ? "" : " ") + token;
			token = tok.nextToken();
		}

		if (!token.equalsIgnoreCase(untilKeyword)) {
			result += (result.equals("") ? "" : " ") + token;
		}
		return result;
	}

	protected String readLine() throws Exception {
		return in.readLine();
	}

	protected void resetConnectionState() {
		in = null;
		out = null;
		process = null;
		engineName = null;
		goRunnable = null;
		lastBestMove = null;
	}

	protected void send(String command) {
		if (!isConnected()) {
			return;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Sending command: " + command);
		}
		try {
			out.print(command + "\n");
			out.flush();
		} catch (Throwable t) {
			LOG.error("Error sending " + command + " to UCI Engine "
					+ toString(), t);
			disconnect();
		}
	}

	/**
	 * Sends all of the options that do not have default values to the engine.
	 */
	protected void sendAllNonDefaultOptions() {
		for (String overrideOption : overrideOptions.keySet()) {
			UCIOption option = getOption(overrideOption);
			if (option != null) {
				option.setValue(overrideOptions.get(overrideOption));
			} else {
				LOG.warn("Could not set default value for property "
						+ overrideOption);
			}
		}
	}
}
