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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chess.EcoInfo;
import raptor.chess.Game;
import raptor.chess.Variant;

/**
 * A singleton service which can be used to lookup EcoInfo on a game.
 * 
 * This service was modeled from initial code John Nahlen produced.
 * 
 * ECO.txt is a modified version of the ECO.txt in BabasChess.
 * 
 * Currently this service only supports Classic but hopefully others will
 * contribute files to match other variants (bug,zh,suicide,losers,etc).
 */
public class EcoService {

	private static final Log LOG = LogFactory.getLog(EcoService.class);

	private static final EcoService singletonInstance = new EcoService();

	public static EcoService getInstance() {
		return singletonInstance;
	}

	private Map<Variant, Map<String, EcoInfo>> typeToInfoMap = new HashMap<Variant, Map<String, EcoInfo>>();

	private EcoService() {
		initClassic();
	}

	/**
	 * Disposes the EcoService.
	 */
	public void dispose() {
		typeToInfoMap.clear();
	}

	/**
	 * @param game
	 *            Instance of Game that an ECOParser object needs to be
	 *            retrieved from.
	 * @return ECOParser instance if found, <code>null</code> if not.
	 */
	public EcoInfo getEcoInfo(Game game) {
		// Don't add debug messages in here. It gets called so often they are
		// annoying and really slow it down.

		Map<String, EcoInfo> map = typeToInfoMap.get(game.getVariant());
		EcoInfo result = null;

		if (map != null) {
			result = map.get(game.toFenPosition() + " "
					+ (game.isWhitesMove() ? 'w' : 'b'));
		} else {
			result = null;
		}

		return result;
	}

	private void initClassic() {
		File file = new File(raptor.Raptor.RESOURCES_COMMON_DIR + "ECOFen.txt");
		typeToInfoMap.put(Variant.classic, parse(file));
	}

	/**
	 * Parses ECO information from File <code>file</code>.<br />
	 * Example line in File <code>file</code>: A3|A00|Anderssen's
	 * Opening|Romford counter-gambit.
	 * 
	 * The result list will be sorted ascending by key length.
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
		Map<String, EcoInfo> result = new HashMap<String, EcoInfo>();

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			while (reader.ready()) {
				String line = reader.readLine();
				String[] arr = line.split("\\|");
				String varName = "";
				if (arr.length == 4) {
					varName = arr[3];
				}
				if (arr.length < 3) {
					continue;
				}
				EcoInfo info = new EcoInfo(arr[0], arr[1], arr[2], varName);
				result.put(arr[0], info);
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
