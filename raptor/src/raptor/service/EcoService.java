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
package raptor.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chess.EcoInfo;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Variant;
import raptor.chess.util.GameUtils;
import raptor.util.RaptorStringTokenizer;

/**
 * A singleton service which can be used to lookup the opening description and
 * ECO code of the current position in a game.
 * 
 * Currently this service only supports Classic but hopefully others will
 * contribute files to match other variants (bug,zh,suicide,losers,etc).
 */
public class EcoService {

	private static final Log LOG = LogFactory.getLog(EcoService.class);

	private static final EcoService singletonInstance = new EcoService();

	private Map<Variant, Map<String, EcoInfo>> typeToFenToEco = new HashMap<Variant, Map<String, EcoInfo>>();

	public static EcoService getInstance() {
		return singletonInstance;
	}

	private EcoService() {
		initClassic();
	}

	/**
	 * Disposes the EcoService.
	 */
	public void dispose() {
		typeToFenToEco.clear();
	}

	/**
	 * Returns the ECO code for the specified game, null if one could not be
	 * found.
	 */
	public String getEco(Game game) {
		// Don't add debug messages in here. It gets called so often they are
		// annoying and really slow it down.
		Map<String, EcoInfo> map = typeToFenToEco.get(getAdjustedVariant(game));
		if (map == null) {
			return null;
		} else {
			EcoInfo info = map.get(getFenKey(game, true));
			return info == null ? null : info.getEcoCode();
		}
	}

	/**
	 * Returns the long description of the opening for the specified game, null
	 * if one could not be found.
	 */
	public String getLongDescription(Game game) {
		// Don't add debug messages in here. It gets called so often they are
		// annoying and really slow it down.
		Map<String, EcoInfo> map = typeToFenToEco.get(getAdjustedVariant(game));
		if (map == null) {
			return null;
		} else {
			EcoInfo info = map.get(getFenKey(game, false));
			return info == null ? null : info.getOpening();
		}
	}

	protected Variant getAdjustedVariant(Game game) {
		if (Variant.isClassic(game.getVariant())) {
			return Variant.classic;
		} else {
			return game.getVariant();
		}
	}

	protected String getFenKey(Game game, boolean includeEP) {
		return game.toFenPosition()
				+ " "
				+ (game.isWhitesMove() ? 'w' : 'b')
				+ " "
				+ game.getFenCastle()
				+ " "
				+ (includeEP ? game.getEpSquare() == GameConstants.EMPTY_SQUARE ? "-"
						: GameUtils.getSan(game.getEpSquare())
						: "-");
	}

	private void initClassic() {
		File file = new File(raptor.Raptor.RESOURCES_DIR + "scidECO.txt");
		typeToFenToEco.put(Variant.classic, parse(file));
	}

	/**
	 * Parses information from an idx file. These files contain FEN on one line
	 * followed by a string on another line. The string can be either eco or
	 * description.
	 * 
	 * @param file
	 *            File containing the ECO information.
	 * @throws IOException
	 *             If something goes wrong during reading.
	 */
	private Map<String, EcoInfo> parse(File file) {
		if (LOG.isDebugEnabled()) {
			LOG.info("parse(" + file.getAbsolutePath() + ")");
		}
		long startTime = System.currentTimeMillis();
		Map<String, EcoInfo> result = new TreeMap<String, EcoInfo>();

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				if (StringUtils.isNotBlank(currentLine)) {

					RaptorStringTokenizer tok = new RaptorStringTokenizer(
							currentLine, " ", true);
					String eco = tok.nextToken();
					String description = tok.nextToken();

					String lastToken = null;
					while (!(lastToken = tok.nextToken()).contains("/")) {
						description += " " + lastToken;
					}
					String fen = lastToken + " " + tok.nextToken() + " "
							+ tok.nextToken() + " " + tok.nextToken();

					result.put(fen, new EcoInfo(fen, eco, description));
				}

			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
				}
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.info("parse( " + file.getAbsolutePath() + ") executed in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}

		return result;
	}
}
