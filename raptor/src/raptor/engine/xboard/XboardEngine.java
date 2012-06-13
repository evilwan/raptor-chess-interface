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
package raptor.engine.xboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import raptor.chess.Variant;
import raptor.service.ThreadService;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringTokenizer;

public class XboardEngine {
	private static final RaptorLogger LOG = RaptorLogger
			.getLog(XboardEngine.class);
	protected static final long CONNECTION_TIMEOUT = 5000;

	protected BufferedReader in;
	protected PrintWriter out;
	protected Process process;
	protected String processPath;
	protected String engineName;
	protected boolean isDefault;

	protected boolean supportsSetboard;
	private Runnable readerRunnable;
	protected Object stopSynch = new Object();
	protected boolean cancelGo;
	private boolean isUsingThreadService = true;
	protected List<Variant> supportedVariants = new ArrayList<Variant>();
	private boolean processingGo;
	private boolean colorCommand;

	/**
	 * Connects to the engine using specified processPath. Also sets the engine
	 * name deducing it from filename by default.
	 * 
	 * @return true if connection was successful, false otherwise
	 */
	public boolean connect() {
		try {
			long startTime = System.currentTimeMillis();
			
			// a bit dirty, but that the way it is
			process = new ProcessBuilder(processPath).directory(
					new File(new File(processPath).getParent())).start();
			in = new BufferedReader(new InputStreamReader(process
					.getInputStream()), 10000);
			out = new PrintWriter(process.getOutputStream());	

			send("xboard");
			send("protover 2");

			String currentLine = null;
			while ((currentLine = in.readLine()) != null) {
				if (!StringUtils.isBlank(currentLine)) {
					if (currentLine.contains("feature")) {
						if (!processFeatureString(currentLine))
							return false;
						
						if (currentLine.contains("done=1"))
							break;
					} else if (LOG.isDebugEnabled()) {
						LOG.debug("Unknown response to xboard ignoring: "
								+ currentLine);
					}					

				}
			}
			
			if (engineName == null) {
				String name = new File(processPath).getName();
				engineName = name.endsWith(".exe") ? name.substring(0, name
						.length() - 4) : name;
			}
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("engineName=\"" + engineName 
						+ "\" initialized in "
						+ (System.currentTimeMillis() - startTime));
			}

		} catch (Throwable t) {
			LOG.error("Error connecting to Xboard Engine " + this, t);
			disconnect();
			return false;
		}

		return true;
	}

	public void analyze(final XboardInfoListener listener) {
		
		if (readerRunnable == null) {
			readerRunnable = new Runnable() {
				public void run() {
					try {
						String line = in.readLine();
						while (line != null) {							
							if (line.length() > 1
									&& Character.isDigit(line.trim().charAt(0))) {
								parseInfoLine(line, listener);
							}

							line = in.readLine();
						}
						readerRunnable = null;
					}  
					catch (Throwable t) {
						LOG.error("Error occured executng go ", t);
					}
				}
			};
			if (isUsingThreadService) {
				ThreadService.getInstance().run(readerRunnable);
			} else {
				new Thread(readerRunnable).start();
			}
		}

		if (!processingGo) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Entering analyze");
			}

			send("analyze");
			processingGo = true;
		}
	}
	
	protected void parseInfoLine(String info, XboardInfoListener listener) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering parseInfoLine(" + info + ",...)");
		}

		String ply = null, score = null, time = null, nodes = null;
		StringBuffer pv = new StringBuffer();
		List<String> infos = new ArrayList<String>(10);

		RaptorStringTokenizer tok = new RaptorStringTokenizer(info, " ", true);

		while (tok.hasMoreTokens()) {
			infos.add(tok.nextToken());
		}

		ply = infos.get(0);
		score = infos.get(1);
		time = infos.get(2);
		nodes = infos.get(3).trim();
		
		for (int i = 4; i < infos.size(); i++) {
            pv.append(infos.get(i)).append(" ");
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Parsed ply=\"" + ply + "\" score=\"" + score
					+ "\" time=\"" + time + "\" nodes=\"" + nodes + "\" pv=\""
					+ pv + "\"");
		}

		listener.engineSentInfo(ply, score, time, nodes, pv.toString());
	}
	
	public void setPosition(String fen, boolean isWhite) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering setPosition(" + fen + ",...)");
		}

		if (processingGo) {
			stop();
		}		
		
		send("force");
		send("setboard " + fen);
		
		if (colorCommand)
			send(isWhite ? "white" : "black");
	}

	public void stop() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering stop(...)");
		}
		
		synchronized (stopSynch) {
			if (processingGo) {
				send("exit");
				processingGo = false;
			}
		}
	}

	public boolean isProcessingGo() {
		return processingGo;
	}

	/**
	 * Processes "feature" command from engine. See section 9 of the
	 * specification. I decided not to use any high level tools here.
	 */
	private boolean processFeatureString(String str) {
		str = str.substring(str.indexOf("feature"));
		StringBuffer word = new StringBuffer();
		boolean insideQuotes = false;
		int i = 0;
		for (char c: str.toCharArray()) {
			word.append(c);
			if ((c == ' ' && !insideQuotes) || str.length()-1 == ++i) {
				if (LOG.isDebugEnabled())
					LOG.debug("Proccessing feature string word: " + word);
				
				String w = word.toString().trim();
				if (w.startsWith("setboard"))
					supportsSetboard = w.endsWith("1");
				else if (w.startsWith("analyze")) {
					if (!w.endsWith("1"))
						return false;					
				}
				else if (w.startsWith("myname")) {
					engineName = w.substring(8, w.length()-1);
				}
				else if (w.contains("colors=1")) {					
					colorCommand = true;
				}
				else if (w.startsWith("variants")) {
					convertStringToVariantsList(w);

					if (LOG.isDebugEnabled())
						LOG.debug("Supported variants: "
								+ supportedVariants.toString());
				}
				
				word.setLength(0);
				continue;
			}
			if (c == '"')
				insideQuotes = !insideQuotes;
		}
		
		return true;
	}
	
	public void setSupportedVariants(String variants) {
		convertStringToVariantsList(variants);
	}

	private void convertStringToVariantsList(String w) {
		if (w.contains("crazyhouse"))
			supportedVariants.add(Variant.crazyhouse);
		if (w.contains("losers"))
			supportedVariants.add(Variant.losers);
		if (w.contains("suicide"))
			supportedVariants.add(Variant.suicide);
		if (w.contains("atomic"))
			supportedVariants.add(Variant.atomic);
	}

	public void send(String command) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sending command: " + command);
		}
		
		if (!isConnected())
			return;
		
		try {
			out.print(command + "\n");
			out.flush();
		} catch (Throwable t) {
			LOG.error("Error sending " + command + " to Xboard Engine "
					+ toString(), t);
			disconnect();
		}
	}

	/**
	 * Disconnects from the engine
	 */
	protected void disconnect() {
		try {
			if (isConnected()) {
				process.destroy();

				if (in != null) {
					try {
						in.close();
					} catch (Throwable t) {
					} finally {
						in = null;
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (Throwable t) {
					} finally {
						out = null;
					}
				}
			}
		} catch (Throwable t) {
			LOG.error("Error disconnecting from XboardEngine " + this, t);
		} finally {
			resetConnectionState();
		}
	}
	
	/**
	 * Returns true if there is a connection to the XboardEngine, false otherwise.
	 */
	public boolean isConnected() {
		if (process != null) {
			try {
				process.exitValue();
				return false;
			} catch (IllegalThreadStateException itse) {
				return true;
			}
		}
		return false;
	}
	
	protected void resetConnectionState() {
		in = null;
		out = null;
		process = null;
		engineName = null;
		readerRunnable = null;
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
			Thread.sleep(100);
		} catch (InterruptedException ie) {
		}
		disconnect();
	}

	public String getProcessPath() {
		return processPath;
	}

	public String getEngineName() {
		return engineName;
	}

	public void setProcessPath(String processPath) {
		this.processPath = processPath;
	}

	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}

	public void setUsingThreadService(boolean isUsingThreadService) {
		this.isUsingThreadService = isUsingThreadService;
	}

	public XboardEngine getDeepCopy() {
		XboardEngine engine = new XboardEngine();
		engine.setProcessPath(processPath);
		engine.setEngineName(engineName);
		return engine;
	}

	public void newGame(Variant var) {
		if (processingGo) {
			stop();
		}
		send("new");
		send("variant " + var.name());
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public boolean doesSupportVariant(Variant variant) {
		return supportedVariants.contains(variant);
	}

	public String supportedVariantsInString() {
		return supportedVariants.toString();
	}
}
